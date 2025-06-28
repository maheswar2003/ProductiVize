package com.productivize.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivize.data.dao.JournalDao
import com.productivize.data.dao.SettingsDao
import com.productivize.data.model.JournalEntry
import com.productivize.logic.JournalAssistant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao,
    private val settingsDao: SettingsDao,
    private val assistant: JournalAssistant
) : ViewModel() {

    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private val _journalEntry = MutableStateFlow(JournalEntry(date = currentDate))
    val journalEntry: StateFlow<JournalEntry> = _journalEntry.asStateFlow()
    
    private var autoLockJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            journalDao.getEntryByDate(currentDate).collect { entry ->
                entry?.let {
                    _journalEntry.value = it
                } ?: run {
                    // Create entry with auto-generated content
                    try {
                        val achievements = assistant.generateAutoAchievements()
                        val patterns = assistant.detectPatterns()
                        val newEntry = JournalEntry(date = currentDate).copy(
                            autoAchievements = achievements,
                            autoPatterns = patterns
                        )
                        _journalEntry.value = newEntry
                    } catch (e: Exception) {
                        // Fallback entry if auto-generation fails
                        _journalEntry.value = JournalEntry(date = currentDate).copy(
                            autoAchievements = "Ready to capture today's achievements!",
                            autoPatterns = "Building your productivity patterns..."
                        )
                    }
                }
                
                // Start auto-lock timer if setting is enabled and journal is unlocked
                startAutoLockTimer()
            }
        }
    }

    private fun startAutoLockTimer() {
        autoLockJob?.cancel()
        viewModelScope.launch {
            val settings = settingsDao.getSettings()
            if (settings?.autoLockJournal == true && !_journalEntry.value.isLocked) {
                autoLockJob = launch {
                    try {
                        delay(5 * 60 * 1000) // 5 minutes
                        if (!_journalEntry.value.isLocked && isActive) {
                            _journalEntry.value = _journalEntry.value.copy(isLocked = true)
                            saveEntry()
                        }
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        // Timer was cancelled, which is expected behavior
                    }
                }
            }
        }
    }

    private fun resetAutoLockTimer() {
        if (_journalEntry.value.isLocked) return
        autoLockJob?.cancel()
        startAutoLockTimer()
    }

    fun updateWins(text: String) {
        _journalEntry.value = _journalEntry.value.copy(wins = text)
        resetAutoLockTimer()
    }

    fun updateChallenges(text: String) {
        _journalEntry.value = _journalEntry.value.copy(challenges = text)
        resetAutoLockTimer()
    }

    fun updateGoalsTomorrow(text: String) {
        _journalEntry.value = _journalEntry.value.copy(goalsTomorrow = text)
        resetAutoLockTimer()
    }

    fun updateMood(mood: String) {
        _journalEntry.value = _journalEntry.value.copy(moodEmoji = mood)
        resetAutoLockTimer()
    }

    fun updateImages(uris: List<String>) {
        _journalEntry.value = _journalEntry.value.copy(imageUris = uris)
        resetAutoLockTimer()
    }

    fun updateVoiceNote(path: String?) {
        _journalEntry.value = _journalEntry.value.copy(voiceNotePath = path)
        resetAutoLockTimer()
    }

    fun toggleLock() {
        _journalEntry.value = _journalEntry.value.copy(isLocked = !_journalEntry.value.isLocked)
        if (_journalEntry.value.isLocked) {
            autoLockJob?.cancel()
        } else {
            startAutoLockTimer()
        }
    }

    fun saveEntry() {
        viewModelScope.launch {
            journalDao.insert(_journalEntry.value)
        }
        resetAutoLockTimer()
    }
    
    override fun onCleared() {
        super.onCleared()
        autoLockJob?.cancel()
    }
} 