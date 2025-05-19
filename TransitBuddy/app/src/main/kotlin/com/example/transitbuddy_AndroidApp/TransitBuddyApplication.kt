package com.example.transitbuddy_AndroidApp

import android.app.Application
import com.google.firebase.FirebaseApp

class TransitBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 