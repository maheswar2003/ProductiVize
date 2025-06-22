package com.productivize.di

import android.content.Context
import androidx.room.Room
import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.dao.SettingsDao
import com.productivize.data.dao.JournalDao
import com.productivize.data.database.ProductiVizeDatabase
import com.productivize.data.repository.ProductivityRepository
import com.productivize.domain.calculator.AchievementCalculator
import com.productivize.domain.generator.InsightGenerator
import com.productivize.utils.DataExporter
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
    fun provideDatabase(@ApplicationContext context: Context): ProductiVizeDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ProductiVizeDatabase::class.java,
            "productivize_database"
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
    
    @Provides
    @Singleton
    fun provideAchievementCalculator(): AchievementCalculator {
        return AchievementCalculator()
    }
    
    @Provides
    @Singleton
    fun provideInsightGenerator(): InsightGenerator {
        return InsightGenerator()
    }
    
    @Provides
    @Singleton
    fun provideDataExporter(@ApplicationContext context: Context): DataExporter {
        return DataExporter(context)
    }
    
    @Provides
    @Singleton
    fun provideProductivityRepository(
        hourLogDao: HourLogDao,
        dailySummaryDao: DailySummaryDao,
        achievementCalculator: AchievementCalculator,
        insightGenerator: InsightGenerator
    ): ProductivityRepository {
        return ProductivityRepository(hourLogDao, dailySummaryDao, achievementCalculator, insightGenerator)
    }
} 