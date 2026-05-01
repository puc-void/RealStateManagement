package com.example.realstate

import android.app.Application
import com.example.realstate.data.network.PreferenceManager

class RealStateApp : Application() {
    companion object {
        lateinit var preferenceManager: PreferenceManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(applicationContext)
    }
}
