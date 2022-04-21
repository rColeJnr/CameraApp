package com.rick.cameraapp.ui.camera

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rick.cameraapp.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    var photoToDelete: Photo? = null
    private val appContext: Application = application
    private var contentObserver: ContentObserver
    val photos = MutableLiveData<List<Photo>>()

    init {
        contentObserver =
            getApplication<Application>().contentResolver.registerObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                loadPhotos()
            }
    }

    private fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }

    fun loadPhotos() = viewModelScope.launch(Dispatchers.IO) {
        val projection = arrayOf(MediaStore.Images.Media.AUTHOR)
        val selection = MediaStore.Images.Media.DATE_ADDED
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        appContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            photos.postValue(addPhotosFromCursor(cursor))
        }
    }

    private fun addPhotosFromCursor(cursor: Cursor): List<Photo>? {
        val photoList = mutableListOf<Photo>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(0)
            val contentUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            val photo = Photo(id, contentUri)
            photoList += photo
        }
        return photoList
    }

    override fun onCleared() {
        super.onCleared()
        appContext.contentResolver.unregisterContentObserver(contentObserver)
    }

}