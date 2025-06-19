package com.productivize.data.dao

import androidx.room.*
import com.productivize.data.model.HourLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface HourLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hourLog: HourLog)
    
    @Update
    suspend fun update(hourLog: HourLog)
    
    @Delete
    suspend fun delete(hourLog: HourLog)
    
    @Query("SELECT * FROM hour_logs WHERE id = :id")
    suspend fun getHourLogById(id: String): HourLog?
    
    @Query("SELECT * FROM hour_logs WHERE date(dateTime) = date(:date) ORDER BY hour ASC")
    fun getHourLogsForDate(date: String): Flow<List<HourLog>>
    
    @Query("SELECT * FROM hour_logs WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getHourLogsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<HourLog>>
    
    @Query("SELECT * FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) = date(:date)")
    suspend fun getRatedHoursForDate(date: String): List<HourLog>
    
    @Query("SELECT AVG(rating) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) = date(:date)")
    suspend fun getAverageRatingForDate(date: String): Float?
    
    @Query("SELECT COUNT(*) FROM hour_logs WHERE rating IS NOT NULL AND date(dateTime) = date(:date)")
    suspend fun getRatedHoursCountForDate(date: String): Int
    
    @Query("SELECT * FROM hour_logs WHERE rating >= 4 AND date(dateTime) = date(:date)")
    suspend fun getPeakHoursForDate(date: String): List<HourLog>
    
    @Query("SELECT * FROM hour_logs WHERE rating <= 2 AND rating IS NOT NULL AND date(dateTime) = date(:date)")
    suspend fun getLowHoursForDate(date: String): List<HourLog>
} 