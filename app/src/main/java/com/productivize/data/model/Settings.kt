package com.productivize.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 0,
    val darkMode: Boolean = false,
    val notificationTime: String = "20:00",
    val vibrationEnabled: Boolean = true,
    val exportFormat: String = "CSV", // CSV/JSON
    val notificationsEnabled: Boolean = true,
    val hourlyReminders: Boolean = true,
    val journalReminders: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val autoLockJournal: Boolean = false,
    val autoBackupEnabled: Boolean = false
) 