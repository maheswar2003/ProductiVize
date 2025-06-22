package com.productivize.utils

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class SpeechToTextHelper(private val caller: ActivityResultCaller, private val onResult: (String) -> Unit) {
    private val launcher = caller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        onResult(matches?.firstOrNull() ?: "")
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        launcher.launch(intent)
    }
}

@Composable
fun rememberSpeechToTextHelper(onResult: (String) -> Unit): SpeechToTextHelper {
    val context = LocalContext.current
    val activity = context as? ActivityResultCaller
        ?: throw IllegalStateException("Context is not an ActivityResultCaller")
    return remember { SpeechToTextHelper(activity, onResult) }
} 