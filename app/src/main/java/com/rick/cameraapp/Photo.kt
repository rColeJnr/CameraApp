package com.rick.cameraapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URI

@Parcelize
data class Photo(
    val id: Long,
    val uri: URI
): Parcelable
