package com.productivize.data.dao

import androidx.room.*
import com.productivize.data.model.DailySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailySummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailySummary: DailySummary)
    
    @Update
    suspend fun update(dailySummary: DailySummary)
    
    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    suspend fun getSummaryForDate(date: LocalDate): DailySummary?
    
    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    fun observeSummaryForDate(date: LocalDate): Flow<DailySummary?>
    
    @Query("SELECT * FROM daily_summaries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSummariesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DailySummary>>
    
    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int): Flow<List<DailySummary>>
    
    @Query("SELECT AVG(achievementPercentage) FROM daily_summaries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageAchievementBetweenDates(startDate: LocalDate, endDate: LocalDate): Float?
    
    @Query("SELECT * FROM daily_summaries WHERE achievementPercentage >= 80 ORDER BY date DESC")
    fun getExcellentDays(): Flow<List<DailySummary>>
} 