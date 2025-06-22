package com.productivize.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

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
                            viewModel.clearAllData() 
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
                        subtitle = "Set your daily productivity target: 8 hours",
                        onClick = { /* TODO: Open goal setting dialog */ }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        title = "Achievement Threshold",
                        subtitle = "Minimum rating for achievement: 3 stars",
                        onClick = { /* TODO: Open threshold setting */ }
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
                        onClick = { /* TODO: Show version info */ }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Get help and contact support",
                        onClick = { /* TODO: Open help */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Rate ProductiVize on Google Play",
                        onClick = { /* TODO: Open Play Store */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Share ProductiVize with friends",
                        onClick = { /* TODO: Open share dialog */ }
                    )
                }
            }
        }
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