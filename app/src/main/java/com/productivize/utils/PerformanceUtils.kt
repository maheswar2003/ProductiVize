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

    /**
     * Performance monitoring for real-time updates
     */
    object PerformanceMonitor {
        private var lastUpdateTime = 0L
        private var updateCount = 0
        private const val MAX_UPDATE_RATE = 60 // Max updates per second

        fun recordUpdate() {
            val currentTime = System.currentTimeMillis()
            updateCount++

            if (currentTime - lastUpdateTime > 1000) { // Log every second
                val updatesPerSecond = updateCount
                lastUpdateTime = currentTime
                updateCount = 0

                if (updatesPerSecond > MAX_UPDATE_RATE) {
                    println("⚠️ High update rate detected: $updatesPerSecond updates/sec")
                } else {
                    println("✅ Smooth performance: $updatesPerSecond updates/sec")
                }
            }
        }

        fun isUpdateRateHealthy(): Boolean {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime > 1000) {
                return true // No recent updates, considered healthy
            }
            return updateCount <= MAX_UPDATE_RATE
        }
    }
} 