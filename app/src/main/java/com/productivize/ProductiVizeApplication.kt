package com.productivize

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ProductiVizeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
} 