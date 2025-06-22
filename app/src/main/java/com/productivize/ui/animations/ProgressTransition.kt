package com.productivize.ui.animations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

class ProgressTransition(private val targetValue: Float) {
    @Composable
    fun animatedValue(): Float {
        val value by animateFloatAsState(
            targetValue = this.targetValue,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            ),
            label = "progressAnimation"
        )
        return value
    }

    @Composable
    fun colorTransition(animatedValue: Float): Color {
        val color by animateColorAsState(
            when {
                animatedValue > 80 -> Color(0xFF2EC4B6)
                animatedValue > 50 -> Color(0xFFFFBF69)
                else -> Color(0xFFFF6B6B)
            },
            animationSpec = tween(800),
            label = "colorTransition"
        )
        return color
    }
} 