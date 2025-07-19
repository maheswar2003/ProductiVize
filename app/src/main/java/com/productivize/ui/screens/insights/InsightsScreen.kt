package com.productivize.ui.screens.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.productivize.ui.components.WeeklyChart
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivize.data.repository.ProductivityRepository
import com.productivize.data.model.DailySummary
import com.productivize.data.model.PerformanceTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: ProductivityRepository
) : ViewModel() {
    
    private val today = LocalDate.now()
    
    // Optimized: Reduced from 6 StateFlows to 2 combined ones
    val summariesData: StateFlow<SummariesData> = combine(
        repository.getWeeklySummaries(),
        repository.getMonthlySummaries()
    ) { weekly, monthly ->
        SummariesData(
            weeklySummaries = weekly,
            monthlySummaries = monthly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SummariesData(emptyList(), emptyList())
    )
    
    // Optimized: Single analytics call instead of 4 separate ones
    val analyticsData: StateFlow<ProductivityRepository.AnalyticsData?> = repository
        .getAnalyticsData(today)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Optimized: Lazy insights generation only when needed
    fun getPersonalizedInsights(): Flow<List<String>> = flow {
        val insights = repository.getBasicInsightsForDate(today)
        emit(insights)
    }.flowOn(Dispatchers.IO)
    
    // Force refresh analytics data
    fun refreshAnalyticsData() {
        viewModelScope.launch {
            try {
                repository.refreshAnalyticsData(today)
            } catch (e: Exception) {
                println("Error refreshing analytics data: ${e.message}")
            }
        }
    }
    
    // Force refresh insights data
    fun refreshInsightsData() {
        viewModelScope.launch {
            try {
                // Force recalculation of daily summary to ensure fresh insights
                repository.recalculateDailySummary(today)
            } catch (e: Exception) {
                println("Error refreshing insights data: ${e.message}")
            }
        }
    }
    
    // Performance data class
    data class SummariesData(
        val weeklySummaries: List<DailySummary>,
        val monthlySummaries: List<DailySummary>
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val summariesData by viewModel.summariesData.collectAsState()
    val analyticsData by viewModel.analyticsData.collectAsState()
    val personalizedInsights by viewModel.getPersonalizedInsights().collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Advanced Insights") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (summariesData.weeklySummaries.isEmpty() && analyticsData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🚀",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start tracking to unlock advanced insights",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your productivity hours to see AI-powered analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Analytics Card
                item {
                    analyticsData?.let { analytics ->
                        LiveAnalyticsCard(analytics.liveAnalytics)
                    }
                }
                
                // Streak Analysis Card
                item {
                    analyticsData?.let { analytics ->
                        StreakAnalysisCard(analytics.streakAnalysis)
                    }
                }
                
                // Weekly Analytics Card
                item {
                    analyticsData?.let { analytics ->
                        WeeklyAnalyticsCard(analytics.weeklyAnalytics)
                    }
                }
                
                // Personalized Insights Card
                item {
                    if (personalizedInsights.isNotEmpty()) {
                        PersonalizedInsightsCard(personalizedInsights)
                    }
                }
                
                // Weekly Chart
                item {
                    if (summariesData.weeklySummaries.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📈 Weekly Trend",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                WeeklyChart(
                                    dailySummaries = summariesData.weeklySummaries.takeLast(7),
                                    modifier = Modifier.height(200.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveAnalyticsCard(analytics: ProductivityRepository.LiveAnalyticsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Live Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = when (analytics.currentTrend) {
                        PerformanceTrend.UPWARD -> "📈 Improving"
                        PerformanceTrend.DOWNWARD -> "📉 Declining"
                        PerformanceTrend.STABLE -> "➡️ Stable"
                        else -> "🔄 Neutral"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(analytics.energyLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Energy Level",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${analytics.focusDuration}m",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Focus Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun StreakAnalysisCard(streak: ProductivityRepository.StreakAnalysisData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (streak.isStreakAtRisk) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 Streak Analysis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (streak.isStreakAtRisk) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                if (streak.isStreakAtRisk) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Streak at risk",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${streak.streakLength}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (streak.isStreakAtRisk) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Day Streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (streak.isStreakAtRisk) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                if (streak.isStreakAtRisk) {
                    Column {
                        Text(
                            text = "⚠️ At Risk",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Stay focused today!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyAnalyticsCard(weekly: ProductivityRepository.WeeklyAnalyticsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Weekly Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${weekly.averageAchievement.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Achievement",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${weekly.consistency.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Consistency",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (weekly.recommendations.isNotEmpty()) {
                Text(
                    text = "💡 Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                weekly.recommendations.forEach { recommendation ->
                    Text(
                        text = "• $recommendation",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalizedInsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Today's Insights",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            insights.forEach { insight ->
                Text(
                    text = "• $insight",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
} 