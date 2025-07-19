package com.productivize.ui.screens.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivize.data.repository.ProductivityRepository
import com.productivize.data.model.HourLog
import com.productivize.data.model.PerformanceTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: ProductivityRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _showAdvancedMetrics = MutableStateFlow(false)
    private val _showStreakDetails = MutableStateFlow(false)
    private val _showLiveAnalytics = MutableStateFlow(false)
    
    // Instant rating state for immediate UI feedback
    private val _instantRatings = MutableStateFlow<Map<Int, Int>>(emptyMap())
    
    // Main UI state with instant rating integration
    val uiState: StateFlow<AdvancedTrackerUiState> = combine(
        _selectedDate.flatMapLatest { date ->
            repository.getDailyData(date)
        },
        _showAdvancedMetrics,
        _showStreakDetails,
        _showLiveAnalytics,
        _instantRatings
    ) { dailyData, showAdvanced, showStreak, showLive, instantRatings ->
        createInstantUiState(dailyData, instantRatings).copy(
            showAdvancedMetrics = showAdvanced,
            showStreakDetails = showStreak,
            showLiveAnalytics = showLive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = AdvancedTrackerUiState()
    )

    // Optimized UI state creation with instant ratings
    private fun createInstantUiState(
        dailyData: ProductivityRepository.DailyData,
        instantRatings: Map<Int, Int>
    ): AdvancedTrackerUiState {
        val currentHour = LocalDateTime.now().hour
        val isToday = dailyData.date == LocalDate.now()
        
        // Create hour states with instant ratings applied
        val hourUiStates = (0..23).map { hour ->
            val hourLog = dailyData.hourLogs.find { it.hour == hour }
            val instantRating = instantRatings[hour]
            val effectiveRating = instantRating ?: hourLog?.rating
            
            HourUiState(
                hour = hour,
                hourDisplay = "${hour.toString().padStart(2, '0')}:00",
                rating = effectiveRating,
                tags = hourLog?.tags ?: emptyList(),
                notes = hourLog?.notes ?: "",
                ratingColor = getRatingColor(effectiveRating ?: 0),
                isCurrentHour = hour == currentHour && isToday,
                energyLevel = (effectiveRating ?: 0) / 5f
            )
        }
        
        // Calculate instant metrics including instant ratings
        val allRatedHours = hourUiStates.filter { it.rating != null }
        val instantRatedCount = allRatedHours.size
        val instantAverageRating = if (allRatedHours.isNotEmpty()) {
            allRatedHours.sumOf { it.rating!! }.toFloat() / allRatedHours.size
        } else 0f
        val instantProductiveHours = allRatedHours.count { it.rating!! >= 3 }
        val instantAchievement = if (instantRatedCount > 0) {
            (instantProductiveHours.toFloat() / instantRatedCount) * 100f
        } else 0f
        
        val dailySummary = dailyData.dailySummary
        
        return AdvancedTrackerUiState(
            selectedDate = dailyData.date,
            hourLogs = hourUiStates,
            // Use instant calculations if we have instant ratings, otherwise use summary
            achievementPercentage = if (instantRatings.isNotEmpty()) instantAchievement else (dailySummary?.achievementPercentage ?: 0f),
            ratedHours = if (instantRatings.isNotEmpty()) instantRatedCount else (dailySummary?.totalHoursRated ?: 0),
            averageRating = if (instantRatings.isNotEmpty()) instantAverageRating else (dailySummary?.averageRating ?: 0f),
            peakHours = dailySummary?.peakHours?.size ?: 0,
            insights = dailyData.insights,
            
            // Performance metrics from summary
            performanceIndex = dailySummary?.performanceIndex ?: 0f,
            performanceGrade = dailySummary?.performanceGrade ?: "C",
            consistency = dailySummary?.consistency ?: 0f,
            consistencyRating = dailySummary?.consistencyRating ?: "Variable",
            momentum = dailySummary?.momentum ?: 0f,
            momentumLevel = getMomentumLevel(dailySummary?.momentum ?: 0f),
            energyPattern = dailySummary?.energyPattern ?: "BALANCED",
            
            // Streak info
            streakBadge = "🔥 ${dailySummary?.streakCount ?: 0} day streak",
            streakLength = dailySummary?.streakCount ?: 0,
            streakQuality = 0.8f,
            isStreakAtRisk = (dailySummary?.streakCount ?: 0) > 0 && instantProductiveHours < 3,
            streakPrediction = "Keep going!",
            streakMaintenanceTips = getStreakTips(dailySummary?.streakCount ?: 0),
            
            // Live analytics
            currentTrend = PerformanceTrend.NEUTRAL,
            energyLevel = instantAverageRating / 5f,
            focusDuration = instantRatedCount * 60,
            liveSuggestion = "Keep tracking your hours",
            microIntervention = "",
            anomalies = emptyList()
        )
    }

    // INSTANT rating update - UI updates immediately, database in background
    fun updateHourRating(hour: Int, rating: Int) {
        // 1. INSTANT UI UPDATE - happens immediately
        val currentInstantRatings = _instantRatings.value.toMutableMap()
        currentInstantRatings[hour] = rating
        _instantRatings.value = currentInstantRatings
        
        // 2. Background database update
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDate = _selectedDate.value
                val dateTime = currentDate.atTime(hour, 0)
                val id = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"))

                val updatedLog = HourLog(
                    id = id,
                    dateTime = dateTime,
                    hour = hour,
                    rating = rating,
                    notes = "",
                    tags = emptyList(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Save to database
                repository.updateHourLog(updatedLog)
                
                // Clear instant rating after database update (with delay to ensure smooth UX)
                kotlinx.coroutines.delay(500)
                val updatedInstantRatings = _instantRatings.value.toMutableMap()
                updatedInstantRatings.remove(hour)
                _instantRatings.value = updatedInstantRatings
                
            } catch (e: Exception) {
                // On error, remove the instant rating to show actual state
                val updatedInstantRatings = _instantRatings.value.toMutableMap()
                updatedInstantRatings.remove(hour)
                _instantRatings.value = updatedInstantRatings
                println("Error updating hour rating: ${e.message}")
            }
        }
    }

    fun updateHourNotes(hour: Int, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDate = _selectedDate.value
                val existingLogs = repository.getHourLogsForDate(currentDate).first()
                val existingLog = existingLogs.find { it.hour == hour }
                
                if (existingLog != null) {
                    val updatedLog = existingLog.copy(notes = notes)
                    repository.updateHourLog(updatedLog)
                }
            } catch (e: Exception) {
                println("Error updating hour notes: ${e.message}")
            }
        }
    }

    fun navigateToDate(date: LocalDate) {
        _selectedDate.value = date
        // Clear instant ratings when changing dates
        _instantRatings.value = emptyMap()
    }

    fun toggleAdvancedMetrics() {
        _showAdvancedMetrics.value = !_showAdvancedMetrics.value
    }

    fun toggleStreakDetails() {
        _showStreakDetails.value = !_showStreakDetails.value
    }

    fun toggleLiveAnalytics() {
        _showLiveAnalytics.value = !_showLiveAnalytics.value
    }
    
    // Force recalculation of daily summary for current date
    fun recalculateDailySummary() {
        viewModelScope.launch {
            try {
                repository.recalculateDailySummary(_selectedDate.value)
            } catch (e: Exception) {
                println("Error recalculating daily summary: ${e.message}")
            }
        }
    }

    // Helper functions
    private fun getMomentumLevel(momentum: Float): String {
        return if (momentum > 0.6f) "High" else if (momentum > 0.3f) "Medium" else "Low"
    }

    private fun getStreakTips(streakCount: Int): List<String> {
        return when {
            streakCount < 3 -> listOf("Stay consistent", "Focus on quality")
            streakCount < 7 -> listOf("Build momentum", "Track daily progress")
            else -> listOf("Maintain excellence", "Share your success")
        }
    }

    private fun getRatingColor(rating: Int): String {
        return when (rating) {
            1 -> "#FF5252"
            2 -> "#FF9800"
            3 -> "#FFC107"
            4 -> "#4CAF50"
            5 -> "#2196F3"
            else -> "#E0E0E0"
        }
    }
}

