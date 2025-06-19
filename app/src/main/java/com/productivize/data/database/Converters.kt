package com.productivize.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
} 