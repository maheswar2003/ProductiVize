package com.productivize.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val validRating = rating.coerceIn(0, 5)
    
    // Stable references for better performance
    val starTints = remember(validRating) {
        (1..5).map { starValue ->
            when {
                starValue <= validRating && validRating >= 4 -> TealRating
                starValue <= validRating && validRating == 3 -> AmberRating
                starValue <= validRating -> CoralRating
                else -> Color.Gray
            }
        }
    }
    
    val starIcons = remember(validRating) {
        (1..5).map { starValue ->
            if (starValue <= validRating) R.drawable.ic_star_filled
            else R.drawable.ic_star_outline
        }
    }
    
    Row(modifier = modifier) {
        repeat(5) { index ->
            val starValue = index + 1
            
            Icon(
                painter = painterResource(id = starIcons[index]),
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
                tint = starTints[index]
            )
        }
    }
} 