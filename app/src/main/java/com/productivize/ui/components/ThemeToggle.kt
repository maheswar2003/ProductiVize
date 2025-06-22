package com.productivize.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemeToggle(isDarkMode: Boolean, onThemeChanged: (Boolean) -> Unit) {
    val transition = updateTransition(targetState = isDarkMode, label = "theme")
    val thumbOffset by animateDpAsState(targetValue = if (isDarkMode) 24.dp else 0.dp, label = "thumb")
    val trackColor by transition.animateColor(label = "track") { dark ->
        if (dark) MaterialTheme.colorScheme.primary else Color.LightGray
    }
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable { onThemeChanged(!isDarkMode) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
} 