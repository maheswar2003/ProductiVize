package com.productivize.logic

import com.productivize.data.model.JournalEntry
import com.productivize.data.repository.ProductivityRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalAssistant @Inject constructor(
    private val repository: ProductivityRepository
) {
    
    suspend fun generateAutoAchievements(): String {
        return try {
            val today = LocalDate.now()
            val dailySummary = repository.getDailySummary(today).first()
            
            when {
                dailySummary?.achievementPercentage ?: 0f >= 80f -> 
                    "🌟 Outstanding day! You achieved ${dailySummary?.achievementPercentage?.toInt()}% productivity."
                dailySummary?.achievementPercentage ?: 0f >= 60f -> 
                    "💪 Good progress with ${dailySummary?.achievementPercentage?.toInt()}% achievement today."
                else -> 
                    "🎯 Every step counts. Focus on small wins for tomorrow."
            }
        } catch (e: Exception) {
            "Ready to capture today's achievements!"
        }
    }
    
    suspend fun detectPatterns(): String {
        return try {
            val today = LocalDate.now()
            val recentLogs = repository.getHourLogsForDate(today).first()
            
            val morningHours = recentLogs.filter { it.hour in 6..11 }
            val eveningHours = recentLogs.filter { it.hour in 18..22 }
            
            when {
                morningHours.mapNotNull { it.rating }.average() > 3.5 -> 
                    "🌅 You're a morning person! Your best work happens early."
                eveningHours.mapNotNull { it.rating }.average() > 3.5 -> 
                    "🌙 Evening productivity detected. You thrive in later hours."
                else -> 
                    "📊 Building your productivity patterns day by day."
            }
        } catch (e: Exception) {
            "Tracking your unique productivity patterns..."
        }
    }

    suspend fun generateSuggestions(entry: JournalEntry): List<String> {
        return listOf(
            "What made you feel most accomplished today?",
            "How did you overcome challenges?",
            "What would make tomorrow even better?"
        )
    }

    fun analyzeSentiment(text: String): MoodType {
        return when {
            containsPositiveKeywords(text) -> MoodType.POSITIVE
            containsNegativeKeywords(text) -> MoodType.NEGATIVE
            else -> MoodType.NEUTRAL
        }
    }

    private fun containsPositiveKeywords(text: String) =
        listOf("great", "happy", "win", "success", "progress", "achieved", "accomplished").any { 
            it in text.lowercase() 
        }
        
    private fun containsNegativeKeywords(text: String) =
        listOf("tired", "bad", "challenge", "stress", "fail", "difficult", "struggled").any { 
            it in text.lowercase() 
        }
}

enum class MoodType { POSITIVE, NEGATIVE, NEUTRAL } 