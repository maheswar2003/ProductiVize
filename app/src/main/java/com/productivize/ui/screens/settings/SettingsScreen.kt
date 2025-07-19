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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val exportState by viewModel.exportState.collectAsState()
    val settingsFeedback by viewModel.settingsFeedback.collectAsState()
    val context = LocalContext.current
    
    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show export feedback
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is SettingsViewModel.ExportState.Success -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is SettingsViewModel.ExportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }
    
    // Show settings feedback
    LaunchedEffect(settingsFeedback) {
        settingsFeedback?.let { feedback ->
            snackbarHostState.showSnackbar(feedback)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Dark Mode",
                        subtitle = "Switch between light and dark theme",
                        trailing = {
                            val hapticFeedback = LocalHapticFeedback.current
                            Switch(
                                checked = settings.darkMode,
                                onCheckedChange = { 
                                    // Provide immediate haptic feedback
                                    try {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (e: Exception) {
                                        // Haptic feedback failed, continue without it
                                    }
                                    viewModel.updateDarkMode(it) 
                                }
                            )
                        }
                    )
                }
            }

            // Notifications Section (Simplified)
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
                        subtitle = if (settings.biometricLockEnabled) {
                            "🔒 Journal secured with biometric authentication"
                        } else {
                            if (viewModel.isBiometricAvailable()) {
                                "🔓 Tap to secure journal with fingerprint/face"
                            } else {
                                "⚠️ Biometric authentication not available"
                            }
                        },
                        trailing = {
                            Switch(
                                checked = settings.biometricLockEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && !viewModel.isBiometricAvailable()) {
                                        // Show error or disable the switch
                                        // For now, just don't enable it
                                        return@Switch
                                    }
                                    viewModel.updateBiometricLock(enabled)
                                }
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Auto-Lock Journal",
                        subtitle = if (settings.autoLockJournal) {
                            "🔒 Journal auto-locks after 5 minutes"
                        } else {
                            "🔓 Journal stays unlocked"
                        },
                        trailing = {
                            Switch(
                                checked = settings.autoLockJournal,
                                onCheckedChange = viewModel::updateAutoLockJournal
                            )
                        }
                    )
                }
            }

            // Data Management Section
            item {
                SettingsSection(title = "Data Management") {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Export Data",
                        subtitle = when (exportState) {
                            is SettingsViewModel.ExportState.Exporting -> "Exporting data..."
                            else -> "Download your productivity data as ${settings.exportFormat}"
                        },
                        onClick = { 
                            if (exportState !is SettingsViewModel.ExportState.Exporting) {
                                viewModel.exportData() 
                            }
                        },
                        trailing = {
                            if (exportState is SettingsViewModel.ExportState.Exporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All Data",
                        subtitle = "Permanently delete all your data",
                        onClick = { showClearDataDialog = true }
                    )
                }
            }

            // Productivity Section
            item {
                SettingsSection(title = "Productivity") {
                    SettingsItem(
                        icon = Icons.Default.Flag,
                        title = "Daily Goal",
                        subtitle = "Set your daily productivity target: ${settings.dailyGoalHours} hours",
                        onClick = { showDailyGoalDialog = true }
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