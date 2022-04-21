package com.rick.cameraapp.ui.camera

import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.rick.cameraapp.CameraPermissionHelper
import com.rick.cameraapp.MainActivity
import com.rick.cameraapp.R
import com.rick.cameraapp.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cameraExecutor = Executors.newSingleThreadExecutor()
        openCamera()

        binding.apply {
            fab.setOnClickListener {
                capturePhoto()
            }
        }

        return root
    }

    private fun capturePhoto() {
        if (!this::imageCapture.isInitialized) {
            Toast.makeText(
                requireActivity(),
                resources.getString(R.string.error_saving),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val contentValues = (activity as MainActivity).prepareContentValues()

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            requireActivity().applicationContext.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputFileOptions, ContextCompat.getMainExecutor(requireActivity()), object :
                ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.photo_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.error_saving),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun openCamera() {
        if (CameraPermissionHelper.hasCameraPermission(requireActivity()) && CameraPermissionHelper.hasStoragePermission(
                requireActivity()
            )
        ) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(binding.cameraFeed.surfaceProvider) }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (e: IllegalStateException) {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.error_camera),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }, ContextCompat.getMainExecutor(requireActivity()))
        } else CameraPermissionHelper.requestPermissions(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}