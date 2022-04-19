package com.rick.cameraapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object CameraPermissionHelper {
    private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private const val READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE

    fun hasCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, READ_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    /*requestPermissions and it is used to request the camera and storage permissions. The user
can either grant or refuse permission. If they refuse, the user might not understand why the permissions have been
requested. In which case, we can use a method called shouldShowRequestPermissionRationale to check whether the
user has refused a given permission. If the permission has been refused, then the
shouldShowRequestPermissionRationale method returns a value of true. In which case, the above code builds an
alert dialog that will display the message contained in the permission_required string resource. The alert dialog will
also feature an OK button that will request the permissions again when clicked.*/

    fun requestPermissions(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)) {
            AlertDialog.Builder(activity).apply {
                setMessage(activity.getString(R.string.permission_required))
                setPositiveButton(activity.getString(R.string.ok)){_, _ ->
                    ActivityCompat.requestPermissions(activity, arrayOf(CAMERA_PERMISSION, READ_PERMISSION), 1)
                    show()
                }
            }
        } else ActivityCompat.requestPermissions(activity, arrayOf(CAMERA_PERMISSION, READ_PERMISSION), 1)
    }
}