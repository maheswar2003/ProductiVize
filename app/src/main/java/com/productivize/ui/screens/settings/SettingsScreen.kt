package com.productivize.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.productivize.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    
    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
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
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Dark Mode",
                        subtitle = "Switch between light and dark theme",
                        trailing = {
                            Switch(
                                checked = settings.darkMode,
                                onCheckedChange = { viewModel.updateDarkMode(it) }
                            )
                        }
                    )
                }
            }

            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Enable Notifications",
                        subtitle = "Get reminders to track your productivity",
                        trailing = {
                            Switch(
                                checked = settings.notificationsEnabled,
                                onCheckedChange = viewModel::updateNotificationsEnabled
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Hourly Reminders",
                        subtitle = "Remind me to rate each hour",
                        trailing = {
                            Switch(
                                checked = settings.hourlyReminders,
                                onCheckedChange = viewModel::updateHourlyReminders
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        title = "Journal Reminders",
                        subtitle = "Daily reflection reminder at ${settings.notificationTime}",
                        onClick = { showTimePickerDialog = true },
                        trailing = {
                            Switch(
                                checked = settings.journalReminders,
                                onCheckedChange = viewModel::updateJournalReminders
                            )
                        }
                    )
                }
            }

            // Privacy & Security Section
            item {
                SettingsSection(title = "Privacy & Security") {
                    SettingsItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Secure your journal with fingerprint/face",
                        trailing = {
                            Switch(
                                checked = settings.biometricLockEnabled,
                                onCheckedChange = viewModel::updateBiometricLock
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Auto-Lock Journal",
                        subtitle = "Lock journal after 5 minutes of inactivity",
                        trailing = {
                            Switch(
                                checked = settings.autoLockJournal,
                                onCheckedChange = viewModel::updateAutoLockJournal
                            )
                        }
                    )
                }
            }

            // Data & Backup Section
            item {
                SettingsSection(title = "Data & Backup") {
                    SettingsItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Auto Backup",
                        subtitle = "Automatically backup your data",
                        trailing = {
                            Switch(
                                checked = settings.autoBackupEnabled,
                                onCheckedChange = viewModel::updateAutoBackup
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Export Data",
                        subtitle = "Download your productivity data as ${settings.exportFormat}",
                        onClick = { viewModel.exportData() }
                    )
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All Data",
                        subtitle = "Permanently delete all your data",
                        onClick = { 
                            // TODO: Show confirmation dialog
                            showClearDataDialog = true 
                        }
                    )
                }
            }

            // Goals & Productivity Section
            item {
                SettingsSection(title = "Goals & Productivity") {
                    SettingsItem(
                        icon = Icons.Default.Flag,
                        title = "Daily Goal",
                        subtitle = "Set your daily productivity target: ${settings.dailyGoalHours} hours",
                        onClick = { showDailyGoalDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        title = "Achievement Threshold",
                        subtitle = "Minimum rating for achievement: ${settings.achievementThreshold} stars",
                        onClick = { showThresholdDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Vibration,
                        title = "Vibration Feedback",
                        subtitle = "Haptic feedback for interactions",
                        trailing = {
                            Switch(
                                checked = settings.vibrationEnabled,
                                onCheckedChange = { viewModel.updateVibration(it) }
                            )
                        }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "ProductiVize v1.0.0",
                        onClick = { viewModel.showVersionInfo() }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Get help and contact support",
                        onClick = { viewModel.openHelp() }
                    )
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Rate ProductiVize on Google Play",
                        onClick = { viewModel.rateApp() }
                    )
                    SettingsItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Share ProductiVize with friends",
                        onClick = { viewModel.shareApp() }
                    )
                }
            }
        }
    }
    
    // Daily Goal Dialog
    if (showDailyGoalDialog) {
        DailyGoalDialog(
            currentGoal = settings.dailyGoalHours,
            onGoalSelected = { hours ->
                viewModel.updateDailyGoal(hours)
                showDailyGoalDialog = false
            },
            onDismiss = { showDailyGoalDialog = false }
        )
    }
    
    // Achievement Threshold Dialog
    if (showThresholdDialog) {
        AchievementThresholdDialog(
            currentThreshold = settings.achievementThreshold,
            onThresholdSelected = { threshold ->
                viewModel.updateAchievementThreshold(threshold)
                showThresholdDialog = false
            },
            onDismiss = { showThresholdDialog = false }
        )
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        TimePickerDialog(
            currentTime = settings.notificationTime,
            onTimeSelected = { time ->
                viewModel.updateNotificationTime(time)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        ClearDataDialog(
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            },
            onDismiss = { showClearDataDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun DailyGoalDialog(
    currentGoal: Int,
    onGoalSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Productivity Goal") },
        text = {
            Column {
                Text("How many hours do you want to be productive each day?")
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..16).toList()) { hours ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onGoalSelected(hours) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (hours == currentGoal) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$hours",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (hours == currentGoal) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AchievementThresholdDialog(
    currentThreshold: Int,
    onThresholdSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Achievement Threshold") },
        text = {
            Column {
                Text("What's the minimum rating for an hour to count as an achievement?")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { rating ->
                        Card(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onThresholdSelected(rating) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (rating == currentThreshold) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$rating stars",
                                    tint = if (rating == currentThreshold) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selected: $currentThreshold star${if (currentThreshold != 1) "s" else ""} minimum",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimePickerDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonTimes = listOf("18:00", "19:00", "20:00", "21:00", "22:00")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Journal Reminder Time") },
        text = {
            Column {
                Text("When would you like to be reminded to write in your journal?")
                Spacer(modifier = Modifier.height(16.dp))
                
                commonTimes.forEach { time ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onTimeSelected(time) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (time == currentTime) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (time == currentTime) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClearDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear All Data") },
        text = {
            Column {
                Text("Are you sure you want to permanently delete all your data?")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 