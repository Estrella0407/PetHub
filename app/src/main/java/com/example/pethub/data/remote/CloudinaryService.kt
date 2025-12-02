package com.example.pethub.data.remote

import android.net.Uri
import com.cloudinary.android.MediaManager // ✅ Correct Cloudinary Import
import com.cloudinary.android.callback.UploadCallback // ✅ Fixes "Unresolved reference"

class CloudinaryService(private val mediaManager: MediaManager) {

    fun uploadImage(uri: Uri, callback: UploadCallback) {
        mediaManager.upload(uri)
            .callback(callback)
            .dispatch()
    }
}
