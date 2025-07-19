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
fun AchievementRing(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    val transition = remember(percentage) { ProgressTransition(percentage) }
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    // Stable calculations to prevent unnecessary recompositions
    val strokeWidth = remember { 20f }
    val startAngle = remember { -90f }
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val animatedValue = transition.animatedValue()
        val color = transition.colorTransition(animatedValue)
        val sweepAngle = remember(animatedValue) { (animatedValue / 100f) * 360f }
        
        Canvas(modifier = Modifier.size(200.dp)) {
            val radius = (size.minDimension - strokeWidth) / 2
            val centerOffset = (size.width - radius * 2) / 2
            val arcSize = radius * 2

            // Background ring
            drawCircle(
                color = surfaceVariantColor,
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )
            
            // Progress ring - only draw if there's progress
            if (sweepAngle > 0) {
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(centerOffset, centerOffset),
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        
        // Percentage text with stable formatting
        val percentageText = remember(animatedValue) { "${animatedValue.toInt()}%" }
        Text(
            text = percentageText,
            style = MaterialTheme.typography.displayMedium,
            color = color
        )
    }
}