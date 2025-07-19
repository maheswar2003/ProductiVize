package com.productivize.data.dao

import androidx.room.*
import com.productivize.data.model.HourLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface HourLogDao {
    
    @Query("SELECT * FROM hour_logs WHERE id = :id")
    suspend fun getHourLogById(id: String): HourLog?
    
    @Query("SELECT * FROM hour_logs WHERE date(dateTime) = :date ORDER BY hour ASC")
    fun getHourLogsForDate(date: LocalDate): Flow<List<HourLog>>
    
    @Query("SELECT * FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) = :date ORDER BY hour ASC")
    fun getRatedHourLogsForDate(date: LocalDate): Flow<List<HourLog>>
    
    @Query("SELECT * FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate ORDER BY dateTime ASC")
    fun getRatedHourLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<HourLog>>
    
    @Query("SELECT * FROM hour_logs WHERE date(dateTime) >= :startDate AND date(dateTime) <= :endDate ORDER BY dateTime ASC")
    fun getHourLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<HourLog>>
    
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) = :date")
    suspend fun getAverageRatingForDate(date: LocalDate): Float?
    
    @Query("SELECT COUNT(*) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) = :date")
    suspend fun getRatedHoursCountForDate(date: LocalDate): Int
    
    @Query("SELECT COUNT(*) FROM hour_logs WHERE rating IS NOT NULL AND rating >= 3 AND date(dateTime) = :date")
    suspend fun getProductiveHoursCountForDate(date: LocalDate): Int
    
    @Query("SELECT * FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate ORDER BY rating DESC, dateTime ASC")
    fun getTopRatedHours(startDate: LocalDate, endDate: LocalDate): Flow<List<HourLog>>
    
    @Query("SELECT * FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate ORDER BY rating ASC, dateTime ASC")
    fun getLowestRatedHours(startDate: LocalDate, endDate: LocalDate): Flow<List<HourLog>>
    
    @Query("SELECT hour, COUNT(*) as count FROM hour_logs WHERE rating IS NOT NULL AND rating >= 4 AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate GROUP BY hour ORDER BY count DESC LIMIT 3")
    suspend fun getPeakHours(startDate: LocalDate, endDate: LocalDate): List<HourFrequency>
    
    @Query("SELECT hour, COUNT(*) as count FROM hour_logs WHERE rating IS NOT NULL AND rating <= 2 AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate GROUP BY hour ORDER BY count DESC LIMIT 3")
    suspend fun getLowEnergyHours(startDate: LocalDate, endDate: LocalDate): List<HourFrequency>
    
    // Basic analytics queries
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate")
    suspend fun getAverageRatingInRange(startDate: LocalDate, endDate: LocalDate): Float?
    
    @Query("SELECT COUNT(*) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate")
    suspend fun getTotalRatedHours(startDate: LocalDate, endDate: LocalDate): Int
    
    @Query("SELECT COUNT(*) FROM hour_logs WHERE rating IS NOT NULL AND rating >= 3 AND date(dateTime) >= :startDate AND date(dateTime) <= :endDate")
    suspend fun getProductiveHoursInRange(startDate: LocalDate, endDate: LocalDate): Int
    
    // Simplified pattern detection queries
    @Query("SELECT hour, AVG(rating) as avgRating FROM hour_logs WHERE rating IS NOT NULL GROUP BY hour ORDER BY avgRating DESC LIMIT 5")
    suspend fun getBestPerformingHours(): List<HourPerformance>
    
    @Query("SELECT hour, AVG(rating) as avgRating FROM hour_logs WHERE rating IS NOT NULL GROUP BY hour ORDER BY avgRating ASC LIMIT 5")
    suspend fun getWorstPerformingHours(): List<HourPerformance>
    
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND hour >= 6 AND hour <= 11")
    suspend fun getMorningAverageRating(): Float?
    
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND hour >= 12 AND hour <= 17")
    suspend fun getAfternoonAverageRating(): Float?
    
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND hour >= 18 AND hour <= 23")
    suspend fun getEveningAverageRating(): Float?
    
    // Recent performance queries
    @Query("SELECT * FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate ORDER BY dateTime DESC LIMIT :limit")
    suspend fun getRecentRatedHours(startDate: LocalDate, limit: Int): List<HourLog>
    
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) >= :startDate")
    suspend fun getRecentAverageRating(startDate: LocalDate): Float?
    
    @Query("SELECT COUNT(*) FROM hour_logs WHERE rating IS NOT NULL AND rating >= 3 AND date(dateTime) >= :startDate")
    suspend fun getRecentProductiveHours(startDate: LocalDate): Int
    
    // Consistency queries
    @Query("SELECT date(dateTime) as date, COUNT(*) as count FROM hour_logs WHERE rating IS NOT NULL GROUP BY date(dateTime) ORDER BY date DESC LIMIT 30")
    suspend fun getDailyRatingCounts(): List<DailyCount>
    
    @Query("SELECT date(dateTime) as date, AVG(rating) as avgRating FROM hour_logs WHERE rating IS NOT NULL GROUP BY date(dateTime) ORDER BY date DESC LIMIT 30")
    suspend fun getDailyAverageRatings(): List<DailyAverage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourLog(hourLog: HourLog)
    
    @Delete
    suspend fun deleteHourLog(hourLog: HourLog)
    
    @Query("DELETE FROM hour_logs WHERE date(dateTime) = :date")
    suspend fun deleteHourLogsForDate(date: LocalDate)
    
    @Query("DELETE FROM hour_logs WHERE date(dateTime) < :beforeDate")
    suspend fun deleteOldHourLogs(beforeDate: LocalDate)
    
    @Query("DELETE FROM hour_logs")
    suspend fun deleteAllHourLogs()
    
    // Data classes for query results
    data class HourFrequency(
        val hour: Int,
        val count: Int
    )
    
    data class HourPerformance(
        val hour: Int,
        val avgRating: Float
    )
    
    data class DailyCount(
        val date: String,
        val count: Int
    )
    
    data class DailyAverage(
        val date: String,
        val avgRating: Float
    )
} 