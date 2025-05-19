package com.example.transitbuddy

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Main application class for Transit Buddy Sign Up
 */
class TransitBuddyApplication : Application() {
    companion object {
        private const val TAG = "TransitBuddyApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Enable persistence for offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        
        // Set up exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
        }
    }
} 