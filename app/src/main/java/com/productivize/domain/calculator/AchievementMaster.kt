package com.productivize.domain.calculator

import com.productivize.data.model.AchievementScore
import com.productivize.data.model.DailySummary
import com.productivize.data.model.HourLog
import com.productivize.data.model.PerformanceTrend
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@Singleton
class AchievementMaster @Inject constructor() {
    
    /**
     * Self-optimizing threshold based on historical performance
     * Adaptive Intelligence Engine
     */
    fun calculateDynamicThreshold(history: List<DailySummary>): Int {
        if (history.isEmpty()) return 3
        
        val weeklyAvg = history.takeLast(7).map { it.achievementPercentage }.average()
        val consistencyAvg = history.takeLast(7).map { it.consistency }.average()
        
        return when {
            weeklyAvg > 85 && consistencyAvg > 0.8 -> 4  // Raise standard for high performers
            weeklyAvg < 60 -> 2                         // Lower during struggle periods
            weeklyAvg < 45 -> 1                         // Emergency support mode
            else -> 3                                   // Standard threshold
        }.also { threshold ->
            // Log threshold change for analytics
            println("🧠 Dynamic threshold adjusted to: $threshold (Weekly avg: ${weeklyAvg.roundToInt()}%)")
        }
    }
    
    /**
     * Primary metric: Threshold-based achievement
     */
    fun calculateCoreAchievement(logs: List<HourLog>, threshold: Int): AchievementScore {
        if (logs.isEmpty()) return AchievementScore()
        
        val ratedLogs = logs.filter { it.rating != null }
        if (ratedLogs.isEmpty()) return AchievementScore()
        
        val productiveHours = ratedLogs.count { (it.rating ?: 0) >= threshold }
        val percentage = productiveHours.toFloat() / ratedLogs.size
        
        val performanceIndex = calculatePerformanceIndex(ratedLogs, threshold)
        val consistency = calculateConsistency(ratedLogs)
        
        return AchievementScore(
            percentage = percentage,
            productiveHours = productiveHours,
            performanceIndex = performanceIndex,
            consistency = consistency
        )
    }
    
    /**
     * Secondary metric: Weighted performance index with circadian rhythms
     */
    fun calculatePerformanceIndex(logs: List<HourLog>, threshold: Int): Float {
        if (logs.isEmpty()) return 0f
        
        val circadianWeights = getCircadianWeights()
        
        val weightedSum = logs.sumOf { log ->
            val hourWeight = circadianWeights[log.hour] ?: 1f
            val ratingBonus = if ((log.rating ?: 0) >= threshold) 1.2f else 0.8f
            val ratingValue = (log.rating ?: 0).toFloat() / 5f // Normalize to 0-1
            
            (hourWeight * ratingBonus * ratingValue).toDouble()
        }
        
        val totalWeight = logs.size.toFloat()
        return (weightedSum / totalWeight).toFloat()
    }
    
    /**
     * Tertiary metric: Consistency scoring
     */
    fun calculateConsistency(logs: List<HourLog>): Float {
        val ratings = logs.mapNotNull { it.rating }
        if (ratings.size < 2) return 0f
        
        val mean = ratings.average()
        val variance = ratings.sumOf { (it - mean).pow(2) } / ratings.size
        
        // Convert variance to consistency score (0-1 scale, 1 = perfect consistency)
        return max(0f, 1f - (variance.toFloat() / 4f))
    }
    
    /**
     * Calculate performance trend over time
     */
    fun calculateTrend(recentLogs: List<HourLog>, windowSize: Int = 4): PerformanceTrend {
        val ratedLogs = recentLogs.mapNotNull { log -> log.rating?.let { log.hour to it } }
        if (ratedLogs.size < windowSize * 2) return PerformanceTrend.NEUTRAL
        
        val recentAvg = ratedLogs.takeLast(windowSize).map { it.second }.average()
        val previousAvg = ratedLogs.take(ratedLogs.size - windowSize)
            .takeLast(windowSize).map { it.second }.average()
        
        return when {
            recentAvg > previousAvg + 0.5 -> PerformanceTrend.UPWARD
            recentAvg < previousAvg - 0.5 -> PerformanceTrend.DOWNWARD
            else -> PerformanceTrend.STABLE
        }
    }
    
    /**
     * Calculate momentum based on recent performance
     */
    fun calculateMomentum(logs: List<HourLog>, threshold: Int): Float {
        if (logs.size < 3) return 0f
        
        val ratings = logs.mapNotNull { it.rating }
        val recentRatings = ratings.takeLast(3)
        val momentum = recentRatings.zipWithNext { a, b -> 
            when {
                b > a && b >= threshold -> 0.3f
                b < a && a >= threshold -> -0.3f
                b >= threshold -> 0.1f
                else -> -0.1f
            }
        }.sum()
        
        return momentum.coerceIn(-1f, 1f)
    }
    
    /**
     * Detect energy patterns throughout the day
     */
    fun detectEnergyPattern(logs: List<HourLog>): String {
        val hourlyRatings = logs.mapNotNull { log -> 
            log.rating?.let { log.hour to it } 
        }.groupBy { it.first }
        
        val morningAvg = hourlyRatings.filterKeys { it in 6..11 }
            .values.flatten().map { it.second }.average()
        val afternoonAvg = hourlyRatings.filterKeys { it in 12..17 }
            .values.flatten().map { it.second }.average()
        val eveningAvg = hourlyRatings.filterKeys { it in 18..23 }
            .values.flatten().map { it.second }.average()
        
        return when {
            morningAvg > afternoonAvg && morningAvg > eveningAvg -> "MORNING_LARK"
            eveningAvg > morningAvg && eveningAvg > afternoonAvg -> "NIGHT_OWL"
            afternoonAvg > morningAvg && afternoonAvg > eveningAvg -> "AFTERNOON_PEAK"
            else -> "BALANCED"
        }
    }
    
    /**
     * Circadian rhythm weights for different hours
     */
    private fun getCircadianWeights(): Map<Int, Float> {
        return mapOf(
            // Early morning (5-8 AM): Medium-high weight
            5 to 0.9f, 6 to 1.0f, 7 to 1.1f, 8 to 1.2f,
            // Morning peak (9-12 PM): High weight
            9 to 1.3f, 10 to 1.3f, 11 to 1.2f,
            // Lunch hour: Lower weight
            12 to 0.8f,
            // Afternoon (1-5 PM): Variable weight
            13 to 0.9f, 14 to 0.8f, 15 to 0.9f, 16 to 1.0f, 17 to 1.1f,
            // Evening (6-9 PM): Medium weight
            18 to 1.0f, 19 to 0.9f, 20 to 0.8f, 21 to 0.7f,
            // Night (10 PM - 4 AM): Low weight
            22 to 0.6f, 23 to 0.5f, 0 to 0.4f, 1 to 0.3f, 2 to 0.3f, 3 to 0.3f, 4 to 0.4f
        )
    }
} 