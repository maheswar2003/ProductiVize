package com.productivize.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.productivize.R
import com.productivize.ui.theme.AmberRating
import com.productivize.ui.theme.CoralRating
import com.productivize.ui.theme.TealRating

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val validRating = rating.coerceIn(0, 5) // Ensure rating is within valid range
    
    Row(modifier = modifier) {
        (1..5).forEach { starValue ->
            Icon(
                painter = painterResource(
                    id = if (starValue <= validRating) R.drawable.ic_star_filled
                         else R.drawable.ic_star_outline
                ),
                contentDescription = "$starValue star",
                modifier = Modifier
                    .clickable { 
                        if (vibrationEnabled) {
                            try {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (e: Exception) {
                                // Haptic feedback failed, continue without it
                            }
                        }
                        onRatingSelected(starValue)
                    }
                    .padding(4.dp),
                tint = if (starValue <= validRating) {
                    Color(0xFFFFD700) // Gold color for filled stars
                } else {
                    Color.Gray
                }
            )
        }
    }
} 