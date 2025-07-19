package com.productivize.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

class BiometricAuth(private val activity: FragmentActivity) : BiometricAuthInterface {
    
    override fun isAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    override fun authenticate(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val biometricManager = BiometricManager.from(activity)
        
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(onSuccess, onFailure)
            }
            else -> {
                // Fallback or show error
                onFailure()
            }
        }
    }
    
    private fun showBiometricPrompt(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure()
            }
        })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Journal")
            .setSubtitle("Use your biometric credential to unlock your private journal")
            .setNegativeButtonText("Cancel")
            .build()
            
        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun rememberBiometricAuth(): BiometricAuthInterface {
    val context = LocalContext.current
    return remember {
        // Find FragmentActivity by traversing up the context chain
        var currentContext = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is FragmentActivity) {
                return@remember BiometricAuth(currentContext)
            }
            currentContext = currentContext.baseContext
        }
        
        // Fallback implementation for when FragmentActivity is not found
        object : BiometricAuthInterface {
            override fun isAvailable(): Boolean = false
            override fun authenticate(onSuccess: () -> Unit, onFailure: () -> Unit) {
                // Silently fail when biometric auth is not available
                onFailure()
            }
        }
    }
}

interface BiometricAuthInterface {
    fun isAvailable(): Boolean
    fun authenticate(onSuccess: () -> Unit, onFailure: () -> Unit)
} 