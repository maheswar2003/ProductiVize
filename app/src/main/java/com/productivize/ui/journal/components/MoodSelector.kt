package com.productivize.ui.journal.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoodSelector(currentMood: String, onMoodSelected: (String) -> Unit) {
    val moods = listOf("😢", "😞", "😐", "😊", "😍", "🔥")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("How was your day?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow {
            items(moods) { mood ->
                MoodBadge(
                    mood = mood,
                    isSelected = mood == currentMood,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }
    }
}

@Composable
fun MoodBadge(mood: String, isSelected: Boolean, onClick: () -> Unit) {
    val size by animateDpAsState(targetValue = if (isSelected) 56.dp else 48.dp)
    val elevation by animateDpAsState(targetValue = if (isSelected) 8.dp else 2.dp)
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .padding(4.dp)
            .size(size),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = mood,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
} 