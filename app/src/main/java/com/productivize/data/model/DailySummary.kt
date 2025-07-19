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
    val peakHours: List<Int> = emptyList(), // Hours with high ratings
    val lowHours: List<Int> = emptyList(), // Hours with low ratings
    val topTags: List<String> = emptyList(), // Most used tags
    val insights: List<String> = emptyList(), // Generated insights
    val journalEntry: String? = null,
    val wins: List<String> = emptyList(), // Pre-filled from high ratings
    val challenges: List<String> = emptyList(), // Pre-filled from low ratings
    val createdAt: Long = System.currentTimeMillis(),
    // New advanced metrics
    val performanceIndex: Float = 0f,        // Circadian-weighted performance
    val performanceGrade: String = "C",      // Performance grade (A+ to F)
    val consistency: Float = 0f,             // Consistency scoring (0-1)
    val consistencyRating: String = "Variable", // Consistency rating
    val streakCount: Int = 0,                // Current streak length
    val streakType: String = "",             // Type of streak
    val adaptiveThreshold: Int = 3,          // Dynamic threshold used
    val productiveHours: Int = 0,            // Count of productive hours
    val momentum: Float = 0f,                // Performance momentum
    val momentumLevel: String = "Neutral",   // Momentum level (High, Medium, Low, Neutral)
    val energyPattern: String = "BALANCED",  // Detected energy pattern
    val predictiveInsight: String = ""       // Tomorrow's prediction
) {
    val productivityLevel: String
        get() = when {
            achievementPercentage >= 90 -> "Exceptional"
            achievementPercentage >= 80 -> "Excellent"
            achievementPercentage >= 70 -> "Great"
            achievementPercentage >= 60 -> "Good"
            achievementPercentage >= 50 -> "Fair"
            else -> "Building"
        }
    
} 