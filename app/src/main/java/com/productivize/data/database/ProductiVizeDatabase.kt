package com.productivize.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.model.HourLog
import com.productivize.data.model.DailySummary

@Database(
    entities = [HourLog::class, DailySummary::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ProductiVizeDatabase : RoomDatabase() {
    
    abstract fun hourLogDao(): HourLogDao
    abstract fun dailySummaryDao(): DailySummaryDao
    
    companion object {
        @Volatile
        private var INSTANCE: ProductiVizeDatabase? = null
        
        fun getDatabase(context: Context): ProductiVizeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductiVizeDatabase::class.java,
                    "productivize_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 