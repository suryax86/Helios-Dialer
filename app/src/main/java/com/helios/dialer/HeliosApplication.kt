package com.helios.dialer


import android.app.Application

class HeliosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // No telemetry, no background analytics SDKs initialized here
    }
}
