package com.productivize.data.dao

import androidx.room.*
import com.productivize.data.model.DailySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailySummaryDao {
    
    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    fun getDailySummary(date: LocalDate): Flow<DailySummary?>
    
    @Query("SELECT * FROM daily_summaries WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getDailySummariesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailySummary>>
    
    @Query("SELECT * FROM daily_summaries WHERE date >= :startDate ORDER BY date DESC LIMIT 7")
    fun getWeeklySummaries(startDate: LocalDate): Flow<List<DailySummary>>
    
    @Query("SELECT * FROM daily_summaries WHERE date >= :startDate ORDER BY date DESC LIMIT 30")
    fun getMonthlySummaries(startDate: LocalDate): Flow<List<DailySummary>>
    
    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSummary(): DailySummary?
    
    @Query("SELECT AVG(achievementPercentage) FROM daily_summaries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getAverageAchievement(startDate: LocalDate, endDate: LocalDate): Float?
    
    @Query("SELECT AVG(productiveHours) FROM daily_summaries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getAverageProductiveHours(startDate: LocalDate, endDate: LocalDate): Float?
    
    @Query("SELECT * FROM daily_summaries WHERE achievementPercentage >= :threshold AND date >= :startDate ORDER BY date DESC")
    suspend fun getHighAchievementDays(threshold: Float, startDate: LocalDate): List<DailySummary>
    
    @Query("SELECT COUNT(*) FROM daily_summaries WHERE productiveHours >= :threshold AND date >= :startDate")
    suspend fun getProductiveDaysCount(threshold: Int, startDate: LocalDate): Int
    
    @Query("SELECT * FROM daily_summaries WHERE date >= :startDate ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSummaries(startDate: LocalDate, limit: Int): List<DailySummary>
    
    // Simplified streak calculation
    @Query("""
        SELECT date, achievementPercentage, productiveHours
        FROM daily_summaries 
        WHERE date >= :startDate 
        ORDER BY date DESC 
        LIMIT :limit
    """)
    suspend fun getStreakData(startDate: LocalDate, limit: Int): List<StreakDataResult>
    
    // Simplified performance trend
    @Query("""
        SELECT date, achievementPercentage, productiveHours
        FROM daily_summaries 
        WHERE date >= :startDate 
        ORDER BY date ASC 
        LIMIT :limit
    """)
    suspend fun getPerformanceTrendData(startDate: LocalDate, limit: Int): List<PerformanceTrendData>
    
    // Basic analytics queries
    @Query("SELECT MAX(achievementPercentage) FROM daily_summaries WHERE date >= :startDate")
    suspend fun getMaxAchievement(startDate: LocalDate): Float?
    
    @Query("SELECT MIN(achievementPercentage) FROM daily_summaries WHERE date >= :startDate")
    suspend fun getMinAchievement(startDate: LocalDate): Float?
    
    @Query("SELECT COUNT(*) FROM daily_summaries WHERE date >= :startDate")
    suspend fun getTotalDaysTracked(startDate: LocalDate): Int
    
    @Query("SELECT SUM(productiveHours) FROM daily_summaries WHERE date >= :startDate")
    suspend fun getTotalProductiveHours(startDate: LocalDate): Int?
    
    @Query("SELECT SUM(totalHoursRated) FROM daily_summaries WHERE date >= :startDate")
    suspend fun getTotalRatedHours(startDate: LocalDate): Int?
    
    // Consistency metrics
    @Query("SELECT * FROM daily_summaries WHERE productiveHours > 0 AND date >= :startDate ORDER BY date DESC")
    suspend fun getActiveTrackingDays(startDate: LocalDate): List<DailySummary>
    
    @Query("SELECT AVG(achievementPercentage) FROM daily_summaries WHERE date >= :startDate AND productiveHours > 0")
    suspend fun getAverageActiveAchievement(startDate: LocalDate): Float?
    
    // Weekly analysis
    @Query("""
        SELECT 
            date, 
            achievementPercentage,
            productiveHours,
            totalHoursRated,
            averageRating,
            peakHours,
            lowHours
        FROM daily_summaries 
        WHERE date >= :startDate 
        ORDER BY date ASC
    """)
    suspend fun getWeeklyAnalysisData(startDate: LocalDate): List<WeeklyAnalysisData>
    
    // Monthly trends
    @Query("""
        SELECT 
            date, 
            achievementPercentage,
            productiveHours,
            averageRating
        FROM daily_summaries 
        WHERE date >= :startDate 
        ORDER BY date ASC
    """)
    suspend fun getMonthlyTrendData(startDate: LocalDate): List<MonthlyTrendData>
    
    // Performance distribution
    @Query("""
        SELECT 
            CASE 
                WHEN achievementPercentage >= 80 THEN 'high'
                WHEN achievementPercentage >= 60 THEN 'medium'
                ELSE 'low'
            END as performance_tier,
            COUNT(*) as count
        FROM daily_summaries 
        WHERE date >= :startDate
        GROUP BY performance_tier
    """)
    suspend fun getPerformanceDistribution(startDate: LocalDate): List<PerformanceDistribution>
    
    // Recent performance patterns
    @Query("""
        SELECT 
            date,
            achievementPercentage,
            productiveHours,
            CASE 
                WHEN achievementPercentage > 75 THEN 'excellent'
                WHEN achievementPercentage > 50 THEN 'good'
                ELSE 'needs_improvement'
            END as performance_level
        FROM daily_summaries 
        WHERE date >= :startDate 
        ORDER BY date DESC 
        LIMIT :limit
    """)
    suspend fun getRecentPerformancePatterns(startDate: LocalDate, limit: Int): List<RecentPerformancePattern>
    
    // Goal achievement analysis
    @Query("""
        SELECT 
            date,
            productiveHours,
            CASE 
                WHEN productiveHours >= 8 THEN 'achieved'
                WHEN productiveHours >= 6 THEN 'close'
                ELSE 'missed'
            END as goal_status
        FROM daily_summaries 
        WHERE date >= :startDate 
        ORDER BY date DESC
    """)
    suspend fun getGoalAchievementAnalysis(startDate: LocalDate): List<GoalAchievementData>
    
    // Productivity insights
    @Query("""
        SELECT 
            AVG(achievementPercentage) as avg_achievement,
            AVG(productiveHours) as avg_productive_hours,
            COUNT(*) as total_days,
            MAX(achievementPercentage) as best_achievement,
            MIN(achievementPercentage) as worst_achievement
        FROM daily_summaries 
        WHERE date >= :startDate
    """)
    suspend fun getProductivityInsights(startDate: LocalDate): ProductivityInsights?
    
    // Simplified achievement percentiles
    @Query("""
        SELECT 
            achievementPercentage,
            COUNT(*) as frequency
        FROM daily_summaries 
        WHERE date >= :startDate
        GROUP BY achievementPercentage
        ORDER BY achievementPercentage DESC
    """)
    suspend fun getAchievementPercentiles(startDate: LocalDate): List<AchievementPercentile>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(dailySummary: DailySummary)
    
    @Update
    suspend fun updateDailySummary(dailySummary: DailySummary)
    
    @Delete
    suspend fun deleteDailySummary(dailySummary: DailySummary)
    
    @Query("DELETE FROM daily_summaries WHERE date = :date")
    suspend fun deleteDailySummaryForDate(date: LocalDate)
    
    @Query("DELETE FROM daily_summaries WHERE date < :beforeDate")
    suspend fun deleteOldDailySummaries(beforeDate: LocalDate)
    
    @Query("DELETE FROM daily_summaries")
    suspend fun deleteAllDailySummaries()
    
    // Data classes for query results
    data class StreakDataResult(
        val date: LocalDate,
        val achievementPercentage: Float,
        val productiveHours: Int
    )
    
    data class PerformanceTrendData(
        val date: LocalDate,
        val achievementPercentage: Float,
        val productiveHours: Int
    )
    
    data class WeeklyAnalysisData(
        val date: LocalDate,
        val achievementPercentage: Float,
        val productiveHours: Int,
        val totalHoursRated: Int,
        val averageRating: Float,
        val peakHours: String,
        val lowHours: String
    )
    
    data class MonthlyTrendData(
        val date: LocalDate,
        val achievementPercentage: Float,
        val productiveHours: Int,
        val averageRating: Float
    )
    
    data class PerformanceDistribution(
        val performance_tier: String,
        val count: Int
    )
    
    data class RecentPerformancePattern(
        val date: LocalDate,
        val achievementPercentage: Float,
        val productiveHours: Int,
        val performance_level: String
    )
    
    data class GoalAchievementData(
        val date: LocalDate,
        val productiveHours: Int,
        val goal_status: String
    )
    
    data class ProductivityInsights(
        val avg_achievement: Float,
        val avg_productive_hours: Float,
        val total_days: Int,
        val best_achievement: Float,
        val worst_achievement: Float
    )
    
    data class AchievementPercentile(
        val achievementPercentage: Float,
        val frequency: Int
    )
} 