package com.productivize.domain.generator

import com.productivize.data.model.HourLog
import javax.inject.Inject
import kotlin.math.roundToInt

class InsightGenerator @Inject constructor() {
    
    fun generateDailyInsights(
        ratedHours: List<HourLog>,
        achievementPercentage: Float,
        peakHours: List<Int>,
        lowHours: List<Int>,
        dailyGoalHours: Int = 8,
        achievementThreshold: Int = 3
    ): List<String> {
        val insights = mutableListOf<String>()
        
        // Goal achievement insight
        val productiveHours = ratedHours.count { (it.rating ?: 0) >= achievementThreshold }
        insights.add(generateGoalInsight(productiveHours, dailyGoalHours, achievementThreshold))
        
        // Achievement-based insight
        insights.add(generateAchievementInsight(achievementPercentage, achievementThreshold))
        
        // Peak hours insight
        if (peakHours.isNotEmpty()) {
            insights.add(generatePeakHoursInsight(peakHours))
        }
        
        // Low hours insight with suggestion
        if (lowHours.isNotEmpty()) {
            insights.add(generateLowHoursInsight(lowHours, achievementThreshold))
        }
        
        // Pattern insights
        insights.addAll(generatePatternInsights(ratedHours))
        
        // Tag-based insights
        val tagInsights = generateTagInsights(ratedHours)
        if (tagInsights.isNotEmpty()) {
            insights.addAll(tagInsights)
        }
        
        return insights.take(3) // Return top 3 insights
    }
    
    private fun generateGoalInsight(productiveHours: Int, dailyGoalHours: Int, achievementThreshold: Int): String {
        val goalPercentage = (productiveHours.toFloat() / dailyGoalHours * 100).roundToInt()
        return when {
            productiveHours >= dailyGoalHours -> "🎯 Goal achieved! You had $productiveHours productive hours (${achievementThreshold}+ stars). Excellent work!"
            productiveHours >= (dailyGoalHours * 0.8).toInt() -> "🎯 Almost there! $productiveHours/$dailyGoalHours productive hours ($goalPercentage% of goal)"
            productiveHours >= (dailyGoalHours * 0.5).toInt() -> "🎯 Halfway to your goal: $productiveHours/$dailyGoalHours productive hours"
            else -> "🎯 $productiveHours/$dailyGoalHours productive hours today. Small steps lead to big achievements!"
        }
    }
    
    private fun generateAchievementInsight(percentage: Float, threshold: Int): String {
        return when {
            percentage >= 80 -> "⭐ Outstanding! ${percentage.roundToInt()}% of your hours met the ${threshold}-star standard!"
            percentage >= 60 -> "💪 Good progress with ${percentage.roundToInt()}% of hours above ${threshold} stars!"
            percentage >= 40 -> "${percentage.roundToInt()}% achievement rate. Focus on your strengths tomorrow!"
            else -> "${percentage.roundToInt()}% achievement today. Every ${threshold}-star hour counts toward progress!"
        }
    }
    
    private fun generatePeakHoursInsight(peakHours: List<Int>): String {
        val hourRanges = formatHourRanges(peakHours)
        return "Your peak performance hours: $hourRanges. Schedule important tasks during these times! ⚡"
    }
    
    private fun generateLowHoursInsight(lowHours: List<Int>, threshold: Int): String {
        val suggestions = mapOf(
            (11..13) to "post-lunch dip → try a 10-min walk or light stretching",
            (14..16) to "afternoon slump → consider a healthy snack or brief meditation",
            (20..23) to "evening fatigue → wind down with lighter tasks or planning",
            (0..6) to "late night hours → prioritize sleep for better next-day performance"
        )
        
        val firstLowHour = lowHours.first()
        val suggestion = suggestions.entries.find { firstLowHour in it.key }?.value
            ?: "energy dip → try changing your environment or task type"
        
        return "Low ratings around ${formatHour(firstLowHour)} suggest $suggestion"
    }
    
    private fun generatePatternInsights(ratedHours: List<HourLog>): List<String> {
        val insights = mutableListOf<String>()
        
        // Check for consistent morning performance
        val morningHours = ratedHours.filter { it.hour in 6..11 }
        if (morningHours.size >= 3) {
            val avgMorningRating = morningHours.mapNotNull { it.rating }.average()
            if (avgMorningRating >= 4.0) {
                insights.add("Strong morning performance (avg ${avgMorningRating.format(1)}★). You're a morning person! 🌅")
            }
        }
        
        // Check for post-meal patterns
        val postLunchHours = ratedHours.filter { it.hour in 13..14 }
        if (postLunchHours.isNotEmpty()) {
            val avgPostLunch = postLunchHours.mapNotNull { it.rating }.average()
            if (avgPostLunch <= 2.5) {
                insights.add("Post-lunch productivity dip detected. Try lighter meals or a quick walk 🚶‍♂️")
            }
        }
        
        // Check for consistency
        val ratings = ratedHours.mapNotNull { it.rating }
        if (ratings.size >= 5) {
            val variance = ratings.map { (it - ratings.average()) * (it - ratings.average()) }.average()
            if (variance < 0.5) {
                insights.add("Very consistent performance today! Stability is a superpower 💫")
            }
        }
        
        return insights
    }
    
    private fun generateTagInsights(ratedHours: List<HourLog>): List<String> {
        val insights = mutableListOf<String>()
        
        // Group ratings by tag
        val tagRatings = mutableMapOf<String, MutableList<Int>>()
        ratedHours.forEach { hour ->
            hour.rating?.let { rating ->
                hour.tags.forEach { tag ->
                    tagRatings.getOrPut(tag) { mutableListOf() }.add(rating)
                }
            }
        }
        
        // Find best performing tag
        val tagAverages = tagRatings.mapValues { it.value.average() }
        val bestTag = tagAverages.maxByOrNull { it.value }
        
        if (bestTag != null && bestTag.value >= 4.0) {
            insights.add("${bestTag.key} activities drove your best performance (${bestTag.value.format(1)}★ avg)")
        }
        
        // Find struggling tag
        val worstTag = tagAverages.minByOrNull { it.value }
        if (worstTag != null && worstTag.value <= 2.5) {
            insights.add("${worstTag.key} tasks were challenging today. Consider different approaches or timing")
        }
        
        return insights
    }
    
    private fun formatHourRanges(hours: List<Int>): String {
        if (hours.isEmpty()) return ""
        
        val sorted = hours.sorted()
        val ranges = mutableListOf<String>()
        var start = sorted[0]
        var end = sorted[0]
        
        for (i in 1 until sorted.size) {
            if (sorted[i] == end + 1) {
                end = sorted[i]
            } else {
                ranges.add(if (start == end) formatHour(start) else "${formatHour(start)}-${formatHour(end)}")
                start = sorted[i]
                end = sorted[i]
            }
        }
        ranges.add(if (start == end) formatHour(start) else "${formatHour(start)}-${formatHour(end)}")
        
        return ranges.joinToString(", ")
    }
    
    private fun formatHour(hour: Int): String {
        return when (hour) {
            0 -> "12AM"
            in 1..11 -> "${hour}AM"
            12 -> "12PM"
            else -> "${hour - 12}PM"
        }
    }
    
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
} 