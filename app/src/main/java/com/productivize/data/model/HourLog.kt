package com.productivize.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "hour_logs")
data class HourLog(
    @PrimaryKey
    val id: String = "", // Format: "yyyy-MM-dd-HH"
    val dateTime: LocalDateTime,
    val hour: Int, // 0-23
    val rating: Int? = null, // 1-5 stars, null if not rated
    val tags: List<String> = emptyList(), // #work, #study, #health, #social
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isRated: Boolean
        get() = rating != null
    
    val ratingColor: String
        get() = when (rating) {
            4, 5 -> "#2EC4B6" // Teal
            3 -> "#FFBF69" // Amber
            1, 2 -> "#FF6B6B" // Coral
            else -> "#E0E0E0" // Gray for unrated
        }
} 