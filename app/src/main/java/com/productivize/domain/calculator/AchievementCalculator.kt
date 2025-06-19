package com.productivize.domain.calculator

import com.productivize.data.model.HourLog
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

class AchievementCalculator @Inject constructor() {
    
    /**
     * Calculates achievement percentage based on rated hours.
     * Formula: ((sum of ratings - minimum possible) / (maximum possible - minimum possible)) * 100
     * 
     * For example, if user rated 10 hours with ratings [5,5,4,4,3,3,2,2,1,1]:
     * - Sum of ratings = 30
     * - Minimum possible (all 1s) = 10
     * - Maximum possible (all 5s) = 50
     * - Achievement % = ((30 - 10) / (50 - 10)) * 100 = 50%
     */
    fun calculateAchievementPercentage(ratedHours: List<HourLog>): Float {
        if (ratedHours.isEmpty()) return 0f
        
        val ratings = ratedHours.mapNotNull { it.rating }
        if (ratings.isEmpty()) return 0f
        
        val sumOfRatings = ratings.sum()
        val numberOfRatings = ratings.size
        
        // Minimum possible score (all 1s)
        val minPossible = numberOfRatings * 1
        
        // Maximum possible score (all 5s)
        val maxPossible = numberOfRatings * 5
        
        // Calculate achievement percentage
        val achievement = if (maxPossible > minPossible) {
            ((sumOfRatings - minPossible).toFloat() / (maxPossible - minPossible)) * 100
        } else {
            0f
        }
        
        // Round to 1 decimal place and ensure it's between 0 and 100
        return max(0f, achievement.coerceAtMost(100f))
            .let { (it * 10).roundToInt() / 10f }
    }
    
    /**
     * Calculates weighted achievement considering the importance of different hours
     * (e.g., work hours might be weighted more heavily)
     */
    fun calculateWeightedAchievement(
        ratedHours: List<HourLog>,
        hourWeights: Map<Int, Float> = defaultHourWeights()
    ): Float {
        if (ratedHours.isEmpty()) return 0f
        
        var weightedSum = 0f
        var totalWeight = 0f
        
        ratedHours.forEach { hourLog ->
            hourLog.rating?.let { rating ->
                val weight = hourWeights[hourLog.hour] ?: 1f
                weightedSum += rating * weight
                totalWeight += weight
            }
        }
        
        if (totalWeight == 0f) return 0f
        
        val weightedAverage = weightedSum / totalWeight
        // Convert average rating (1-5) to percentage (0-100)
        val achievement = ((weightedAverage - 1) / 4) * 100
        
        return max(0f, achievement.coerceAtMost(100f))
            .let { (it * 10).roundToInt() / 10f }
    }
    
    /**
     * Default hour weights - higher weight for typical productive hours
     */
    private fun defaultHourWeights(): Map<Int, Float> {
        return mapOf(
            // Early morning (5-8 AM): Medium weight
            5 to 0.8f, 6 to 0.9f, 7 to 1.0f, 8 to 1.0f,
            // Morning (9-12 PM): High weight
            9 to 1.2f, 10 to 1.2f, 11 to 1.2f,
            // Lunch hour: Low weight
            12 to 0.7f,
            // Afternoon (1-5 PM): High weight
            13 to 1.1f, 14 to 1.2f, 15 to 1.2f, 16 to 1.1f, 17 to 1.0f,
            // Evening (6-9 PM): Medium weight
            18 to 0.9f, 19 to 0.8f, 20 to 0.8f, 21 to 0.7f,
            // Night (10 PM - 4 AM): Low weight
            22 to 0.6f, 23 to 0.5f, 0 to 0.4f, 1 to 0.3f, 2 to 0.3f, 3 to 0.3f, 4 to 0.4f
        )
    }
    
    /**
     * Calculates productivity trend (positive/negative/neutral)
     */
    fun calculateTrend(currentAchievement: Float, previousAchievement: Float): ProductivityTrend {
        val difference = currentAchievement - previousAchievement
        return when {
            difference > 5 -> ProductivityTrend.IMPROVING
            difference < -5 -> ProductivityTrend.DECLINING
            else -> ProductivityTrend.STABLE
        }
    }
    
    enum class ProductivityTrend {
        IMPROVING,
        DECLINING,
        STABLE
    }
} 