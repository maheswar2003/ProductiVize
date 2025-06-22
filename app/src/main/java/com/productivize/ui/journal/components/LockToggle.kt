package com.productivize.ui.journal.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun LockToggle(isLocked: Boolean, onToggle: () -> Unit) {
    val icon = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen
    val description = if (isLocked) "Unlock journal" else "Lock journal"
    IconButton(onClick = onToggle) {
        Icon(icon, contentDescription = description)
    }
} 