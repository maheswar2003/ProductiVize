package com.productivize.ui.journal.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.productivize.utils.rememberSpeechToTextHelper

@Composable
fun VoiceInputButton(onResult: (String) -> Unit) {
    var isListening by remember { mutableStateOf(false) }
    
    val speechHelper = rememberSpeechToTextHelper { result ->
        if (result.isNotBlank()) {
            onResult(result)
        }
        isListening = false
    }

    IconButton(
        onClick = {
            isListening = true
            speechHelper.startListening()
        }
    ) {
        if (isListening) {
            CircularProgressIndicator()
        } else {
            Icon(Icons.Filled.Mic, contentDescription = "Voice input")
        }
    }
} 