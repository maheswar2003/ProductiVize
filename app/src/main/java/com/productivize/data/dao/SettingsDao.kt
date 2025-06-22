package com.productivize.data.dao

import androidx.room.*
import com.productivize.data.model.Settings

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 0 LIMIT 1")
    suspend fun getSettings(): Settings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: Settings)

    @Update
    suspend fun update(settings: Settings)
} 