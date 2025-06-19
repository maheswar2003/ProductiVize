package com.productivize.ui.screens.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivize.data.repository.ProductivityRepository
import com.productivize.data.model.HourLog
import com.productivize.data.model.DailySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class TrackerUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val hourLogs: List<HourUiState> = emptyList(),
    val achievementPercentage: Float = 0f,
    val averageRating: Float = 0f,
    val ratedHours: Int = 0,
    val peakHours: Int = 0,
    val insights: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class HourUiState(
    val hour: Int,
    val hourDisplay: String,
    val rating: Int? = null,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val ratingColor: String = "#E0E0E0"
)

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: ProductivityRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()
    
    init {
        loadDataForDate(LocalDate.now())
    }
    
    private fun loadDataForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedDate = date) }
            
            // Create initial hour logs for the entire day
            val initialHourLogs = (0..23).map { hour ->
                HourUiState(
                    hour = hour,
                    hourDisplay = formatHour(hour)
                )
            }
            _uiState.update { it.copy(hourLogs = initialHourLogs) }
            
            // Collect hour logs from database
            repository.getHourLogsForDate(date).collect { hourLogs ->
                val updatedHourLogs = (0..23).map { hour ->
                    val hourLog = hourLogs.find { it.hour == hour }
                    HourUiState(
                        hour = hour,
                        hourDisplay = formatHour(hour),
                        rating = hourLog?.rating,
                        tags = hourLog?.tags ?: emptyList(),
                        notes = hourLog?.notes,
                        ratingColor = hourLog?.ratingColor ?: "#E0E0E0"
                    )
                }
                _uiState.update { it.copy(hourLogs = updatedHourLogs) }
            }
            
            // Collect daily summary
            repository.getDailySummary(date).collect { summary ->
                if (summary != null) {
                    _uiState.update { state ->
                        state.copy(
                            achievementPercentage = summary.achievementPercentage,
                            averageRating = summary.averageRating,
                            ratedHours = summary.totalHoursRated,
                            peakHours = summary.peakHours.size,
                            insights = summary.insights,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
    
    fun updateHourRating(hour: Int, rating: Int) {
        viewModelScope.launch {
            // Get existing tags and notes if any
            val existingHourLog = _uiState.value.hourLogs.find { it.hour == hour }
            
            repository.saveHourRating(
                hour = hour,
                rating = rating,
                tags = existingHourLog?.tags ?: emptyList(),
                notes = existingHourLog?.notes
            )
            
            // Update UI immediately for better responsiveness
            _uiState.update { state ->
                state.copy(
                    hourLogs = state.hourLogs.map {
                        if (it.hour == hour) {
                            it.copy(
                                rating = rating,
                                ratingColor = getRatingColor(rating)
                            )
                        } else it
                    }
                )
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        if (date != _uiState.value.selectedDate) {
            loadDataForDate(date)
        }
    }
    
    private fun formatHour(hour: Int): String {
        return when (hour) {
            0 -> "12:00 AM"
            in 1..11 -> "$hour:00 AM"
            12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }
    
    private fun getRatingColor(rating: Int): String {
        return when (rating) {
            4, 5 -> "#2EC4B6" // Teal
            3 -> "#FFBF69" // Amber
            1, 2 -> "#FF6B6B" // Coral
            else -> "#E0E0E0" // Gray
        }
    }
} 