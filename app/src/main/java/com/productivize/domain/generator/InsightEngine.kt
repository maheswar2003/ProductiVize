package com.productivize.domain.generator

import com.productivize.data.model.AchievementScore
import com.productivize.data.model.DailySummary
import com.productivize.data.model.HourLog
import com.productivize.data.model.PatternInsight
import com.productivize.data.model.PerformanceTrend
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

@Singleton
class InsightEngine @Inject constructor() {
    
    /**
     * Neural pattern database - AI-powered pattern recognition
     */
    private val neuralPatterns = mapOf(
        "morning_peak" to PatternInsight(
            pattern = "morning_peak",
            timeRange = 6..11,
            tips = listOf(
                "🌅 Schedule complex tasks before noon",
                "⚡ Leverage morning energy for deep work",
                "🧠 Brain performance peaks in morning hours",
                "📚 Perfect time for learning and analysis"
            ),
            confidence = 0.85f
        ),
        "afternoon_dip" to PatternInsight(
            pattern = "afternoon_dip", 
            timeRange = 13..16,
            tips = listOf(
                "🚶 Take a 10-minute energy walk",
                "💡 Light exercise boosts afternoon focus",
                "🥗 Avoid heavy meals during lunch",
                "☕ Strategic caffeine timing helps"
            ),
            confidence = 0.90f
        ),
        "evening_rebound" to PatternInsight(
            pattern = "evening_rebound",
            timeRange = 17..20,
            tips = listOf(
                "🎨 Creative work window detected",
                "📞 Ideal time for social connections",
                "🔄 Second wind optimization period",
                "💭 Reflective tasks work best now"
            ),
            confidence = 0.75f
        ),
        "night_owl" to PatternInsight(
            pattern = "night_owl",
            timeRange = 21..23,
            tips = listOf(
                "🌙 Deep focus time for night productivity",
                "📝 Planning tomorrow works well",
                "🔍 Detail-oriented tasks excel",
                "🎯 Strategic thinking peak hours"
            ),
            confidence = 0.70f
        ),
        "consistency_master" to PatternInsight(
            pattern = "consistency_master",
            timeRange = 0..23,
            tips = listOf(
                "📊 Remarkable stability today",
                "🎯 Consistency is your superpower",
                "⚖️ Balanced energy distribution",
                "🏆 Steady performance excellence"
            ),
            confidence = 0.95f
        )
    )
    
    /**
     * Generate comprehensive insights using AI analysis
     */
    fun generateInsights(
        logs: List<HourLog>, 
        scores: AchievementScore,
        history: List<DailySummary> = emptyList()
    ): List<String> {
        val insights = mutableListOf<String>()
        
        // Pattern detection (neural network simulation)
        val detectedPatterns = detectPatterns(logs)
        detectedPatterns.forEach { pattern ->
            insights.add("🔥 ${pattern.pattern.replace("_", " ").capitalize()} pattern detected")
            insights.add(pattern.tips.random())
        }
        
        // Performance insights based on tri-metric scoring
        insights.addAll(generatePerformanceInsights(scores))
        
        // Consistency insights
        insights.addAll(generateConsistencyInsights(scores.consistency))
        
        // Trend-based insights
        insights.addAll(generateTrendInsights(scores.trend))
        
        // Predictive suggestions
        insights.addAll(generatePredictiveInsights(logs, history))
        
        // Momentum insights
        insights.addAll(generateMomentumInsights(logs))
        
        return insights.distinct().take(4)  // Max 4 high-impact insights
    }
    
    /**
     * Neural pattern detection algorithm
     */
    private fun detectPatterns(logs: List<HourLog>): List<PatternInsight> {
        val detectedPatterns = mutableListOf<PatternInsight>()
        val hourlyRatings = logs.mapNotNull { log -> 
            log.rating?.let { log.hour to it } 
        }.groupBy { it.first }
        
        neuralPatterns.forEach { (patternName, pattern) ->
            val patternHours = hourlyRatings.filterKeys { it in pattern.timeRange }
            if (patternHours.isNotEmpty()) {
                val avgRating = patternHours.values.flatten().map { it.second }.average()
                val confidence = calculatePatternConfidence(avgRating, pattern.timeRange, logs)
                
                if (confidence > 0.6f) {
                    detectedPatterns.add(pattern.copy(confidence = confidence))
                }
            }
        }
        
        return detectedPatterns.sortedByDescending { it.confidence }
    }
    
    /**
     * Calculate pattern confidence using AI heuristics
     */
    private fun calculatePatternConfidence(
        avgRating: Double, 
        timeRange: IntRange, 
        logs: List<HourLog>
    ): Float {
        val rangeSize = timeRange.last - timeRange.first + 1
        val actualHours = logs.count { it.hour in timeRange && it.rating != null }
        val coverage = actualHours.toFloat() / rangeSize
        val ratingStrength = (avgRating / 5.0).toFloat()
        
        return (coverage * 0.6f + ratingStrength * 0.4f).coerceIn(0f, 1f)
    }
    
