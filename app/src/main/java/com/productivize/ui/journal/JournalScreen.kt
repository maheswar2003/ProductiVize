package com.productivize.ui.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivize.ui.journal.components.MoodSelector
import com.productivize.ui.journal.components.SmartTextFieldSection
import com.productivize.ui.journal.components.VoiceInputButton
import com.productivize.ui.journal.components.AttachmentGallery
import com.productivize.ui.journal.components.AutoContentSection
import com.productivize.security.rememberBiometricAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    val entry by viewModel.journalEntry.collectAsState()
    val scrollState = rememberScrollState()
    val biometricAuth = rememberBiometricAuth()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Journal") },
                actions = { 
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveEntry() }) {
                Icon(Icons.Filled.Save, contentDescription = "Save Journal")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Mood selector
            MoodSelector(currentMood = entry.moodEmoji) { viewModel.updateMood(it) }

            // Auto-generated sections
            AutoContentSection(
                achievements = entry.autoAchievements,
                patterns = entry.autoPatterns
            )

            // User input sections
            SmartTextFieldSection(
                title = "Today's Wins",
                text = entry.wins,
                onTextChange = viewModel::updateWins,
                suggestions = listOf("Completed a big task", "Stayed focused", "Helped a teammate")
            )
            SmartTextFieldSection(
                title = "Challenges Faced",
                text = entry.challenges,
                onTextChange = viewModel::updateChallenges,
                suggestions = listOf("Got distracted", "Felt tired", "Too many meetings")
            )
            SmartTextFieldSection(
                title = "Tomorrow's Goals",
                text = entry.goalsTomorrow,
                onTextChange = viewModel::updateGoalsTomorrow
            )

            // Voice input
            VoiceInputButton { result ->
                viewModel.updateWins(entry.wins + " " + result)
            }

            // Attachments
            AttachmentGallery(uris = entry.imageUris) { newUris ->
                viewModel.updateImages(newUris)
            }
        }
    }
}

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (achievements.isNotBlank()) {
                    Text("Auto Achievements:", style = MaterialTheme.typography.titleSmall)
                    Text(achievements, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                if (patterns.isNotBlank()) {
                    Text("Detected Patterns:", style = MaterialTheme.typography.titleSmall)
                    Text(patterns, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
} 