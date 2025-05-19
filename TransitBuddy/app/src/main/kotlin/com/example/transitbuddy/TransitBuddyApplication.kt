package com.example.transitbuddy_AndroidApp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Main application class for Transit Buddy Sign Up
 */
class TransitBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Initialize Firebase Database with the correct URL
            val database = FirebaseDatabase.getInstance()
            database.setPersistenceEnabled(true)
            
            // Set the database URL explicitly
            database.getReference(".info/connected").keepSynced(true)
            
            Log.d("TransitBuddyApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("TransitBuddyApp", "Firebase initialization failed: ${e.message}")
            e.printStackTrace()
        }
        
        // Set up exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("TransitBuddyApp", "Uncaught exception in thread ${thread.name}", throwable)
        }
    }
} 