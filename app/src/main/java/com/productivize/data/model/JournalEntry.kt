package com.productivize.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val createdTime: Long = System.currentTimeMillis(),
    val wins: String = "",
    val challenges: String = "",
    val goalsTomorrow: String = "",
    val autoAchievements: String = "",
    val autoPatterns: String = "",
    val moodEmoji: String = "😐", // Default neutral
    val voiceNotePath: String? = null,
    val imageUris: List<String> = emptyList(),
    val isLocked: Boolean = false
) 