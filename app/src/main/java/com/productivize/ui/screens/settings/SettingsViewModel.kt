package com.productivize.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivize.data.dao.SettingsDao
import com.productivize.data.dao.HourLogDao
import com.productivize.data.dao.DailySummaryDao
import com.productivize.data.dao.JournalDao
import com.productivize.data.model.Settings
import com.productivize.utils.DataExporter
import com.productivize.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDao: SettingsDao,
    private val hourLogDao: HourLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val journalDao: JournalDao,
    private val dataExporter: DataExporter,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDao.getSettings()?.let { savedSettings ->
                _settings.value = savedSettings
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(darkMode = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateNotificationTime(time: String) {
        val updatedSettings = _settings.value.copy(notificationTime = time)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateVibration(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(vibrationEnabled = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateExportFormat(format: String) {
        val updatedSettings = _settings.value.copy(exportFormat = format)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(notificationsEnabled = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
        
        if (enabled) {
            // Show a test notification to demonstrate the feature works
            notificationHelper.showJournalReminder(updatedSettings)
        } else {
            // Cancel all notifications when disabled
            notificationHelper.cancelAllNotifications()
        }
    }

    fun updateHourlyReminders(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(hourlyReminders = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
        
        if (enabled && updatedSettings.notificationsEnabled) {
            // Show a test hourly reminder
            notificationHelper.showHourlyReminder(updatedSettings)
        }
    }

    fun updateJournalReminders(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(journalReminders = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
        
        if (enabled && updatedSettings.notificationsEnabled) {
            // Show a test journal reminder
            notificationHelper.showJournalReminder(updatedSettings)
        }
    }

    fun updateBiometricLock(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(biometricLockEnabled = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateAutoLockJournal(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(autoLockJournal = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateAutoBackup(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(autoBackupEnabled = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
        
        if (enabled) {
            // Schedule daily backup (simplified implementation)
            scheduleAutoBackup()
        } else {
            // Cancel scheduled backups
            cancelAutoBackup()
        }
    }

    private fun scheduleAutoBackup() {
        // In a real implementation, this would use WorkManager or AlarmManager
        // For now, we'll just show a notification that auto-backup is enabled
        viewModelScope.launch {
            try {
                // Simulate backup scheduling
                notificationHelper.showAutoBackupEnabled()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun cancelAutoBackup() {
        // Cancel any scheduled backup work
        // For now, just a placeholder
    }

    fun updateDailyGoal(hours: Int) {
        val updatedSettings = _settings.value.copy(dailyGoalHours = hours)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateAchievementThreshold(threshold: Int) {
        val updatedSettings = _settings.value.copy(achievementThreshold = threshold)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    private fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            settingsDao.insert(settings)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                val hourLogs = hourLogDao.getAllHourLogs()
                val dailySummaries = dailySummaryDao.getAllSummaries()
                val journalEntries = journalDao.getAllEntries()
                
                when (_settings.value.exportFormat) {
                    "CSV" -> {
                        val shareIntent = dataExporter.exportToCSV(hourLogs, dailySummaries, journalEntries)
                        shareIntent?.let { 
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(Intent.createChooser(it, "Export Data"))
                        }
                    }
                    "JSON" -> {
                        val shareIntent = dataExporter.exportToJSON(hourLogs, dailySummaries, journalEntries)
                        shareIntent?.let { 
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(Intent.createChooser(it, "Export Data"))
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle export error - could emit to UI state
                e.printStackTrace()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                hourLogDao.deleteAllHourLogs()
                dailySummaryDao.deleteAllSummaries()
                journalDao.deleteAllEntries()
                // Reset settings to default
                _settings.value = Settings()
                settingsDao.insert(_settings.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun showVersionInfo() {
        // Create an intent to show app info or version details
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback - could show a dialog with help info
            e.printStackTrace()
        }
    }
    
    fun openHelp() {
        // Open help documentation or support page
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://productivize.app/help")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback - could show a dialog with help info
        }
    }
    
    fun rateApp() {
        // Open Play Store rating page
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("market://details?id=${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web Play Store
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                // Handle error
            }
        }
    }
    
    fun shareApp() {
        // Create share intent for the app
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out ProductiVize!")
            putExtra(Intent.EXTRA_TEXT, 
                "I've been using ProductiVize to track my productivity and it's amazing! " +
                "Get it here: https://play.google.com/store/apps/details?id=${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ProductiVize"))
    }
} 