    /**
     * Generate performance insights based on tri-metric scoring
     */
    private fun generatePerformanceInsights(scores: AchievementScore): List<String> {
        val insights = mutableListOf<String>()
        
        when {
            scores.performanceIndex > 1.2f -> insights.add("🚀 Exceptional circadian performance today!")
            scores.performanceIndex > 1.05f -> insights.add("⚡ Above-average energy optimization")
            scores.performanceIndex < 0.8f -> insights.add("🔋 Energy levels need attention")
        }
        
        when {
            scores.productiveHours > 12 -> insights.add("🏆 Marathon achievement unlocked - ${scores.productiveHours}h productive!")
            scores.productiveHours > 8 -> insights.add("💪 Strong daily performance - ${scores.productiveHours}h productive")
            scores.productiveHours > 4 -> insights.add("🌱 Solid foundation - ${scores.productiveHours}h productive")
            else -> insights.add("🎯 Building momentum - every hour counts")
        }
        
        return insights
    }
    
    /**
     * Generate consistency insights
     */
    private fun generateConsistencyInsights(consistency: Float): List<String> {
        return when {
            consistency > 0.9f -> listOf("📊 Remarkable stability - consistency master!")
            consistency > 0.8f -> listOf("⚖️ Very consistent performance today")
            consistency > 0.7f -> listOf("🎯 Good consistency patterns emerging")
            consistency > 0.6f -> listOf("📈 Moderate consistency - room for improvement")
            else -> listOf("🔄 Variable performance - focus on steady rhythm")
        }
    }
    
    /**
     * Generate trend-based insights
     */
    private fun generateTrendInsights(trend: PerformanceTrend): List<String> {
        return when (trend) {
            PerformanceTrend.UPWARD -> listOf("📈 Upward trend detected - momentum building!")
            PerformanceTrend.DOWNWARD -> listOf("📉 Downward trend - time for energy reset")
            PerformanceTrend.STABLE -> listOf("➡️ Stable performance - maintain current approach")
            PerformanceTrend.NEUTRAL -> listOf("🔄 Neutral trend - opportunity for optimization")
        }
    }
    
    /**
     * Generate predictive suggestions based on historical patterns
     */
    private fun generatePredictiveInsights(logs: List<HourLog>, history: List<DailySummary>): List<String> {
        val insights = mutableListOf<String>()
        
        // Analyze low-performance periods
        val lowPeriods = logs.filter { it.rating in 1..2 }.groupBy { it.hour / 4 }
        if (lowPeriods.isNotEmpty()) {
            val worstPeriod = lowPeriods.maxByOrNull { it.value.size }?.key
            insights.add("🔮 Tomorrow: Boost ${when(worstPeriod) {
                0 -> "early morning hours (6-11AM)"
                1 -> "afternoon focus (12-5PM)" 
                2 -> "evening energy (6-11PM)"
                else -> "late night periods"
            }} with strategic breaks")
        }
        
        // Historical pattern analysis
        if (history.isNotEmpty()) {
            val recentTrend = history.takeLast(3).map { it.achievementPercentage }.average()
            val previousTrend = history.dropLast(3).takeLast(3).map { it.achievementPercentage }.average()
            
            if (recentTrend > previousTrend + 10) {
                insights.add("🌟 Performance trending upward - maintain current strategies")
            } else if (recentTrend < previousTrend - 10) {
                insights.add("⚠️ Performance dip detected - consider strategy adjustment")
            }
        }
        
        return insights
    }
    
    /**
     * Generate momentum-based insights
     */
    private fun generateMomentumInsights(logs: List<HourLog>): List<String> {
        val insights = mutableListOf<String>()
        val recentRatings = logs.mapNotNull { it.rating }.takeLast(3)
        
        if (recentRatings.size >= 3) {
            val momentum = recentRatings.zipWithNext { a, b -> b - a }.sum()
            when {
                momentum > 2 -> insights.add("🚀 Strong positive momentum - ride the wave!")
                momentum < -2 -> insights.add("🔄 Momentum dip - time for energy intervention")
                else -> insights.add("⚖️ Steady momentum - consistent performance")
            }
        }
        
        return insights
    }
    
    /**
     * Generate contextual micro-interventions
     */
    fun generateMicroIntervention(currentHour: Int, recentRatings: List<Int>): String {
        val timeOfDay = when (currentHour) {
            in 6..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..23 -> "evening"
            else -> "night"
        }
        
        val recentAvg = recentRatings.average()
        
        return when {
            recentAvg < 2.5 -> "🔋 Energy low - try 2min breathing exercise"
            recentAvg < 3.5 -> "💡 Focus dip - change environment or task"
            currentHour == 14 -> "🚶 Post-lunch walk recommended"
            currentHour in 15..16 -> "☕ Strategic caffeine timing"
            else -> "🎯 Maintain current focus approach"
        }
    }
    
    /**
     * String extension for capitalization
     */
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
} 