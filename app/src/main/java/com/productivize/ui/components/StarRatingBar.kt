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
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Row(modifier = modifier) {
        (1..5).forEach { starValue ->
            Icon(
                painter = painterResource(
                    id = if (starValue <= rating) R.drawable.ic_star_filled
                         else R.drawable.ic_star_outline
                ),
                contentDescription = "$starValue star",
                modifier = Modifier
                    .clickable { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRatingSelected(starValue) 
                    }
                    .padding(4.dp),
                tint = when {
                    rating >= starValue && rating >= 4 -> TealRating   // Teal for 4-5 stars
                    rating >= starValue && rating == 3 -> AmberRating  // Amber for 3 stars
                    rating >= starValue -> CoralRating                 // Coral for 1-2 stars
                    else -> Color.LightGray
                }
            )
        }
    }
} 