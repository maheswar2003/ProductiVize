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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    
    // Export state management
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()
    
    // Settings feedback state
    private val _settingsFeedback = MutableStateFlow<String?>(null)
    val settingsFeedback: StateFlow<String?> = _settingsFeedback.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                settingsDao.getSettings()?.let { savedSettings ->
                    _settings.value = savedSettings
                }
            } catch (e: Exception) {
                // Keep default settings if loading fails
                e.printStackTrace()
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        // INSTANT UI UPDATE - happens immediately on main thread
        _settings.value = _settings.value.copy(darkMode = enabled)
        
        // Background database save - non-blocking
        viewModelScope.launch(Dispatchers.IO) {
            try {
                settingsDao.insert(_settings.value)
            } catch (e: Exception) {
                // On error, revert to previous state
                withContext(Dispatchers.Main) {
                    _settings.value = _settings.value.copy(darkMode = !enabled)
                }
                e.printStackTrace()
            }
        }
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
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(biometricLockEnabled = enabled)
                _settings.value = updatedSettings
                saveSettings(updatedSettings)
                
                // Show feedback based on biometric availability
                if (enabled) {
                    if (isBiometricAvailable()) {
                        _settingsFeedback.value = "🔒 Biometric lock enabled"
                    } else {
                        _settingsFeedback.value = "⚠️ Biometric authentication not available"
                        // Revert the setting since biometric is not available
                        _settings.value = _settings.value.copy(biometricLockEnabled = false)
                    }
                } else {
                    _settingsFeedback.value = "🔓 Biometric lock disabled"
                }
                
                // Clear feedback after 3 seconds
                kotlinx.coroutines.delay(3000)
                _settingsFeedback.value = null
            } catch (e: Exception) {
                // Revert on error
                _settings.value = _settings.value.copy(biometricLockEnabled = !enabled)
                e.printStackTrace()
            }
        }
    }

    fun updateAutoLockJournal(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(autoLockJournal = enabled)
                _settings.value = updatedSettings
                saveSettings(updatedSettings)
                
                if (enabled) {
                    _settingsFeedback.value = "🔒 Auto-lock journal enabled (5 minutes)"
                } else {
                    _settingsFeedback.value = "🔓 Auto-lock journal disabled"
                }
                
                // Clear feedback after 3 seconds
                kotlinx.coroutines.delay(3000)
                _settingsFeedback.value = null
            } catch (e: Exception) {
                // Revert on error
                _settings.value = _settings.value.copy(autoLockJournal = !enabled)
                e.printStackTrace()
            }
        }
    }

    fun updateDailyGoal(hours: Int) {
        val updatedSettings = _settings.value.copy(dailyGoalHours = hours)
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
                _exportState.value = ExportState.Exporting
                
                // Get actual data from database
                val startDate = java.time.LocalDate.now().minusDays(365) // Last year of data
                val endDate = java.time.LocalDate.now()
                
                val hourLogs = hourLogDao.getHourLogsInRange(startDate, endDate).first()
                val dailySummaries = dailySummaryDao.getDailySummariesInRange(startDate, endDate).first()
                val journalEntries = journalDao.getAllEntries()
                
                // Check if we have data to export
                if (hourLogs.isEmpty() && dailySummaries.isEmpty() && journalEntries.isEmpty()) {
                    _exportState.value = ExportState.Error("No data available to export")
                    return@launch
                }
                
                val shareIntent = when (_settings.value.exportFormat) {
                    "CSV" -> dataExporter.exportToCSV(hourLogs, dailySummaries, journalEntries)
                    "JSON" -> dataExporter.exportToJSON(hourLogs, dailySummaries, journalEntries)
                    else -> dataExporter.exportToCSV(hourLogs, dailySummaries, journalEntries)
                }
                
                if (shareIntent != null) {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(Intent.createChooser(shareIntent, "Export Data"))
                    _exportState.value = ExportState.Success("Data exported successfully!")
                } else {
                    _exportState.value = ExportState.Error("Failed to create export file")
                }
                
                // Reset state after a delay
                kotlinx.coroutines.delay(3000)
                _exportState.value = ExportState.Idle
                
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("Export failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    sealed class ExportState {
        object Idle : ExportState()
        object Exporting : ExportState()
        data class Success(val message: String) : ExportState()
        data class Error(val message: String) : ExportState()
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                hourLogDao.deleteAllHourLogs()
                dailySummaryDao.deleteAllDailySummaries()
                journalDao.deleteAllEntries()
                // Reset settings to default
                _settings.value = Settings()
                settingsDao.insert(_settings.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Check if biometric authentication is available
    fun isBiometricAvailable(): Boolean {
        return try {
            val biometricManager = androidx.biometric.BiometricManager.from(context)
            biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
    
    // Get biometric status for UI feedback
    fun getBiometricStatus(): String {
        return if (isBiometricAvailable()) {
            "Available"
        } else {
            "Not available"
        }
    }
} 