// Optimized UI state data class with stable keys
data class AdvancedTrackerUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val hourLogs: List<HourUiState> = emptyList(),
    val achievementPercentage: Float = 0f,
    val ratedHours: Int = 0,
    val averageRating: Float = 0f,
    val peakHours: Int = 0,
    val insights: List<String> = emptyList(),
    
    // Performance metrics
    val performanceIndex: Float = 0f,
    val performanceGrade: String = "C",
    val consistency: Float = 0f,
    val consistencyRating: String = "Variable",
    val momentum: Float = 0f,
    val momentumLevel: String = "Low",
    val energyPattern: String = "BALANCED",
    
    // Streak info
    val streakBadge: String = "🔥 0 day streak",
    val streakLength: Int = 0,
    val streakQuality: Float = 0f,
    val isStreakAtRisk: Boolean = false,
    val streakPrediction: String = "",
    val streakMaintenanceTips: List<String> = emptyList(),
    
    // Live analytics
    val currentTrend: PerformanceTrend = PerformanceTrend.NEUTRAL,
    val energyLevel: Float = 0f,
    val focusDuration: Int = 0,
    val liveSuggestion: String = "",
    val microIntervention: String = "",
    val anomalies: List<String> = emptyList(),
    
    // UI state
    val showAdvancedMetrics: Boolean = false,
    val showStreakDetails: Boolean = false,
    val showLiveAnalytics: Boolean = false
)

data class HourUiState(
    val hour: Int,
    val hourDisplay: String,
    val rating: Int?,
    val tags: List<String>,
    val notes: String,
    val ratingColor: String,
    val isCurrentHour: Boolean,
    val energyLevel: Float
) 