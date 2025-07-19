package com.productivize.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.productivize.data.dao.*
import com.productivize.data.model.*

@Database(
    entities = [
        HourLog::class,
        DailySummary::class,
        Settings::class,
        JournalEntry::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ProductiVizeDatabase : RoomDatabase() {
    abstract fun hourLogDao(): HourLogDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun journalDao(): JournalDao
    
    companion object {
        // Migration from version 4 to 5
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add settings table enhancements
                database.execSQL("ALTER TABLE settings ADD COLUMN dailyGoalHours INTEGER NOT NULL DEFAULT 8")
                
                // Add additional hour log fields
                database.execSQL("ALTER TABLE hour_logs ADD COLUMN notes TEXT")
                database.execSQL("ALTER TABLE hour_logs ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE hour_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // Migration from version 5 to 6 - Advanced features
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add advanced analytics fields to daily_summaries
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN performanceIndex REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN performanceGrade TEXT NOT NULL DEFAULT 'C'")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN consistency REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN consistencyRating TEXT NOT NULL DEFAULT 'Variable'")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN momentum REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN momentumLevel TEXT NOT NULL DEFAULT 'Neutral'")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN energyPattern TEXT NOT NULL DEFAULT 'BALANCED'")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN streakCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN streakType TEXT NOT NULL DEFAULT 'NONE'")
            }
        }
        
        // Migration from version 6 to 7 - Add missing columns
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add missing columns to daily_summaries table
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN adaptiveThreshold INTEGER NOT NULL DEFAULT 3")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN productiveHours INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE daily_summaries ADD COLUMN predictiveInsight TEXT NOT NULL DEFAULT ''")
            }
        }
        
        // Migration from version 7 to 8 - Fix schema mismatch
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new table with the correct schema
                database.execSQL("""
                    CREATE TABLE daily_summaries_new (
                        date TEXT NOT NULL PRIMARY KEY,
                        totalHoursRated INTEGER NOT NULL DEFAULT 0,
                        achievementPercentage REAL NOT NULL DEFAULT 0.0,
                        averageRating REAL NOT NULL DEFAULT 0.0,
                        peakHours TEXT NOT NULL DEFAULT '[]',
                        lowHours TEXT NOT NULL DEFAULT '[]',
                        topTags TEXT NOT NULL DEFAULT '[]',
                        insights TEXT NOT NULL DEFAULT '[]',
                        journalEntry TEXT,
                        wins TEXT NOT NULL DEFAULT '[]',
                        challenges TEXT NOT NULL DEFAULT '[]',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        performanceIndex REAL NOT NULL DEFAULT 0.0,
                        performanceGrade TEXT NOT NULL DEFAULT 'C',
                        consistency REAL NOT NULL DEFAULT 0.0,
                        consistencyRating TEXT NOT NULL DEFAULT 'Variable',
                        momentum REAL NOT NULL DEFAULT 0.0,
                        momentumLevel TEXT NOT NULL DEFAULT 'Neutral',
                        energyPattern TEXT NOT NULL DEFAULT 'BALANCED',
                        streakCount INTEGER NOT NULL DEFAULT 0,
                        streakType TEXT NOT NULL DEFAULT 'NONE',
                        adaptiveThreshold INTEGER NOT NULL DEFAULT 3,
                        productiveHours INTEGER NOT NULL DEFAULT 0,
                        predictiveInsight TEXT NOT NULL DEFAULT ''
                    )
                """)
                
                // Copy data from old table to new table if it exists
                try {
                    database.execSQL("""
                        INSERT INTO daily_summaries_new 
                        SELECT date, totalHoursRated, achievementPercentage, averageRating, 
                               peakHours, lowHours, topTags, insights, journalEntry, wins, 
                               challenges, createdAt, performanceIndex, performanceGrade, 
                               consistency, consistencyRating, momentum, 
                               COALESCE(momentumLevel, 'Neutral') as momentumLevel,
                               energyPattern, streakCount, streakType,
                               COALESCE(adaptiveThreshold, 3) as adaptiveThreshold,
                               COALESCE(productiveHours, 0) as productiveHours,
                               COALESCE(predictiveInsight, '') as predictiveInsight
                        FROM daily_summaries
                    """)
                } catch (e: Exception) {
                    // Old table doesn't exist, that's fine
                }
                
                // Drop old table and rename new table
                database.execSQL("DROP TABLE IF EXISTS daily_summaries")
                database.execSQL("ALTER TABLE daily_summaries_new RENAME TO daily_summaries")
            }
        }
    }
} 