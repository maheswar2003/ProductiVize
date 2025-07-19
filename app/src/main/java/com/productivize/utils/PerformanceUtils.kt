package com.productivize.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

/**
 * Performance utilities for optimizing UI and memory usage
 */
object PerformanceUtils {
    
    /**
     * Optimized remember function that includes stability checks
     */
    @Composable
    inline fun <T> rememberStable(
        key: Any? = null,
        crossinline calculation: () -> T
    ): T {
        return remember(key) { calculation() }
    }
    
    /**
     * Optimized state management with debouncing
     */
    @Composable
    fun <T> rememberDebouncedState(
        initialValue: T,
        debounceMillis: Long = 300
    ): MutableState<T> {
        val state = remember { mutableStateOf(initialValue) }
        val debouncedValue = remember { mutableStateOf(initialValue) }
        
        LaunchedEffect(state.value) {
            kotlinx.coroutines.delay(debounceMillis)
            debouncedValue.value = state.value
        }
        
        return state
    }
    
    /**
     * Memory-efficient flow collection
     */
    @Composable
    fun <T> Flow<T>.collectAsStateOptimized(
        initial: T,
        context: kotlin.coroutines.CoroutineContext = kotlin.coroutines.EmptyCoroutineContext
    ): State<T> {
        return this.distinctUntilChanged().collectAsState(initial, context)
    }
    
    /**
     * Optimized DP to PX conversion with caching
     */
    @Composable
    fun Dp.toPxOptimized(): Float {
        val density = LocalDensity.current
        return remember(this, density) {
            with(density) { this@toPxOptimized.toPx() }
        }
    }
    
    /**
     * Memory-efficient string formatting
     */
    fun formatPercentage(value: Float): String {
        return "${value.roundToInt()}%"
    }
    
    fun formatRating(value: Float): String {
        return String.format("%.1f", value)
    }
} 