package com.example.comp2008assignment1

import android.app.Application

class AppStartup : Application() {
    override fun onCreate() {
        super.onCreate()
        ParkRepository.init(this) // reads parks.txt once
    }
}