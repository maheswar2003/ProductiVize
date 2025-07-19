package com.productivize.data.repository

import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.dao.SettingsDao
import com.productivize.data.model.HourLog
import com.productivize.data.model.DailySummary
import com.productivize.data.model.Settings
import com.productivize.data.model.PerformanceTrend
import com.productivize.domain.calculator.AchievementMaster
import com.productivize.domain.generator.InsightEngine
import com.productivize.domain.tracker.MomentumTracker
import com.productivize.domain.analytics.LiveAnalytics
import com.productivize.utils.OptimizedDataStructures
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ProductivityRepository @Inject constructor(
    private val hourLogDao: HourLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val settingsDao: SettingsDao,
    private val achievementMaster: AchievementMaster,
    private val insightEngine: InsightEngine,
    private val momentumTracker: MomentumTracker,
    private val liveAnalytics: LiveAnalytics
) {
    
    // Performance optimization: Cached data with mutex for thread safety
    private val cacheMutex = Mutex()
    private val dailySummaryCache = OptimizedDataStructures.WeakCache<LocalDate, DailySummary?>()
    private val hourLogsCache = OptimizedDataStructures.WeakCache<LocalDate, List<HourLog>>()
    private val analyticsCache = OptimizedDataStructures.WeakCache<String, Any>()
    private var lastCacheUpdate = 0L
    private val cacheTimeout = 5_000L // 5 seconds for faster updates
    
    // Optimized: Single combined flow for daily data
    fun getDailyData(date: LocalDate): Flow<DailyData> = flow {
        val cached = getCachedDailyData(date)
        if (cached != null) {
            emit(cached)
            return@flow
        }
        
        val data = withContext(Dispatchers.IO) {
            val hourLogs = hourLogDao.getHourLogsForDate(date).first()
            val dailySummary = dailySummaryDao.getDailySummary(date).first()
            val insights = getBasicInsightsForDate(date)
            
            DailyData(
                date = date,
                hourLogs = hourLogs,
                dailySummary = dailySummary,
                insights = insights
            )
        }
        
        cacheDailyData(date, data)
        emit(data)
    }.flowOn(Dispatchers.IO)
    
    // Optimized: Combined analytics flow
    fun getAnalyticsData(date: LocalDate): Flow<AnalyticsData> = flow {
        val cacheKey = "analytics_$date"
        val cached = analyticsCache[cacheKey] as? AnalyticsData
        
        if (cached != null && System.currentTimeMillis() - lastCacheUpdate < cacheTimeout) {
            emit(cached)
            return@flow
        }
        
        val data = withContext(Dispatchers.IO) {
            // Get current day's data first to ensure it's up to date
            val currentDaySummary = dailySummaryDao.getDailySummary(date).first()
            val ratedHours = currentDaySummary?.totalHoursRated ?: hourLogDao.getRatedHoursCountForDate(date)
            val productiveHours = currentDaySummary?.productiveHours ?: hourLogDao.getProductiveHoursCountForDate(date)
            val avgRating = currentDaySummary?.averageRating ?: hourLogDao.getAverageRatingForDate(date) ?: 0f
            
            // Streak analysis (lightweight) - use updated summaries
            val recentSummaries = dailySummaryDao.getRecentSummaries(date.minusDays(7), 7)
            val streakLength = recentSummaries.takeWhile { it.productiveHours >= 3 }.size
            
            // Weekly analytics (optimized) - use updated summaries
            val weekSummaries = dailySummaryDao.getWeeklySummaries(date.minusDays(7)).first()
            val weeklyData = if (weekSummaries.isNotEmpty()) {
                val avgAchievement = weekSummaries.map { it.achievementPercentage }.average().toFloat()
                val avgPerformance = weekSummaries.map { it.performanceIndex }.average().toFloat()
                val consistency = calculateConsistency(weekSummaries)
                
                WeeklyAnalyticsData(
                    averageAchievement = avgAchievement,
                    averagePerformanceIndex = avgPerformance,
                    consistency = consistency,
                    streakLength = weekSummaries.maxOfOrNull { it.streakCount } ?: 0,
                    insights = listOf("Weekly performance tracked"),
                    recommendations = generateQuickRecommendations(avgAchievement, consistency)
                )
            } else {
                WeeklyAnalyticsData(0f, 0f, 0f, 0, emptyList(), emptyList())
            }
            
            AnalyticsData(
                liveAnalytics = LiveAnalyticsData(
                    energyLevel = avgRating / 5f,
                    currentTrend = PerformanceTrend.NEUTRAL,
                    focusDuration = ratedHours * 60
                ),
                streakAnalysis = StreakAnalysisData(
                    streakLength = streakLength,
                    isStreakAtRisk = streakLength > 0 && recentSummaries.firstOrNull()?.productiveHours ?: 0 < 3
                ),
                weeklyAnalytics = weeklyData
            )
        }
        
        analyticsCache[cacheKey] = data
        lastCacheUpdate = System.currentTimeMillis()
        emit(data)
    }.flowOn(Dispatchers.IO)
    
    // Basic operations with caching
    suspend fun insertHourLog(hourLog: HourLog) = withContext(Dispatchers.IO) {
        try {
            hourLogDao.insertHourLog(hourLog)
            clearCacheForDate(hourLog.dateTime.toLocalDate())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Always upsert (insert with REPLACE) for HourLog to ensure rating is saved
    suspend fun updateHourLog(hourLog: HourLog) = withContext(Dispatchers.IO) {
        try {
            hourLogDao.insertHourLog(hourLog) // Upsert: insert if new, update if exists
            
            // Calculate and update DailySummary immediately
            val date = hourLog.dateTime.toLocalDate()
            calculateAndUpdateDailySummary(date)
            
            // Clear cache immediately to ensure UI updates
            clearCacheForDate(date)
            // Force immediate cache invalidation
            lastCacheUpdate = 0L
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Calculate and update DailySummary for a given date
    private suspend fun calculateAndUpdateDailySummary(date: LocalDate) {
        try {
            val hourLogs = hourLogDao.getHourLogsForDate(date).first()
            val ratedLogs = hourLogs.filter { it.rating != null }
            
            if (ratedLogs.isEmpty()) {
                // If no rated logs, create empty summary or delete existing
                dailySummaryDao.deleteDailySummaryForDate(date)
                return
            }
            
            // Calculate basic metrics
            val totalHoursRated = ratedLogs.size
            val averageRating = ratedLogs.map { it.rating!! }.average().toFloat()
            val productiveHours = ratedLogs.count { it.rating!! >= 3 }
            val achievementPercentage = (productiveHours.toFloat() / totalHoursRated) * 100f
            
            // Calculate peak and low hours
            val peakHours = ratedLogs.filter { it.rating!! >= 4 }.map { it.hour }
            val lowHours = ratedLogs.filter { it.rating!! <= 2 }.map { it.hour }
            
            // Calculate performance index using AchievementMaster
            val performanceIndex = achievementMaster.calculatePerformanceIndex(ratedLogs, 3)
            val performanceGrade = when {
                performanceIndex >= 1.2f -> "A+"
                performanceIndex >= 1.1f -> "A"
                performanceIndex >= 1.0f -> "B+"
                performanceIndex >= 0.9f -> "B"
                performanceIndex >= 0.8f -> "C+"
                performanceIndex >= 0.7f -> "C"
                performanceIndex >= 0.6f -> "D"
                else -> "F"
            }
            
            // Calculate consistency
            val consistency = achievementMaster.calculateConsistency(ratedLogs)
            val consistencyRating = when {
                consistency >= 0.8f -> "Excellent"
                consistency >= 0.6f -> "Good"
                consistency >= 0.4f -> "Fair"
                else -> "Variable"
            }
            
            // Calculate momentum (simplified)
            val momentum = (performanceIndex * 0.6f + consistency * 0.4f).coerceIn(0f, 1f)
            val momentumLevel = when {
                momentum >= 0.8f -> "High"
                momentum >= 0.6f -> "Medium"
                momentum >= 0.4f -> "Low"
                else -> "Neutral"
            }
            
            // Get top tags
            val allTags = ratedLogs.flatMap { it.tags }
            val topTags = allTags.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }
                .take(5)
                .map { it.key }
            
            // Generate insights
            val insights = buildList {
                if (productiveHours > 0) add("$productiveHours productive hours today")
                if (peakHours.isNotEmpty()) add("Peak performance at ${peakHours.joinToString(", ")}:00")
                if (averageRating > 3.5f) add("Strong average rating of ${String.format("%.1f", averageRating)}")
            }
            
            // Create or update DailySummary
            val dailySummary = DailySummary(
                date = date,
                totalHoursRated = totalHoursRated,
                achievementPercentage = achievementPercentage,
                averageRating = averageRating,
                peakHours = peakHours,
                lowHours = lowHours,
                topTags = topTags,
                insights = insights,
                performanceIndex = performanceIndex,
                performanceGrade = performanceGrade,
                consistency = consistency,
                consistencyRating = consistencyRating,
                momentum = momentum,
                momentumLevel = momentumLevel,
                productiveHours = productiveHours,
                energyPattern = "BALANCED" // Simplified for now
            )
            
            dailySummaryDao.insertDailySummary(dailySummary)
            println("✅ Updated DailySummary for $date: $totalHoursRated hours, ${String.format("%.1f", achievementPercentage)}% achievement")
            
            // Clear analytics cache to ensure fresh data
            analyticsCache.clear()
            lastCacheUpdate = 0L
            
        } catch (e: Exception) {
            println("❌ Error calculating DailySummary for $date: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun insertDailySummary(summary: DailySummary) = withContext(Dispatchers.IO) {
        try {
            dailySummaryDao.insertDailySummary(summary)
            clearCacheForDate(summary.date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Optimized flows
    fun getHourLogsForDate(date: LocalDate): Flow<List<HourLog>> = 
        hourLogDao.getHourLogsForDate(date).flowOn(Dispatchers.IO)
    
    fun getDailySummary(date: LocalDate): Flow<DailySummary?> = 
        dailySummaryDao.getDailySummary(date).flowOn(Dispatchers.IO)
    
    // Lightweight operations
    fun getWeeklySummaries(): Flow<List<DailySummary>> = 
        dailySummaryDao.getWeeklySummaries(LocalDate.now().minusDays(7)).flowOn(Dispatchers.IO)
    
    fun getMonthlySummaries(): Flow<List<DailySummary>> = 
        dailySummaryDao.getMonthlySummaries(LocalDate.now().minusDays(30)).flowOn(Dispatchers.IO)
    
    // Settings
    suspend fun insertSettings(settings: Settings) = settingsDao.insert(settings)
    fun getSettings(): Flow<Settings> = flow { emit(settingsDao.getSettings() ?: Settings()) }
    
    // Public method to recalculate DailySummary for any date
    suspend fun recalculateDailySummary(date: LocalDate) = withContext(Dispatchers.IO) {
        calculateAndUpdateDailySummary(date)
        clearCacheForDate(date)
    }
    
    // Force refresh analytics data
    suspend fun refreshAnalyticsData(date: LocalDate) = withContext(Dispatchers.IO) {
        analyticsCache.clear()
        lastCacheUpdate = 0L
        // Force recalculation of daily summary to ensure fresh data
        calculateAndUpdateDailySummary(date)
    }
    
    // Enhanced insights generation using DailySummary data
    suspend fun getBasicInsightsForDate(date: LocalDate): List<String> = withContext(Dispatchers.IO) {
        try {
            // Get the most up-to-date DailySummary
            val dailySummary = dailySummaryDao.getDailySummary(date).first()
            
            if (dailySummary == null) {
                // Fallback to basic calculation if no summary exists
                val ratedHours = hourLogDao.getRatedHoursCountForDate(date)
                val productiveHours = hourLogDao.getProductiveHoursCountForDate(date)
                val avgRating = hourLogDao.getAverageRatingForDate(date) ?: 0f
                
                buildList {
                    if (ratedHours > 0) add("You rated $ratedHours hours today")
                    if (productiveHours > 0) add("$productiveHours productive hours (3+ stars)")
                    if (avgRating > 0) add("Average rating: ${String.format("%.1f", avgRating)}/5")
                }
            } else {
                // Use DailySummary data for comprehensive insights
                buildList {
                    // Basic metrics
                    if (dailySummary.totalHoursRated > 0) {
                        add("You rated ${dailySummary.totalHoursRated} hours today")
                    }
                    
                    if (dailySummary.productiveHours > 0) {
                        add("${dailySummary.productiveHours} productive hours (3+ stars)")
                    }
                    
                    if (dailySummary.averageRating > 0) {
                        add("Average rating: ${String.format("%.1f", dailySummary.averageRating)}/5")
                    }
                    
                    // Achievement insights
                    if (dailySummary.achievementPercentage > 0) {
                        when {
                            dailySummary.achievementPercentage >= 80 -> add("🌟 Exceptional achievement: ${dailySummary.achievementPercentage.toInt()}%")
                            dailySummary.achievementPercentage >= 60 -> add("💪 Strong performance: ${dailySummary.achievementPercentage.toInt()}% achievement")
                            dailySummary.achievementPercentage >= 40 -> add("📈 Good progress: ${dailySummary.achievementPercentage.toInt()}% achievement")
                            else -> add("🎯 Building momentum: ${dailySummary.achievementPercentage.toInt()}% achievement")
                        }
                    }
                    
                    // Peak hours insights
                    if (dailySummary.peakHours.isNotEmpty()) {
                        val peakHoursText = dailySummary.peakHours.sorted().joinToString(", ") { "${it}:00" }
                        add("⚡ Peak performance at $peakHoursText")
                    }
                    
                    // Performance grade insights
                    if (dailySummary.performanceGrade != "C") {
                        add("📊 Performance grade: ${dailySummary.performanceGrade}")
                    }
                    
                    // Consistency insights
                    if (dailySummary.consistency > 0) {
                        when {
                            dailySummary.consistency >= 0.8 -> add("🎯 Excellent consistency: ${(dailySummary.consistency * 100).toInt()}%")
                            dailySummary.consistency >= 0.6 -> add("📈 Good consistency: ${(dailySummary.consistency * 100).toInt()}%")
                            else -> add("🔄 Variable consistency: ${(dailySummary.consistency * 100).toInt()}%")
                        }
                    }
                    
                    // Energy pattern insights
                    if (dailySummary.energyPattern != "BALANCED") {
                        add("🌅 Energy pattern: ${dailySummary.energyPattern}")
                    }
                    
                    // Momentum insights
                    if (dailySummary.momentum > 0) {
                        when {
                            dailySummary.momentum >= 0.8 -> add("🚀 High momentum: Keep it up!")
                            dailySummary.momentum >= 0.6 -> add("📈 Building momentum")
                            else -> add("🌱 Building foundation")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error generating insights for $date: ${e.message}")
            emptyList()
        }
    }
    
    // Cache management
    private suspend fun getCachedDailyData(date: LocalDate): DailyData? = cacheMutex.withLock {
        if (System.currentTimeMillis() - lastCacheUpdate > cacheTimeout) {
            return null
        }
        
        val hourLogs = hourLogsCache[date]
        val dailySummary = dailySummaryCache[date]
        
        if (hourLogs != null && dailySummary != null) {
            DailyData(
                date = date,
                hourLogs = hourLogs,
                dailySummary = dailySummary,
                insights = emptyList() // Will be populated if needed
            )
        } else null
    }
    
    private suspend fun cacheDailyData(date: LocalDate, data: DailyData) = cacheMutex.withLock {
        hourLogsCache[date] = data.hourLogs
        dailySummaryCache[date] = data.dailySummary
        lastCacheUpdate = System.currentTimeMillis()
    }
    
    private suspend fun clearCacheForDate(date: LocalDate) = cacheMutex.withLock {
        // Clear all caches to ensure immediate UI updates
        hourLogsCache.clear()
        dailySummaryCache.clear()
        analyticsCache.clear()
        lastCacheUpdate = 0L // Force cache refresh
    }
    
    // Helper functions for optimized calculations
    private fun calculateConsistency(summaries: List<DailySummary>): Float {
        if (summaries.isEmpty()) return 0f
        val achievements = summaries.map { it.achievementPercentage }
        val max = achievements.maxOrNull() ?: 0f
        val min = achievements.minOrNull() ?: 0f
        return if (max > 0) 1f - ((max - min) / 100f) else 0f
    }
    
    private fun generateQuickRecommendations(avgAchievement: Float, consistency: Float): List<String> {
        return buildList {
            when {
                avgAchievement < 50f -> add("Focus on increasing productive hours")
                avgAchievement > 80f -> add("Excellent performance! Keep it up")
                else -> add("Good progress, aim for consistency")
            }
            
            when {
                consistency < 0.5f -> add("Work on daily consistency")
                consistency > 0.8f -> add("Great consistent performance")
                else -> add("Maintain steady progress")
            }
        }
    }
    
    // Performance data classes
    data class DailyData(
        val date: LocalDate,
        val hourLogs: List<HourLog>,
        val dailySummary: DailySummary?,
        val insights: List<String>
    )
    
    data class AnalyticsData(
        val liveAnalytics: LiveAnalyticsData,
        val streakAnalysis: StreakAnalysisData,
        val weeklyAnalytics: WeeklyAnalyticsData
    )
    
    data class LiveAnalyticsData(
        val energyLevel: Float,
        val currentTrend: PerformanceTrend,
        val focusDuration: Int
    )
    
    data class StreakAnalysisData(
        val streakLength: Int,
        val isStreakAtRisk: Boolean
    )
    
    data class WeeklyAnalyticsData(
        val averageAchievement: Float,
        val averagePerformanceIndex: Float,
        val consistency: Float,
        val streakLength: Int,
        val insights: List<String>,
        val recommendations: List<String>
    )
} 