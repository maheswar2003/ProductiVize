package com.productivize.ui.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PulsatingCircle(modifier: Modifier = Modifier, color: Color = Color(0xFF6200EE)) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            // Use more efficient animation spec with better easing
            scale.animateTo(1.8f, animationSpec = tween(800, easing = androidx.compose.animation.core.EaseInOut))
            delay(200) // Small delay between animations for smoother performance
            scale.animateTo(1f, animationSpec = tween(800, easing = androidx.compose.animation.core.EaseInOut))
        }
    }

    Canvas(modifier = modifier) {
        // Memoize radius calculation
        val radius = (size.minDimension / 2) * scale.value
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius
        )
    }
} 