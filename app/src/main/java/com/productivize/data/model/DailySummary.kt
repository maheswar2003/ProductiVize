package com.productivize.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey
    val date: LocalDate,
    val totalHoursRated: Int = 0,
    val achievementPercentage: Float = 0f, // 0-100%
    val averageRating: Float = 0f, // Average of all rated hours
    val peakHours: List<Int> = emptyList(), // Hours with 4-5 star ratings
    val lowHours: List<Int> = emptyList(), // Hours with 1-2 star ratings
    val topTags: List<String> = emptyList(), // Most used tags
    val insights: List<String> = emptyList(), // Generated insights
    val journalEntry: String? = null,
    val wins: List<String> = emptyList(), // Pre-filled from high ratings
    val challenges: List<String> = emptyList(), // Pre-filled from low ratings
    val createdAt: Long = System.currentTimeMillis()
) {
    val productivityLevel: String
        get() = when {
            achievementPercentage >= 80 -> "Excellent"
            achievementPercentage >= 60 -> "Good"
            achievementPercentage >= 40 -> "Fair"
            else -> "Needs Improvement"
        }
} 