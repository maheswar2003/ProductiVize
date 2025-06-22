package com.productivize.ui.journal.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AutoContentSection(achievements: String, patterns: String) {
    if (achievements.isNotBlank() || patterns.isNotBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (achievements.isNotBlank()) {
                    Text("Auto Achievements:", style = MaterialTheme.typography.titleSmall)
                    Text(achievements, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                if (patterns.isNotBlank()) {
                    Text("Detected Patterns:", style = MaterialTheme.typography.titleSmall)
                    Text(patterns, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
} 