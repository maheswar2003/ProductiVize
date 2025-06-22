package com.productivize.utils

import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class MediaPicker(private val caller: ActivityResultCaller, private val onResult: (Uri?) -> Unit) {
    private val launcher = caller.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onResult(uri)
    }

    fun pickImage() {
        launcher.launch("image/*")
    }
}

@Composable
fun rememberMediaPicker(onResult: (Uri?) -> Unit): MediaPicker {
    val context = LocalContext.current
    val activity = context as? ActivityResultCaller
        ?: throw IllegalStateException("Context is not an ActivityResultCaller")
    return remember { MediaPicker(activity, onResult) }
} 