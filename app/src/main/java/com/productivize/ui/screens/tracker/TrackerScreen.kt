package com.productivize.ui.screens.tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.productivize.ui.theme.*
import com.productivize.ui.screens.settings.SettingsViewModel
import com.productivize.data.model.PerformanceTrend
import com.productivize.utils.PerformanceUtils
import com.productivize.utils.PerformanceUtils.collectAsStateOptimized
import com.productivize.utils.PerformanceUtils.formatPercentage
import com.productivize.utils.PerformanceUtils.formatRating
import com.productivize.utils.PerformanceUtils.rememberStable
import com.productivize.ui.components.StarRatingBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    navController: NavController,
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Stable references to prevent recomposition
    val stableHourLogs = remember(uiState.hourLogs) { uiState.hourLogs }
    val stableSettings = remember(settings) { settings }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ProductiVize",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                    IconButton(onClick = { viewModel.toggleAdvancedMetrics() }) {
                        Icon(
                            if (uiState.showAdvancedMetrics) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Advanced Metrics"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced Achievement Ring Section
            item(key = "achievement_card") {
                EnhancedAchievementCard(
                    uiState = uiState,
                    onToggleStreak = { viewModel.toggleStreakDetails() },
                    onToggleLive = { viewModel.toggleLiveAnalytics() }
                )
            }
            
            // Advanced Metrics Panel (Expandable)
            if (uiState.showAdvancedMetrics) {
                item(key = "advanced_metrics") {
                    AdvancedMetricsPanel(uiState = uiState)
                }
            }
            
            // Streak Details Panel (Expandable)
            if (uiState.showStreakDetails) {
                item(key = "streak_details") {
                    StreakDetailsPanel(uiState = uiState)
                }
            }
            
            // Live Analytics Panel (Expandable)
            if (uiState.showLiveAnalytics) {
                item(key = "live_analytics") {
                    LiveAnalyticsPanel(uiState = uiState)
                }
            }
            
            // Quick Insights with Enhanced Display
            if (uiState.insights.isNotEmpty()) {
                item(key = "insights") {
                    EnhancedInsightsCard(insights = uiState.insights)
                }
            }
            
            // Date Navigation Section
            item(key = "date_navigation") {
                DateNavigationSection(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { date -> viewModel.navigateToDate(date) }
                )
            }
            
            // Hourly Timeline Header
            item(key = "hours_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rate Your Hours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (uiState.microIntervention.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = uiState.microIntervention,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            // Enhanced Hourly Timeline - optimized with stable keys and stable references
            items(
                items = stableHourLogs,
                key = { hourLog -> hourLog.hour }
            ) { hourLog ->
                OptimizedHourItem(
                    hourLog = hourLog,
                    vibrationEnabled = stableSettings.vibrationEnabled,
                    onRatingClick = { rating ->
                        viewModel.updateHourRating(hourLog.hour, rating)
                    }
                )
            }
        }
    }
    
            // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                selectedDate = uiState.selectedDate,
                onDateSelected = { date ->
                    viewModel.navigateToDate(date)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
}

