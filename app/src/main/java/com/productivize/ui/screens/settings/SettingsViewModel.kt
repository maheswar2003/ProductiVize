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
    }

    fun updateHourlyReminders(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(hourlyReminders = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
    }

    fun updateJournalReminders(enabled: Boolean) {
        val updatedSettings = _settings.value.copy(journalReminders = enabled)
        _settings.value = updatedSettings
        saveSettings(updatedSettings)
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
                        shareIntent?.let { context.startActivity(Intent.createChooser(it, "Export Data")) }
                    }
                    "JSON" -> {
                        val shareIntent = dataExporter.exportToJSON(hourLogs, dailySummaries, journalEntries)
                        shareIntent?.let { context.startActivity(Intent.createChooser(it, "Export Data")) }
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
} 