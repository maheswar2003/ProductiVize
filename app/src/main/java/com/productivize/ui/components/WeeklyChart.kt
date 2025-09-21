package com.productivize.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productivize.data.model.DailySummary
import com.productivize.ui.theme.DeepBlue
import com.productivize.ui.theme.TealRating
import androidx.compose.runtime.Composable as ComposeComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@ComposeComposable
fun WeeklyChart(
    dailySummaries: List<DailySummary>,
    modifier: Modifier = Modifier
) {
    // Memoize the recent days to avoid recalculation
    val recentDays = remember(dailySummaries) {
        dailySummaries.takeLast(7)
    }

    Column(modifier = modifier) {
        Text(
            text = "Weekly Achievement",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val density = LocalDensity.current
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawWeeklyBars(recentDays, density)
            }

            // Day labels - memoized for better performance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                recentDays.forEach { summary ->
                    val dayName = summary.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawWeeklyBars(summaries: List<DailySummary>, density: Density) {
    val recentDays = summaries.takeLast(7)
    if (recentDays.isEmpty()) return

    // Memoize calculations
    val barWidth = size.width / 7f * 0.6f
    val spacing = size.width / 7f
    val maxHeight = size.height * 0.8f

    recentDays.forEachIndexed { index, summary ->
        val barHeight = (summary.achievementPercentage / 100f) * maxHeight
        val x = index * spacing + (spacing - barWidth) / 2
        val y = size.height - barHeight - with(density) { 20.dp.toPx() }

        // Memoize bar color
        val barColor = when {
            summary.achievementPercentage >= 80 -> TealRating
            summary.achievementPercentage >= 60 -> DeepBlue
            else -> Color.LightGray
        }

        // Draw bar
        drawRect(
            color = barColor,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight)
        )

        // Draw percentage text
        // Note: Text drawing requires native canvas access which is more complex in Compose
        // For now, we'll skip the percentage labels on bars
    }
} 