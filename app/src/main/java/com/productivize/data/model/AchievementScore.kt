package com.productivize.data.model

data class AchievementScore(
    val percentage: Float = 0f,                    // 0-1 scale
    val productiveHours: Int = 0,                  // Count of productive hours
    val performanceIndex: Float = 0f,              // Circadian-weighted performance
    val consistency: Float = 0f,                   // Consistency scoring (0-1)
    val streakCount: Int = 0,                      // Current streak length
    val streakType: String = "",                   // Type of streak (FOCUS, PEAK, etc.)
    val trend: PerformanceTrend = PerformanceTrend.NEUTRAL  // Current trend
)

data class HourRating(
    val hour: Int,
    val rating: Int?
)

data class TagFrequency(
    val tag: String,
    val count: Int
)

data class PatternInsight(
    val pattern: String,
    val timeRange: IntRange,
    val tips: List<String>,
    val confidence: Float
)

data class LiveMetrics(
    val currentTrend: PerformanceTrend,
    val suggestion: String,
    val momentum: Float,
    val energyLevel: Float
) 