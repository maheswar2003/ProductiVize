package com.productivize.di

import android.content.Context
import androidx.room.Room
import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.dao.SettingsDao
import com.productivize.data.dao.JournalDao
import com.productivize.data.database.ProductiVizeDatabase
import com.productivize.data.repository.ProductivityRepository
import com.productivize.domain.calculator.AchievementMaster
import com.productivize.domain.generator.InsightEngine
import com.productivize.domain.tracker.MomentumTracker
import com.productivize.domain.analytics.LiveAnalytics
import com.productivize.utils.DataExporter
import com.productivize.utils.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideProductiVizeDatabase(@ApplicationContext context: Context): ProductiVizeDatabase {
        return Room.databaseBuilder(
            context,
            ProductiVizeDatabase::class.java,
            "productivize_database"
        )
        .addMigrations(
            ProductiVizeDatabase.MIGRATION_4_5,
            ProductiVizeDatabase.MIGRATION_5_6,
            ProductiVizeDatabase.MIGRATION_6_7,
            ProductiVizeDatabase.MIGRATION_7_8
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideHourLogDao(database: ProductiVizeDatabase): HourLogDao {
        return database.hourLogDao()
    }
    
    @Provides
    fun provideDailySummaryDao(database: ProductiVizeDatabase): DailySummaryDao {
        return database.dailySummaryDao()
    }
    
    @Provides
    fun provideSettingsDao(database: ProductiVizeDatabase): SettingsDao {
        return database.settingsDao()
    }
    
    @Provides
    fun provideJournalDao(database: ProductiVizeDatabase): JournalDao {
        return database.journalDao()
    }
    
    // Advanced calculation and analytics components
    @Provides
    @Singleton
    fun provideAchievementMaster(): AchievementMaster {
        return AchievementMaster()
    }
    
    @Provides
    @Singleton
    fun provideInsightEngine(): InsightEngine {
        return InsightEngine()
    }
    
    @Provides
    @Singleton
    fun provideMomentumTracker(): MomentumTracker {
        return MomentumTracker()
    }
    
    @Provides
    @Singleton
    fun provideLiveAnalytics(): LiveAnalytics {
        return LiveAnalytics()
    }
    
    // Utility components
    @Provides
    @Singleton
    fun provideDataExporter(@ApplicationContext context: Context): DataExporter {
        return DataExporter(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
    
    // Main repository with all advanced components
    @Provides
    @Singleton
    fun provideProductivityRepository(
        hourLogDao: HourLogDao,
        dailySummaryDao: DailySummaryDao,
        settingsDao: SettingsDao,
        achievementMaster: AchievementMaster,
        insightEngine: InsightEngine,
        momentumTracker: MomentumTracker,
        liveAnalytics: LiveAnalytics
    ): ProductivityRepository {
        return ProductivityRepository(
            hourLogDao, 
            dailySummaryDao, 
            settingsDao, 
            achievementMaster, 
            insightEngine,
            momentumTracker,
            liveAnalytics
        )
    }
} 