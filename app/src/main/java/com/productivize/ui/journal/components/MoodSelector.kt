package com.productivize.ui.journal.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MoodSelector(currentMood: String, onMoodSelected: (String) -> Unit) {
    val moods = listOf(
        MoodData("😢", "Terrible"),
        MoodData("😞", "Bad"),
        MoodData("😐", "Okay"),
        MoodData("😊", "Good"),
        MoodData("😍", "Great"),
        MoodData("🔥", "Amazing")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How was your day?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moods) { moodData ->
                    MoodBadge(
                        mood = moodData.emoji,
                        label = moodData.label,
                        isSelected = moodData.emoji == currentMood,
                        onClick = { onMoodSelected(moodData.emoji) }
                    )
                }
            }
        }
    }
}

@Composable
fun MoodBadge(
    mood: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size by animateDpAsState(
        targetValue = if (isSelected) 80.dp else 70.dp,
        animationSpec = tween(300)
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(300)
    )
    
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .padding(4.dp)
            .size(size),
        elevation = CardDefaults.cardElevation(elevation),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mood,
                style = MaterialTheme.typography.displaySmall
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private data class MoodData(val emoji: String, val label: String) 