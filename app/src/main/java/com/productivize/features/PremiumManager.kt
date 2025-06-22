package com.productivize.features

object PremiumManager {
    const val UNLIMITED_EXPORTS = "unlimited_exports"
    const val VOICE_LOGGING = "voice_logging"
    const val ADVANCED_INSIGHTS = "advanced_insights"

    fun isFeatureEnabled(feature: String): Boolean {
        // TODO: Integrate with BillingClientWrapper and RemoteConfig
        return true // For now, all features enabled
    }
} 