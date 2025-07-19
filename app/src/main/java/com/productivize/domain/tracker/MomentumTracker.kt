package com.productivize.domain.tracker

import com.productivize.data.model.DailySummary
import com.productivize.data.model.HourLog
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class MomentumTracker @Inject constructor() {
    
    /**
     * Streak types based on performance characteristics
     */
    enum class StreakType {
        LONGRUN,      // 10+ productive hours
        PEAK,         // High performance index
        FOCUS,        // Deep work concentration
        CONSISTENT,   // Steady performance
        RECOVERY,     // Bouncing back
        BUILDING      // Gradual improvement
    }
    
    /**
     * Streak DNA data structure
     */
    data class StreakDNA(
        val length: Int,
        val types: Set<StreakType>,
        val badge: String,
        val momentum: Float,
        val quality: Float,
        val prediction: String
    )
    
    /**
     * Calculate comprehensive streak with DNA analysis
     */
    fun calculateStreak(summaries: List<DailySummary>): StreakDNA {
        if (summaries.isEmpty()) {
            return StreakDNA(0, emptySet(), "🌱 Start your streak", 0f, 0f, "Track hours to build momentum")
        }
        
        val sortedSummaries = summaries.sortedByDescending { it.date }
        val (streakLength, streakSummaries) = findCurrentStreak(sortedSummaries)
        
        if (streakLength == 0) {
            return StreakDNA(0, emptySet(), "🌱 Ready to start", 0f, 0f, "One productive day starts the streak")
        }
        
        val streakTypes = analyzeStreakTypes(streakSummaries)
        val momentum = calculateMomentum(streakSummaries)
        val quality = calculateStreakQuality(streakSummaries)
        val badge = generateStreakBadge(streakLength, streakTypes, momentum)
        val prediction = generateStreakPrediction(streakSummaries, momentum)
        
        return StreakDNA(streakLength, streakTypes, badge, momentum, quality, prediction)
    }
    
    /**
     * Find current streak length and qualifying summaries
     */
    private fun findCurrentStreak(summaries: List<DailySummary>): Pair<Int, List<DailySummary>> {
        var streakLength = 0
        val streakSummaries = mutableListOf<DailySummary>()
        var currentDate = LocalDate.now()
        
        for (summary in summaries) {
            if (summary.date != currentDate) break
            
            // Streak qualification: 70% achievement or 6+ productive hours
            if (summary.achievementPercentage >= 70f || summary.productiveHours >= 6) {
                streakLength++
                streakSummaries.add(summary)
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streakLength to streakSummaries
    }
    
    /**
     * Analyze streak types based on performance DNA
     */
    private fun analyzeStreakTypes(summaries: List<DailySummary>): Set<StreakType> {
        val types = mutableSetOf<StreakType>()
        
        summaries.forEach { summary ->
            when {
                summary.productiveHours >= 10 -> types.add(StreakType.LONGRUN)
                summary.performanceIndex >= 1.05f -> types.add(StreakType.PEAK)
                summary.consistency >= 0.8f -> types.add(StreakType.CONSISTENT)
                summary.topTags.any { it.contains("deep") || it.contains("focus") } -> types.add(StreakType.FOCUS)
            }
        }
        
        // Check for recovery pattern
        if (summaries.size >= 3) {
            val achievements = summaries.map { it.achievementPercentage }
            val isRecovery = achievements.zipWithNext { a, b -> b > a }.count { it } >= achievements.size / 2
            if (isRecovery) types.add(StreakType.RECOVERY)
        }
        
        // Check for building pattern
        if (summaries.size >= 5) {
            val recentAvg = summaries.take(3).map { it.achievementPercentage }.average()
            val olderAvg = summaries.drop(3).map { it.achievementPercentage }.average()
            if (recentAvg > olderAvg + 5) types.add(StreakType.BUILDING)
        }
        
        return types.ifEmpty { setOf(StreakType.CONSISTENT) }
    }
    
    /**
     * Calculate momentum based on recent performance trends
     */
    private fun calculateMomentum(summaries: List<DailySummary>): Float {
        if (summaries.size < 2) return 0.5f
        
        val achievements = summaries.map { it.achievementPercentage }
        val performanceIndices = summaries.map { it.performanceIndex }
        
        // Calculate trend slope
        val achievementTrend = achievements.zipWithNext { a, b -> b - a }.average()
        val performanceTrend = performanceIndices.zipWithNext { a, b -> b - a }.average()
        
        // Combine trends with weights
        val momentum = (achievementTrend * 0.6 + performanceTrend * 40) / 100
        
        return (0.5f + momentum.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate overall streak quality
     */
    private fun calculateStreakQuality(summaries: List<DailySummary>): Float {
        if (summaries.isEmpty()) return 0f
        
        val avgAchievement = summaries.map { it.achievementPercentage }.average() / 100
        val avgPerformance = summaries.map { it.performanceIndex }.average()
        val avgConsistency = summaries.map { it.consistency }.average()
        
        return ((avgAchievement * 0.4 + avgPerformance * 0.3 + avgConsistency * 0.3).toFloat())
            .coerceIn(0f, 1f)
    }
    
    /**
     * Generate dynamic streak badge
     */
    private fun generateStreakBadge(length: Int, types: Set<StreakType>, momentum: Float): String {
        val baseEmoji = when {
            length >= 30 -> "🔥"
            length >= 14 -> "⚡"
            length >= 7 -> "🌟"
            length >= 3 -> "💪"
            else -> "✨"
        }
        
        val lengthText = when {
            length >= 30 -> "${length}-DAY LEGEND"
            length >= 14 -> "${length}-DAY FIRE"
            length >= 7 -> "${length}-DAY STREAK"
            length >= 3 -> "${length}-DAY RUN"
            else -> "${length}-DAY START"
        }
        
        val typesBadge = types.joinToString("+") { type ->
            when (type) {
                StreakType.LONGRUN -> "MARATHON"
                StreakType.PEAK -> "PEAK"
                StreakType.FOCUS -> "FOCUS"
                StreakType.CONSISTENT -> "STEADY"
                StreakType.RECOVERY -> "RECOVERY"
                StreakType.BUILDING -> "BUILDING"
            }
        }
        
        val momentumBadge = when {
            momentum > 0.8f -> "🚀"
            momentum > 0.6f -> "📈"
            momentum > 0.4f -> "➡️"
            else -> "⚠️"
        }
        
        return "$baseEmoji $lengthText $momentumBadge | $typesBadge"
    }
    
    /**
     * Generate streak prediction
     */
    private fun generateStreakPrediction(summaries: List<DailySummary>, momentum: Float): String {
        val avgAchievement = summaries.map { it.achievementPercentage }.average()
        val consistency = summaries.map { it.consistency }.average()
        
        return when {
            momentum > 0.8f && consistency > 0.8f -> "🎯 High streak continuation probability"
            momentum > 0.6f && avgAchievement > 75 -> "⚡ Strong momentum - maintain current pace"
            momentum < 0.4f -> "🔄 Focus on consistency to rebuild momentum"
            consistency < 0.6f -> "📊 Stabilize performance for streak longevity"
            else -> "🌱 Steady progress - one day at a time"
        }
    }
    
    /**
     * Calculate streak multiplier for gamification
     */
    fun calculateStreakMultiplier(streakLength: Int, types: Set<StreakType>): Float {
        val baseMultiplier = when {
            streakLength >= 30 -> 2.0f
            streakLength >= 14 -> 1.5f
            streakLength >= 7 -> 1.3f
            streakLength >= 3 -> 1.2f
            else -> 1.0f
        }
        
        val typeBonus = types.sumOf { type ->
            when (type) {
                StreakType.LONGRUN -> 0.1
                StreakType.PEAK -> 0.15
                StreakType.FOCUS -> 0.1
                StreakType.CONSISTENT -> 0.05
                StreakType.RECOVERY -> 0.2
                StreakType.BUILDING -> 0.1
            }
        }.toFloat()
        
        return (baseMultiplier + typeBonus).coerceAtMost(3.0f)
    }
    
    /**
     * Generate streak maintenance tips
     */
    fun generateMaintenanceTips(streakDNA: StreakDNA): List<String> {
        val tips = mutableListOf<String>()
        
        // Length-based tips
        when {
            streakDNA.length >= 14 -> tips.add("🛡️ Protect your streak with 80% rule - aim for progress, not perfection")
            streakDNA.length >= 7 -> tips.add("🎯 Week+ streaks thrive on routine - maintain your successful patterns")
            streakDNA.length >= 3 -> tips.add("💪 Building momentum - consistency beats intensity")
        }
        
        // Type-based tips
        streakDNA.types.forEach { type ->
            when (type) {
                StreakType.LONGRUN -> tips.add("🏃 Marathon streaks: schedule rest periods to prevent burnout")
                StreakType.PEAK -> tips.add("⚡ Peak performance: maintain energy with strategic breaks")
                StreakType.FOCUS -> tips.add("🎯 Deep work streaks: protect your focus environment")
                StreakType.CONSISTENT -> tips.add("📊 Steady streaks: small daily improvements compound")
                StreakType.RECOVERY -> tips.add("🔄 Recovery streaks: celebrate the comeback momentum")
                StreakType.BUILDING -> tips.add("📈 Building streaks: gradually increase challenge level")
            }
        }
        
        // Momentum-based tips
        when {
            streakDNA.momentum > 0.8f -> tips.add("🚀 High momentum: ride the wave but prepare for sustainability")
            streakDNA.momentum < 0.4f -> tips.add("⚠️ Low momentum: focus on micro-wins to rebuild confidence")
            else -> tips.add("⚖️ Stable momentum: maintain current approach")
        }
        
        return tips.take(3)
    }
    
    /**
     * Check if streak is at risk
     */
    fun isStreakAtRisk(recentLogs: List<HourLog>, currentStreak: Int): Boolean {
        if (currentStreak == 0) return false
        
        val today = LocalDate.now()
        val todayLogs = recentLogs.filter { it.dateTime.toLocalDate() == today }
        val ratedToday = todayLogs.count { it.rating != null }
        val productiveToday = todayLogs.count { (it.rating ?: 0) >= 3 }
        
        // Risk factors
        val lowRatingCount = ratedToday < 6
        val lowProductiveCount = productiveToday < 4
        val lateInDay = java.time.LocalTime.now().hour > 20
        
        return when {
            lateInDay && lowRatingCount && lowProductiveCount -> true
            lateInDay && productiveToday == 0 -> true
            else -> false
        }
    }
} 