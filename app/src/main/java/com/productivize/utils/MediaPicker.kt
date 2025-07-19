package com.productivize.utils

import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class MediaPicker(private val onResult: (Uri?) -> Unit) {
    private var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null
    private var isRegistered = false

    fun registerLauncher(caller: ActivityResultCaller) {
        try {
            if (launcher == null && !isRegistered) {
                launcher = caller.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    onResult(uri)
                }
                isRegistered = true
            }
        } catch (e: Exception) {
            // Handle registration failure gracefully
            println("Failed to register launcher: ${e.message}")
            onResult(null)
        }
    }

    fun pickImage() {
        try {
            if (launcher != null && isRegistered) {
                launcher?.launch("image/*")
            } else {
                println("Launcher not registered yet")
                onResult(null)
            }
        } catch (e: Exception) {
            // Handle launch failure gracefully
            println("Failed to launch image picker: ${e.message}")
            onResult(null)
        }
    }
}

@Composable
fun rememberMediaPicker(onResult: (Uri?) -> Unit): MediaPicker {
    return remember { MediaPicker(onResult) }
} 