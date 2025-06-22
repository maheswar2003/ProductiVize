package com.productivize.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivize.data.dao.JournalDao
import com.productivize.data.model.JournalEntry
import com.productivize.logic.JournalAssistant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao,
    private val assistant: JournalAssistant
) : ViewModel() {

    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private val _journalEntry = MutableStateFlow(JournalEntry(date = currentDate))
    val journalEntry: StateFlow<JournalEntry> = _journalEntry.asStateFlow()

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
            }
        }
    }

    fun updateWins(text: String) {
        _journalEntry.value = _journalEntry.value.copy(wins = text)
    }

    fun updateChallenges(text: String) {
        _journalEntry.value = _journalEntry.value.copy(challenges = text)
    }

    fun updateGoalsTomorrow(text: String) {
        _journalEntry.value = _journalEntry.value.copy(goalsTomorrow = text)
    }

    fun updateMood(mood: String) {
        _journalEntry.value = _journalEntry.value.copy(moodEmoji = mood)
    }

    fun updateImages(uris: List<String>) {
        _journalEntry.value = _journalEntry.value.copy(imageUris = uris)
    }

    fun updateVoiceNote(path: String?) {
        _journalEntry.value = _journalEntry.value.copy(voiceNotePath = path)
    }

    fun toggleLock() {
        _journalEntry.value = _journalEntry.value.copy(isLocked = !_journalEntry.value.isLocked)
    }

    fun saveEntry() {
        viewModelScope.launch {
            journalDao.insert(_journalEntry.value)
        }
    }
} 