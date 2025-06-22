package com.productivize.ui.journal.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SmartTextFieldSection(
    title: String,
    text: String,
    onTextChange: (String) -> Unit,
    suggestions: List<String> = emptyList()
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = text,
            onValueChange = {
                onTextChange(it)
                showSuggestions = it.isBlank()
            },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                VoiceInputButton { result ->
                    onTextChange(text + " " + result)
                }
            }
        )

        AnimatedVisibility(showSuggestions && suggestions.isNotEmpty()) {
            Column {
                Text("Suggestions:", style = MaterialTheme.typography.labelMedium)
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = {
                            onTextChange(suggestion)
                            showSuggestions = false
                        },
                        label = { Text(suggestion) }
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(onClick: () -> Unit, label: @Composable () -> Unit) {
    androidx.compose.material3.AssistChip(onClick = onClick, label = label)
} 