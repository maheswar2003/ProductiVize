package com.productivize.ui.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivize.ui.journal.components.MoodSelector
import com.productivize.ui.journal.components.SmartTextFieldSection
import com.productivize.ui.journal.components.AttachmentGallery
import com.productivize.ui.journal.components.AutoContentSection
import com.productivize.ui.journal.components.LockToggle
import com.productivize.security.rememberBiometricAuth
import com.productivize.ui.screens.settings.SettingsViewModel
import com.productivize.utils.PerformanceUtils.collectAsStateOptimized
import androidx.compose.runtime.Stable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    val entry by viewModel.journalEntry.collectAsStateOptimized(com.productivize.data.model.JournalEntry(date = java.time.LocalDate.now().toString()))
    val saveState by viewModel.saveState.collectAsStateOptimized(JournalViewModel.SaveState.Idle)
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsStateOptimized(com.productivize.data.model.Settings())
    val scrollState = rememberScrollState()
    val biometricAuth = rememberBiometricAuth()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for save feedback
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is JournalViewModel.SaveState.Success -> {
                snackbarHostState.showSnackbar("Journal saved successfully!")
            }
            is JournalViewModel.SaveState.Error -> {
                snackbarHostState.showSnackbar("Failed to save: ${state.message}")
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Daily Journal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = { 
                    if (settings.biometricLockEnabled) {
                        LockToggle(
                            isLocked = entry.isLocked,
                            onToggle = {
                                if (entry.isLocked) {
                                    biometricAuth.authenticate(
                                        onSuccess = { viewModel.toggleLock() },
                                        onFailure = { /* Handle failure */ }
                                    )
                                } else {
                                    viewModel.toggleLock()
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (saveState !is JournalViewModel.SaveState.Saving) {
                        viewModel.saveEntry() 
                    }
                },
                containerColor = when (saveState) {
                    is JournalViewModel.SaveState.Success -> MaterialTheme.colorScheme.tertiary
                    is JournalViewModel.SaveState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                when (saveState) {
                    is JournalViewModel.SaveState.Saving -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    is JournalViewModel.SaveState.Success -> {
                        Icon(Icons.Filled.Check, contentDescription = "Saved")
                    }
                    is JournalViewModel.SaveState.Error -> {
                        Icon(Icons.Filled.Error, contentDescription = "Error")
                    }
                    else -> {
                        Icon(Icons.Filled.Save, contentDescription = "Save Journal")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Mood selector with improved spacing
            MoodSelector(currentMood = entry.moodEmoji) { viewModel.updateMood(it) }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Auto-generated sections
            AutoContentSection(
                achievements = entry.autoAchievements,
                patterns = entry.autoPatterns
            )

            // User input sections with improved placeholders
            SmartTextFieldSection(
                title = "Today's Wins",
                text = entry.wins,
                onTextChange = viewModel::updateWins,
                suggestions = listOf("Completed a big task", "Stayed focused", "Helped a teammate", "Learned something new"),
                placeholder = "What went well today?"
            )
            
            SmartTextFieldSection(
                title = "Challenges Faced",
                text = entry.challenges,
                onTextChange = viewModel::updateChallenges,
                suggestions = listOf("Got distracted", "Felt tired", "Too many meetings", "Technical difficulties"),
                placeholder = "What obstacles did you encounter?"
            )
            
            SmartTextFieldSection(
                title = "Tomorrow's Goals",
                text = entry.goalsTomorrow,
                onTextChange = viewModel::updateGoalsTomorrow,
                placeholder = "What do you want to accomplish tomorrow?"
            )

            // Attachments with improved spacing
            Spacer(modifier = Modifier.height(16.dp))
            AttachmentGallery(uris = entry.imageUris) { newUris ->
                viewModel.updateImages(newUris)
            }
            
            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Stable
@Composable
fun LockToggle(isLocked: Boolean, onToggle: () -> Unit) {
    val icon = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen
    val description = if (isLocked) "Unlock journal" else "Lock journal"
    IconButton(onClick = onToggle) {
        Icon(icon, description)
    }
}

@Composable
fun AutoContentSection(achievements: String, patterns: String) {
    if (achievements.isNotBlank() || patterns.isNotBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (achievements.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto Achievements",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = achievements,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (patterns.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                if (patterns.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detected Patterns",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = patterns,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
} 