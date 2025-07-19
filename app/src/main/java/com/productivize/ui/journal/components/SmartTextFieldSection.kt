package com.productivize.ui.journal.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SmartTextFieldSection(
    title: String,
    text: String,
    onTextChange: (String) -> Unit,
    suggestions: List<String> = emptyList(),
    placeholder: String = "Start typing..."
) {
    var showSuggestions by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title and expand/collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = if (isExpanded) "Edit" else "Add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Text field
            AnimatedVisibility(
                visible = isExpanded || text.isNotBlank(),
                enter = androidx.compose.animation.expandVertically(
                    animationSpec = tween(300)
                ) + androidx.compose.animation.fadeIn(
                    animationSpec = tween(300)
                ),
                exit = androidx.compose.animation.shrinkVertically(
                    animationSpec = tween(300)
                ) + androidx.compose.animation.fadeOut(
                    animationSpec = tween(300)
                )
            ) {
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            onTextChange(it)
                            showSuggestions = it.isBlank() && suggestions.isNotEmpty()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(placeholder) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 8
                    )

                    // Suggestions
                    AnimatedVisibility(
                        visible = showSuggestions,
                        enter = androidx.compose.animation.expandVertically(
                            animationSpec = tween(200)
                        ) + androidx.compose.animation.fadeIn(
                            animationSpec = tween(200)
                        ),
                        exit = androidx.compose.animation.shrinkVertically(
                            animationSpec = tween(200)
                        ) + androidx.compose.animation.fadeOut(
                            animationSpec = tween(200)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Quick suggestions:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(suggestions) { suggestion ->
                                    SuggestionChip(
                                        onClick = {
                                            onTextChange(suggestion)
                                            showSuggestions = false
                                        },
                                        label = { Text(suggestion) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(onClick: () -> Unit, label: @Composable () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = label,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    )
} 