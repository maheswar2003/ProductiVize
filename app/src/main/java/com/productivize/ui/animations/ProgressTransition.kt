package com.productivize.ui.animations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

class ProgressTransition(private val targetValue: Float) {

    // Memoized color calculation to avoid repeated computation
    private val targetColor = when {
        targetValue > 80 -> Color(0xFF2EC4B6)
        targetValue > 50 -> Color(0xFFFFBF69)
        else -> Color(0xFFFF6B6B)
    }

    @Composable
    fun animatedValue(): Float {
        val value by animateFloatAsState(
            targetValue = this.targetValue,
            animationSpec = spring(
                dampingRatio = 0.8f,  // Slightly higher damping for smoother animation
                stiffness = 200f      // Lower stiffness for less oscillation
            ),
            label = "progressAnimation"
        )
        return value
    }

    @Composable
    fun colorTransition(animatedValue: Float): Color {
        val color by animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(300),  // Faster color transition
            label = "colorTransition"
        )
        return color
    }
} 