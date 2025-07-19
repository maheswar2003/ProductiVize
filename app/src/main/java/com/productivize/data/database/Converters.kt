package com.productivize.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.productivize.data.model.PerformanceTrend
import com.productivize.domain.tracker.MomentumTracker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }
    
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toStringList(json: String): List<String> {
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }
    
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toIntList(json: String): List<Int> {
        return gson.fromJson(json, object : TypeToken<List<Int>>() {}.type)
    }
    
    // New converters for advanced features
    @TypeConverter
    fun fromPerformanceTrend(trend: PerformanceTrend): String {
        return trend.name
    }
    
    @TypeConverter
    fun toPerformanceTrend(trendString: String): PerformanceTrend {
        return try {
            PerformanceTrend.valueOf(trendString)
        } catch (e: IllegalArgumentException) {
            PerformanceTrend.NEUTRAL
        }
    }
    
    @TypeConverter
    fun fromStreakTypeSet(types: Set<MomentumTracker.StreakType>): String {
        return gson.toJson(types.map { it.name })
    }
    
    @TypeConverter
    fun toStreakTypeSet(json: String): Set<MomentumTracker.StreakType> {
        return try {
            val typeNames: List<String> = gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
            typeNames.mapNotNull { 
                try { 
                    MomentumTracker.StreakType.valueOf(it) 
                } catch (e: IllegalArgumentException) { 
                    null 
                }
            }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    @TypeConverter
    fun fromFloatList(list: List<Float>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toFloatList(json: String): List<Float> {
        return try {
            gson.fromJson(json, object : TypeToken<List<Float>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }
} 