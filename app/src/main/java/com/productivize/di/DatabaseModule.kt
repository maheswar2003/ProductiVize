package com.productivize.di

import android.content.Context
import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.database.ProductiVizeDatabase
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
        return ProductiVizeDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideHourLogDao(database: ProductiVizeDatabase): HourLogDao {
        return database.hourLogDao()
    }
    
    @Provides
    fun provideDailySummaryDao(database: ProductiVizeDatabase): DailySummaryDao {
        return database.dailySummaryDao()
    }
} 