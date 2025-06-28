package com.productivize.data.repository

import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.dao.SettingsDao
import com.productivize.data.model.HourLog
import com.productivize.data.model.DailySummary
import com.productivize.domain.calculator.AchievementCalculator
import com.productivize.domain.generator.InsightGenerator
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductivityRepository @Inject constructor(
    private val hourLogDao: HourLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val settingsDao: SettingsDao,
    private val achievementCalculator: AchievementCalculator,
    private val insightGenerator: InsightGenerator
) {
    
    // HourLog operations
    suspend fun saveHourRating(hour: Int, rating: Int, tags: List<String> = emptyList(), notes: String? = null) {
        val now = LocalDateTime.now().withHour(hour).withMinute(0).withSecond(0).withNano(0)
        val id = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"))
        
        val hourLog = HourLog(
            id = id,
            dateTime = now,
            hour = hour,
            rating = rating,
            tags = tags,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        
        hourLogDao.insert(hourLog)
        updateDailySummary(now.toLocalDate())
    }
    
    suspend fun saveHourRatingForDate(date: LocalDate, hour: Int, rating: Int, tags: List<String> = emptyList(), notes: String? = null) {
        val dateTime = date.atTime(hour, 0, 0, 0)
        val id = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"))
        
        val hourLog = HourLog(
            id = id,
            dateTime = dateTime,
            hour = hour,
            rating = rating,
            tags = tags,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        
        hourLogDao.insert(hourLog)
        updateDailySummary(date)
    }
    
    fun getHourLogsForDate(date: LocalDate): Flow<List<HourLog>> {
        return hourLogDao.getHourLogsForDate(date.toString())
    }
    
    // DailySummary operations
    private suspend fun updateDailySummary(date: LocalDate) {
        val dateStr = date.toString()
        val ratedHours = hourLogDao.getRatedHoursForDate(dateStr)
        
        if (ratedHours.isEmpty()) return
        
        // Get user settings for personalized calculations
        val settings = settingsDao.getSettings() ?: com.productivize.data.model.Settings()
        val achievementThreshold = settings.achievementThreshold
        
        val achievementPercentage = achievementCalculator.calculateAchievementPercentage(
            ratedHours, 
            achievementThreshold
        )
        val averageRating = hourLogDao.getAverageRatingForDate(dateStr) ?: 0f
        
        // Use achievement threshold for peak/low hours
        val peakHours = ratedHours.filter { (it.rating ?: 0) >= achievementThreshold }.map { it.hour }
        val lowHours = ratedHours.filter { (it.rating ?: 0) < achievementThreshold }.map { it.hour }
        
        // Extract top tags
        val allTags = ratedHours.flatMap { it.tags }
        val topTags = allTags.groupingBy { it }.eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
        
        // Generate insights with user's goal
        val insights = insightGenerator.generateDailyInsights(
            ratedHours,
            achievementPercentage,
            peakHours,
            lowHours,
            settings.dailyGoalHours,
            achievementThreshold
        )
        
        // Pre-fill wins and challenges using achievement threshold
        val wins = ratedHours.filter { (it.rating ?: 0) >= achievementThreshold }
            .mapNotNull { it.notes?.takeIf { notes -> notes.isNotBlank() } }
            .take(3)
        
        val challenges = ratedHours.filter { (it.rating ?: 0) < achievementThreshold }
            .mapNotNull { it.notes?.takeIf { notes -> notes.isNotBlank() } }
            .take(3)
        
        val summary = DailySummary(
            date = date,
            totalHoursRated = ratedHours.size,
            achievementPercentage = achievementPercentage,
            averageRating = averageRating,
            peakHours = peakHours,
            lowHours = lowHours,
            topTags = topTags,
            insights = insights,
            wins = wins,
            challenges = challenges
        )
        
        dailySummaryDao.insert(summary)
    }
    
    fun getDailySummary(date: LocalDate): Flow<DailySummary?> {
        return dailySummaryDao.observeSummaryForDate(date)
    }
    
    suspend fun getDailySummaryOnce(date: LocalDate): DailySummary? {
        return dailySummaryDao.getSummaryForDate(date)
    }
    
    suspend fun recalculateDailySummary(date: LocalDate) {
        updateDailySummary(date)
    }
    
    fun getWeeklySummaries(): Flow<List<DailySummary>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        return dailySummaryDao.getSummariesBetweenDates(startDate, endDate)
    }
    
    fun getMonthlySummaries(): Flow<List<DailySummary>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29)
        return dailySummaryDao.getSummariesBetweenDates(startDate, endDate)
    }
} 