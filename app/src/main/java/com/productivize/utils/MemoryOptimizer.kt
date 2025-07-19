package com.productivize.utils

import android.app.ActivityManager
import android.content.Context
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Memory optimization utilities for preventing leaks and improving performance
 */
object MemoryOptimizer {
    
    /**
     * Memory pressure monitor
     */
    private var lastMemoryCheck = 0L
    private const val MEMORY_CHECK_INTERVAL = 60_000L // 1 minute
    
    /**
     * Check if memory pressure is high
     */
    fun isMemoryPressureHigh(context: Context): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMemoryCheck < MEMORY_CHECK_INTERVAL) {
            return false
        }
        
        lastMemoryCheck = currentTime
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
        val memoryUsagePercentage = (usedMemory * 100 / memoryInfo.totalMem).toInt()
        
        return memoryUsagePercentage > 80
    }
    
    /**
     * Force garbage collection when needed
     */
    fun clearMemoryIfNeeded(context: Context) {
        if (isMemoryPressureHigh(context)) {
            System.gc()
        }
    }
}

/**
 * Memory-optimized data structures
 */
object OptimizedDataStructures {
    
    /**
     * Memory-efficient map with automatic cleanup
     */
    class WeakCache<K, V> {
        private val cache = ConcurrentHashMap<K, WeakReference<V>>()
        
        operator fun get(key: K): V? {
            return cache[key]?.get()
        }
        
        operator fun set(key: K, value: V) {
            cache[key] = WeakReference(value)
        }
        
        fun clear() {
            cache.clear()
        }
    }
} 