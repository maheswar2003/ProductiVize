package com.productivize.data.repository

import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
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
    
    fun getHourLogsForDate(date: LocalDate): Flow<List<HourLog>> {
        return hourLogDao.getHourLogsForDate(date.toString())
    }
    
    // DailySummary operations
    private suspend fun updateDailySummary(date: LocalDate) {
        val dateStr = date.toString()
        val ratedHours = hourLogDao.getRatedHoursForDate(dateStr)
        
        if (ratedHours.isEmpty()) return
        
        val achievementPercentage = achievementCalculator.calculateAchievementPercentage(ratedHours)
        val averageRating = hourLogDao.getAverageRatingForDate(dateStr) ?: 0f
        val peakHours = hourLogDao.getPeakHoursForDate(dateStr).map { it.hour }
        val lowHours = hourLogDao.getLowHoursForDate(dateStr).map { it.hour }
        
        // Extract top tags
        val allTags = ratedHours.flatMap { it.tags }
        val topTags = allTags.groupingBy { it }.eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
        
        // Generate insights
        val insights = insightGenerator.generateDailyInsights(
            ratedHours,
            achievementPercentage,
            peakHours,
            lowHours
        )
        
        // Pre-fill wins and challenges
        val wins = ratedHours.filter { it.rating ?: 0 >= 4 }
            .mapNotNull { it.notes?.takeIf { notes -> notes.isNotBlank() } }
            .take(3)
        
        val challenges = ratedHours.filter { it.rating ?: 0 <= 2 }
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