package com.productivize.data.dao

import androidx.room.*
import com.productivize.data.model.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries WHERE date = :date")
    fun getEntryByDate(date: String): Flow<JournalEntry?>

    @Query("SELECT * FROM journal_entries WHERE date BETWEEN :start AND :end")
    suspend fun getEntriesBetweenDates(start: String, end: String): List<JournalEntry>
    
    @Query("SELECT * FROM journal_entries ORDER BY date ASC")
    suspend fun getAllEntries(): List<JournalEntry>
    
    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()
} 