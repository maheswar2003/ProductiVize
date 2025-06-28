package com.productivize.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.dao.SettingsDao
import com.productivize.data.dao.JournalDao
import com.productivize.data.model.HourLog
import com.productivize.data.model.DailySummary
import com.productivize.data.model.Settings
import com.productivize.data.model.JournalEntry

@Database(
    entities = [HourLog::class, DailySummary::class, Settings::class, JournalEntry::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ProductiVizeDatabase : RoomDatabase() {
    
    abstract fun hourLogDao(): HourLogDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun journalDao(): JournalDao
    
    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE settings ADD COLUMN dailyGoalHours INTEGER NOT NULL DEFAULT 8")
                database.execSQL("ALTER TABLE settings ADD COLUMN achievementThreshold INTEGER NOT NULL DEFAULT 3")
            }
        }
    }
} 