package com.productivize.logic

import com.productivize.data.repository.ProductivityRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class JournalAssistant @Inject constructor(
    private val repository: ProductivityRepository
) {
    
    suspend fun generateAutoAchievements(): String {
        return try {
            val today = LocalDate.now()
            val dailyData = repository.getDailyData(today).first()
            val dailySummary = dailyData.dailySummary
            
            when {
                dailySummary?.achievementPercentage ?: 0f >= 85f -> 
                    "🌟 Exceptional performance! You achieved ${dailySummary?.achievementPercentage?.toInt()}% productivity with ${dailySummary?.performanceGrade ?: "A"} grade."
                dailySummary?.achievementPercentage ?: 0f >= 70f -> 
                    "💪 Strong day with ${dailySummary?.achievementPercentage?.toInt()}% achievement. Your performance index is ${String.format("%.2f", dailySummary?.performanceIndex ?: 0f)}."
                dailySummary?.achievementPercentage ?: 0f >= 50f -> 
                    "📈 Good progress with ${dailySummary?.achievementPercentage?.toInt()}% achievement. Keep building momentum!"
                else -> 
                    "🎯 Every step counts. Focus on small wins and consistency for tomorrow."
            }
        } catch (e: Exception) {
            "Ready to capture today's achievements!"
        }
    }
    
    suspend fun generateMoodSuggestions(): List<String> {
        return try {
            val today = LocalDate.now()
            val insights = repository.getBasicInsightsForDate(today)
            
            val suggestions = listOf(
                "🌅 Morning Reflection: How are you feeling starting this day?",
                "⚡ Energy Check: What's your energy level right now?",
                "🎯 Focus Assessment: How clear are your priorities today?",
                "💭 Mindset: What thoughts are driving your actions?",
                "🚀 Momentum: Are you feeling motivated and ready to tackle goals?"
            )
            
            return suggestions + insights.take(2).map { "💡 $it" }
        } catch (e: Exception) {
            listOf(
                "🌅 How are you feeling today?",
                "⚡ What's your energy level?",
                "🎯 What are your main priorities?",
                "💭 Any thoughts on your mind?",
                "🚀 What's motivating you today?"
            )
        }
    }
    
    suspend fun generateProductivityTips(): List<String> {
        return try {
            val today = LocalDate.now()
            val analyticsData = repository.getAnalyticsData(today).first()
            val liveAnalytics = analyticsData.liveAnalytics
            
            val tips = mutableListOf<String>()
            
            // Add energy-based tips
            if (liveAnalytics.energyLevel < 0.5f) {
                tips.add("🔋 Low energy detected - consider a short break or light movement")
            }
            
            // Add focus-based tips
            if (liveAnalytics.focusDuration < 30) {
                tips.add("🎯 Focus opportunity - try time-blocking for deeper work")
            }
            
            // Add trend-based tips
            if (liveAnalytics.currentTrend.name == "DOWNWARD") {
                tips.add("📉 Reverse the trend - identify what's working well and amplify it")
            }
            
            // Add general productivity tips
            tips.addAll(listOf(
                "🌊 Work in flow states - align tasks with your natural energy rhythms",
                "🎨 Batch similar tasks together for better efficiency",
                "🌱 Start with small wins to build momentum",
                "💡 Use the 2-minute rule for quick tasks",
                "🏆 Celebrate progress, not just outcomes"
            ))
            
            tips.take(3)
        } catch (e: Exception) {
            listOf(
                "🌱 Start with small, manageable tasks",
                "🎯 Focus on one thing at a time",
                "🏆 Celebrate your progress today"
            )
        }
    }
    
    suspend fun generateReflectionPrompts(): List<String> {
        return try {
            val today = LocalDate.now()
            val analyticsData = repository.getAnalyticsData(today).first()
            val streakData = analyticsData.streakAnalysis
            
            val prompts = mutableListOf<String>()
            
            // Add streak-based prompts
            if (streakData.streakLength > 0) {
                prompts.add("🔥 You're on a ${streakData.streakLength}-day streak! What's driving this consistency?")
            }
            
            // Add performance-based prompts
            if (streakData.isStreakAtRisk) {
                prompts.add("⚠️ Your streak needs attention. What small step can you take tomorrow?")
            }
            
            // Add general reflection prompts
            prompts.addAll(listOf(
                "💭 What was the highlight of your day?",
                "🎯 What did you learn about yourself today?",
                "🌟 What are you most grateful for right now?",
                "🚀 What would make tomorrow even better?",
                "💡 What pattern do you notice in your productivity?"
            ))
            
            prompts.take(3)
        } catch (e: Exception) {
            listOf(
                "💭 What was the highlight of your day?",
                "🎯 What did you learn today?",
                "🌟 What are you grateful for?"
            )
        }
    }
    
    suspend fun generateWeeklyReview(): String {
        return try {
            val today = LocalDate.now()
            val analyticsData = repository.getAnalyticsData(today).first()
            val weeklyData = analyticsData.weeklyAnalytics
            
            """
            📊 Weekly Performance Review
            
            🎯 Achievement: ${String.format("%.1f", weeklyData.averageAchievement)}%
            💪 Performance Index: ${String.format("%.2f", weeklyData.averagePerformanceIndex)}
            📈 Consistency: ${String.format("%.1f", weeklyData.consistency * 100)}%
            🔥 Streak Status: ${weeklyData.streakLength} days
            
            🌟 Key Insights:
            ${weeklyData.insights.joinToString("\n") { "• $it" }}
            
            🎯 Focus Areas:
            ${weeklyData.recommendations.joinToString("\n") { "• $it" }}
            """.trimIndent()
        } catch (e: Exception) {
            "📊 Weekly review data is being processed. Check back soon!"
        }
    }
} 