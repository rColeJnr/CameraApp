package com.rick.cameraapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rick.cameraapp.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        3
        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_gallery, R.id.navigation_camera
            )
        )
        binding.navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!CameraPermissionHelper.hasCameraPermission(this) || !CameraPermissionHelper.hasStoragePermission(this)) {
            CameraPermissionHelper.requestPermissions(this)
        } else recreate() /*If the hasCameraPermission
and hasStoragePermission methods both return values of true, then the recreate method will reload the activity
because the necessary user permissions have been granted.*/
    }

    fun prepareContentValues(): ContentValues {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "image_$timeStamp"

        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures")
            put(MediaStore.MediaColumns.AUTHOR, "rColeJnr")
        }
    }

    fun saveImage(image: Bitmap) {
        val resolver = applicationContext.contentResolver
        val imageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, prepareContentValues())

        try {
            val fos = imageUri?.let { resolver.openOutputStream(it) }
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos?.close()
            Toast.makeText(this, resources.getString(R.string.photo_saved), Toast.LENGTH_LONG)
                .show()
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, resources.getString(R.string.error_saving), Toast.LENGTH_LONG)
                .show()
        } catch (e: IOException) {
            Toast.makeText(this, resources.getString(R.string.error_saving), Toast.LENGTH_LONG)
                .show()
        }
    }
}