@Composable
fun EnhancedAchievementCard(
    uiState: AdvancedTrackerUiState,
    onToggleStreak: () -> Unit,
    onToggleLive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Achievement Ring with Enhanced Display
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                AchievementRing(
                    percentage = uiState.achievementPercentage,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${uiState.achievementPercentage.toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                    )
                    Text(
                        text = "Achievement",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.performanceGrade,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enhanced Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Rated Hours",
                    value = "${uiState.ratedHours}",
                    icon = Icons.Default.Schedule
                )
                StatItem(
                    label = "Avg Rating",
                    value = String.format("%.1f", uiState.averageRating),
                    icon = Icons.Default.Star
                )
                StatItem(
                    label = "Peak Hours",
                    value = "${uiState.peakHours}",
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Streak Badge (Clickable)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleStreak() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.streakBadge,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Streak Details",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedMetricsPanel(uiState: AdvancedTrackerUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Advanced Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Performance",
                    value = String.format("%.2f", uiState.performanceIndex),
                    grade = uiState.performanceGrade
                )
                MetricItem(
                    label = "Consistency",
                    value = String.format("%.1f%%", uiState.consistency * 100),
                    grade = uiState.consistencyRating
                )
                MetricItem(
                    label = "Momentum",
                    value = String.format("%.1f%%", uiState.momentum * 100),
                    grade = uiState.momentumLevel
                )
            }
            
            if (uiState.energyPattern.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Energy Pattern: ${uiState.energyPattern.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StreakDetailsPanel(uiState: AdvancedTrackerUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Streak Intelligence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Length: ${uiState.streakLength}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Quality: ${String.format("%.1f%%", uiState.streakQuality * 100)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (uiState.isStreakAtRisk) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "⚠️ At Risk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            
            if (uiState.streakPrediction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.streakPrediction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (uiState.streakMaintenanceTips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 " + uiState.streakMaintenanceTips.first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun LiveAnalyticsPanel(uiState: AdvancedTrackerUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Live Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Trend: ${getTrendEmoji(uiState.currentTrend)} ${uiState.currentTrend}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Energy: ${String.format("%.0f%%", uiState.energyLevel * 100)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Text(
                    text = "Focus: ${uiState.focusDuration}min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            if (uiState.liveSuggestion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.liveSuggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            if (uiState.anomalies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔍 " + uiState.anomalies.first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EnhancedInsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            insights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun OptimizedHourItem(
    hourLog: HourUiState,
    vibrationEnabled: Boolean,
    onRatingClick: (Int) -> Unit
) {
    // Stable references to prevent recomposition
    val containerColor = remember(hourLog.isCurrentHour, hourLog.rating, hourLog.ratingColor) {
        when {
            hourLog.isCurrentHour -> Color(0x4D2196F3) // Primary container with alpha
            hourLog.rating != null -> {
                try {
                    Color(android.graphics.Color.parseColor(hourLog.ratingColor)).copy(alpha = 0.1f)
                } catch (e: Exception) {
                    Color.Transparent
                }
            }
            else -> Color.Transparent
        }
    }
    
    val textWeight = remember(hourLog.isCurrentHour) {
        if (hourLog.isCurrentHour) FontWeight.Bold else FontWeight.Medium
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour display with current hour indicator
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = hourLog.hourDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = textWeight
                    )
                    
                    if (hourLog.isCurrentHour) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Current Hour",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Energy level indicator - only show if different from default
                if (hourLog.energyLevel > 0f && hourLog.energyLevel != 0.5f) {
                    LinearProgressIndicator(
                        progress = { hourLog.energyLevel },
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
                
                // Tags display - optimized for performance
                if (hourLog.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        hourLog.tags.take(3).forEach { tag -> // Limit to 3 tags for performance
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Rating stars - Using StarRatingBar component with stable reference
            StarRatingBar(
                rating = hourLog.rating ?: 0,
                onRatingSelected = onRatingClick,
                vibrationEnabled = vibrationEnabled,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun EnhancedHourItem(
    hourLog: HourUiState,
    vibrationEnabled: Boolean,
    onRatingClick: (Int) -> Unit
) {
    // Legacy component - kept for compatibility, redirects to optimized version
    OptimizedHourItem(hourLog, vibrationEnabled, onRatingClick)
}

// Helper functions
private fun getTrendEmoji(trend: PerformanceTrend): String {
    return when (trend) {
        PerformanceTrend.UPWARD -> "📈"
        PerformanceTrend.DOWNWARD -> "📉"
        PerformanceTrend.STABLE -> "➡️"
        PerformanceTrend.NEUTRAL -> "🔄"
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    grade: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = grade,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Keep existing StatItem, DateNavigationSection, and DatePickerDialog composables unchanged
@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DateNavigationSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDateSelected(selectedDate.minusDays(1)) }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
            }
            
            Text(
                text = "${selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(
                onClick = { onDateSelected(selectedDate.plusDays(1)) }
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDate by remember { mutableStateOf(selectedDate) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Date",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Current date display
                Text(
                    text = currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Quick navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Today button
                    OutlinedButton(
                        onClick = { currentDate = LocalDate.now() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentDate == LocalDate.now()) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                Color.Transparent
                        )
                    ) {
                        Text("Today")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Yesterday button
                    OutlinedButton(
                        onClick = { currentDate = LocalDate.now().minusDays(1) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentDate == LocalDate.now().minusDays(1)) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                Color.Transparent
                        )
                    ) {
                        Text("Yesterday")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentDate = currentDate.minusDays(1) }
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Day",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Week view
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (-3..3).forEach { offset ->
                            val date = currentDate.plusDays(offset.toLong())
                            val isSelected = date == currentDate
                            val isToday = date == LocalDate.now()
                            
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { currentDate = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { currentDate = currentDate.plusDays(1) }
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Day",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(currentDate)
                    onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AchievementRing(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val size = minOf(maxWidth, maxHeight)
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (this.size.minDimension - strokeWidth) / 2
            val startAngle = -90f
            val sweepAngle = (percentage / 100f) * 360f
            
            // Background ring
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                radius = radius,
                style = Stroke(strokeWidth)
            )
            
            // Progress ring with gradient effect
            drawArc(
                color = DeepBlue,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    (this.size.width - radius * 2) / 2,
                    (this.size.height - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Add glow effect for high achievement
            if (percentage >= 80) {
                drawArc(
                    color = DeepBlue.copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        (this.size.width - radius * 2) / 2,
                        (this.size.height - radius * 2) / 2
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(strokeWidth + 10f, cap = StrokeCap.Round)
                )
            }
        }
    }
} 