package com.rick.cameraapp.ui.gallery

import android.app.RecoverableSecurityException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.rick.cameraapp.CameraPermissionHelper
import com.rick.cameraapp.MainActivity
import com.rick.cameraapp.Photo
import com.rick.cameraapp.R
import com.rick.cameraapp.databinding.FragmentGalleryBinding
import com.rick.cameraapp.ui.camera.GalleryViewModel

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private lateinit var viewModel: GalleryViewModel
    private lateinit var galleryAdapter: GalleryAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        galleryAdapter = GalleryAdapter(activity as MainActivity, this)
        binding.root.layoutManager = GridLayoutManager(context, 3)
        binding.root.adapter = galleryAdapter
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        viewModel.apply {
            photos.observe(viewLifecycleOwner) { photos ->
                photos?.let {
                    galleryAdapter.notifyItemRangeRemoved(
                        0,
                        galleryAdapter.itemCount
                    )
                    galleryAdapter.photos = it
                    galleryAdapter.notifyItemRangeInserted(
                        0,
                        it.size
                    )
                }
            }
            if (CameraPermissionHelper.hasStoragePermission(requireActivity())) loadPhotos()
            else CameraPermissionHelper.requestPermissions(requireActivity())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showPopup(view: View?, photo: Photo) {
        val popup = PopupMenu(requireActivity(), view)
        popup.inflate(R.menu.popup)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popupdelete -> {
                    viewModel.photoToDelete = photo
                    deletePhoto()
                }
            }
            true
        }
        popup.show()
    }

    private fun deletePhoto() {
        /*requests to delete images may throw a
recoverable security exception that requires the user to grant permission for the action to proceed. If a recoverable
security exception is thrown, then an IntentSenderRequest object is built to prompt the device to ask the user
whether they wish to modify (or delete) the image fil*/
        try {
            val photo = viewModel.photoToDelete ?: return
            val rowsDeleted =
                requireActivity().applicationContext.contentResolver.delete(photo.uri, null)

            if (rowsDeleted == 1) viewModel.photoToDelete = null
        } catch (e: RecoverableSecurityException) {
            val intentSender = e.userAction.actionIntent.intentSender
            val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
            registerResult.launch(intentSenderRequest)
        }
    }

    /*The IntentSenderRequest object will be initiated and monitored by an activity request launcher.*/
    private val registerResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            // Result code of 0 means the user declined permission to delete the photo
            if (result.resultCode != 0) deletePhoto()
        }
}