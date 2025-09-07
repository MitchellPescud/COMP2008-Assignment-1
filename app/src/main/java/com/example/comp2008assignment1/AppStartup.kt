package com.example.comp2008assignment1

import android.app.Application

// Designed to read in parks.txt and create a list of parks within the app upon startup

class AppStartup : Application() {
    override fun onCreate() {
        super.onCreate()
        ParkRepository.init(this) // reads parks.txt once
    }
}