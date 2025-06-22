package com.productivize.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.productivize.ui.animations.ProgressTransition

@Composable
fun AchievementRing(percentage: Float) {
    val transition = remember(percentage) { ProgressTransition(percentage) }
    // Get the color outside the Canvas
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        val animatedValue = transition.animatedValue()
        val color = transition.colorTransition(animatedValue)
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 20f
            val radius = (size.minDimension - strokeWidth) / 2
            val startAngle = -90f
            val sweepAngle = (animatedValue / 100f) * 360f

            // Background ring
            drawCircle(
                color = surfaceVariantColor, // Use the retrieved color
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )
            // Progress ring
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
        Text(
            text = "${animatedValue.toInt()}%",
            style = MaterialTheme.typography.displayMedium,
            color = color
        )
        // TODO: Add Lottie/Motion Compose animation for celebration if animatedValue >= 100
    }
}