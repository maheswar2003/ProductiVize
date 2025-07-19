package com.productivize.domain.analytics

import com.productivize.data.model.HourLog
import com.productivize.data.model.LiveMetrics
import com.productivize.data.model.PerformanceTrend
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class LiveAnalytics @Inject constructor() {
    
    companion object {
        private const val WINDOW_SIZE = 4  // Hours for moving average
        private const val TREND_SENSITIVITY = 0.3  // Sensitivity for trend detection
    }
    
    /**
     * Calculate live performance trend using sliding window analysis
     */
    fun calculateLiveTrend(logs: List<HourLog>): PerformanceTrend {
        val ratedLogs = logs.filter { it.rating != null }
            .sortedBy { it.dateTime }
        
        if (ratedLogs.size < WINDOW_SIZE * 2) return PerformanceTrend.NEUTRAL
        
        val recentWindow = ratedLogs.takeLast(WINDOW_SIZE)
        val previousWindow = ratedLogs.dropLast(WINDOW_SIZE).takeLast(WINDOW_SIZE)
        
        val recentAvg = recentWindow.mapNotNull { it.rating }.average()
        val previousAvg = previousWindow.mapNotNull { it.rating }.average()
        
        val difference = recentAvg - previousAvg
        
        return when {
            difference > TREND_SENSITIVITY -> PerformanceTrend.UPWARD
            difference < -TREND_SENSITIVITY -> PerformanceTrend.DOWNWARD
            else -> PerformanceTrend.STABLE
        }
    }
    
    /**
     * Generate real-time performance suggestion
     */
    fun generateLiveSuggestion(
        trend: PerformanceTrend,
        currentHour: Int = LocalTime.now().hour,
        recentRatings: List<Int> = emptyList()
    ): String {
        val baseTimeAdvice = getTimeBasedAdvice(currentHour)
        val trendAdvice = getTrendAdvice(trend)
        val ratingAdvice = getRatingAdvice(recentRatings)
        
        return when {
            recentRatings.isNotEmpty() && recentRatings.average() < 2.5 -> ratingAdvice
            trend == PerformanceTrend.DOWNWARD -> trendAdvice
            currentHour in listOf(14, 15, 16) -> baseTimeAdvice
            else -> trendAdvice
        }
    }
    
    /**
     * Calculate comprehensive live metrics
     */
    fun calculateLiveMetrics(logs: List<HourLog>): LiveMetrics {
        val trend = calculateLiveTrend(logs)
        val suggestion = generateLiveSuggestion(trend)
        val momentum = calculateMomentum(logs)
        val energyLevel = calculateEnergyLevel(logs)
        
        return LiveMetrics(
            currentTrend = trend,
            suggestion = suggestion,
            momentum = momentum,
            energyLevel = energyLevel
        )
    }
    
    /**
     * Calculate momentum based on recent performance velocity
     */
    private fun calculateMomentum(logs: List<HourLog>): Float {
        val recentRatings = logs.filter { it.rating != null }
            .sortedBy { it.dateTime }
            .takeLast(6)
            .mapNotNull { it.rating }
        
        if (recentRatings.size < 3) return 0.5f
        
        // Calculate velocity (rate of change)
        val velocities = recentRatings.zipWithNext { a, b -> b - a }
        val avgVelocity = velocities.average()
        
        // Calculate acceleration (rate of change of velocity)
        val accelerations = velocities.zipWithNext { a, b -> b - a }
        val avgAcceleration = if (accelerations.isNotEmpty()) accelerations.average() else 0.0
        
        // Combine velocity and acceleration
        val momentum = (avgVelocity * 0.7 + avgAcceleration * 0.3) / 4  // Normalize to -1 to 1
        
        return (0.5f + momentum.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate current energy level based on recent performance
     */
    private fun calculateEnergyLevel(logs: List<HourLog>): Float {
        val currentHour = LocalTime.now().hour
        val recentLogs = logs.filter { it.rating != null }
            .sortedBy { it.dateTime }
            .takeLast(3)
        
        if (recentLogs.isEmpty()) return 0.5f
        
        val recentAvg = recentLogs.mapNotNull { it.rating }.average()
        val circadianFactor = getCircadianEnergyFactor(currentHour)
        
        // Combine recent performance with circadian expectations
        val baseEnergy = recentAvg / 5.0  // Normalize to 0-1
        val adjustedEnergy = baseEnergy * circadianFactor
        
        return adjustedEnergy.coerceIn(0.0, 1.0).toFloat()
    }
    
    /**
     * Get circadian energy factor for current hour
     */
    private fun getCircadianEnergyFactor(hour: Int): Double {
        return when (hour) {
            in 6..8 -> 0.8    // Morning wake-up
            in 9..11 -> 1.0   // Morning peak
            12 -> 0.7         // Lunch dip
            in 13..15 -> 0.6  // Afternoon low
            in 16..18 -> 0.9  // Evening recovery
            in 19..21 -> 0.8  // Evening moderate
            in 22..23 -> 0.5  // Night wind-down
            else -> 0.3       // Deep night
        }
    }
    
    /**
     * Get time-based advice for current hour
     */
    private fun getTimeBasedAdvice(hour: Int): String {
        return when (hour) {
            in 6..8 -> "🌅 Morning energy building - ease into focused work"
            in 9..11 -> "🧠 Peak morning hours - tackle complex challenges"
            12 -> "🍽️ Lunch break - fuel up for afternoon"
            in 13..15 -> "🚶 Afternoon dip - try movement or light tasks"
            in 16..18 -> "⚡ Second wind - capitalize on renewed energy"
            in 19..21 -> "🌆 Evening focus - wrap up or plan tomorrow"
            in 22..23 -> "🌙 Wind-down time - reflect and prepare for rest"
            else -> "😴 Night hours - prioritize rest for tomorrow"
        }
    }
    
    /**
     * Get trend-based advice
     */
    private fun getTrendAdvice(trend: PerformanceTrend): String {
        return when (trend) {
            PerformanceTrend.UPWARD -> "📈 Momentum building - maintain current strategies"
            PerformanceTrend.DOWNWARD -> "🔄 Energy dip detected - try 5-min reset break"
            PerformanceTrend.STABLE -> "➡️ Steady performance - stay hydrated and focused"
            PerformanceTrend.NEUTRAL -> "🎯 Consistent pace - small adjustments for optimization"
        }
    }
    
    /**
     * Get rating-based advice
     */
    private fun getRatingAdvice(recentRatings: List<Int>): String {
        if (recentRatings.isEmpty()) return "📊 Start tracking to get personalized suggestions"
        
        val avg = recentRatings.average()
        val trend = if (recentRatings.size >= 2) {
            recentRatings.zipWithNext { a, b -> b - a }.average()
        } else 0.0
        
        return when {
            avg < 2.0 -> "🔋 Low energy detected - try 2-min breathing exercise"
            avg < 2.5 && trend < 0 -> "⚠️ Declining performance - change environment or task"
            avg < 3.0 -> "💡 Moderate focus - small adjustment could help"
            avg >= 4.0 && trend > 0 -> "🚀 High performance - maintain current approach"
            avg >= 4.0 -> "⭐ Excellent focus - you're in the zone!"
            else -> "🎯 Steady progress - keep going"
        }
    }
    
    /**
     * Detect performance anomalies
     */
    fun detectAnomalies(logs: List<HourLog>): List<String> {
        val anomalies = mutableListOf<String>()
        val ratedLogs = logs.filter { it.rating != null }
            .sortedBy { it.dateTime }
        
        if (ratedLogs.size < 4) return anomalies
        
        // Detect sudden drops
        val recentRatings = ratedLogs.takeLast(4).mapNotNull { it.rating }
        val suddenDrop = recentRatings.zipWithNext { a, b -> a - b }.any { it >= 3 }
        if (suddenDrop) {
            anomalies.add("⚠️ Sudden performance drop detected")
        }
        
        // Detect unusual patterns
        val lastFour = ratedLogs.takeLast(4)
        val allLow = lastFour.all { (it.rating ?: 0) <= 2 }
        if (allLow) {
            anomalies.add("🔴 Extended low performance period")
        }
        
        val allHigh = lastFour.all { (it.rating ?: 0) >= 4 }
        if (allHigh) {
            anomalies.add("🟢 Extended high performance period")
        }
        
        return anomalies
    }
    
    /**
     * Generate micro-intervention based on current state
     */
    fun generateMicroIntervention(
        currentRating: Int?,
        recentRatings: List<Int>,
        currentHour: Int
    ): String {
        val currentTime = LocalTime.now()
        
        return when {
            currentRating == null -> "⏰ Time to rate your current hour"
            currentRating <= 2 -> when {
                currentTime.minute < 30 -> "🔄 Early hour reset - change your approach"
                else -> "💪 Push through or take strategic break"
            }
            currentRating == 3 -> "⚡ Good pace - small boost could elevate performance"
            currentRating >= 4 -> "🎯 Excellent! Maintain this energy level"
            recentRatings.isNotEmpty() && recentRatings.average() < 2.5 -> 
                "🔋 Consider 10-min walk or hydration break"
            currentHour in 14..16 -> "🌤️ Afternoon optimization - try changing tasks"
            else -> "📊 Steady progress - keep your momentum"
        }
    }
    
    /**
     * Calculate focus duration recommendation
     */
    fun calculateFocusDuration(recentPerformance: List<Int>): Int {
        if (recentPerformance.isEmpty()) return 25  // Default Pomodoro
        
        val avg = recentPerformance.average()
        val consistency = if (recentPerformance.size >= 2) {
            val variance = recentPerformance.map { (it - avg) * (it - avg) }.average()
            1.0 - (variance / 4.0)  // Normalize variance
        } else 0.5
        
        return when {
            avg >= 4.0 && consistency > 0.8 -> 45  // High performance + consistent
            avg >= 3.5 && consistency > 0.6 -> 35  // Good performance + moderate consistency
            avg >= 3.0 -> 25  // Standard Pomodoro
            avg >= 2.0 -> 20  // Shorter for low performance
            else -> 15  // Very short for very low performance
        }.coerceIn(15, 60)
    }
} 