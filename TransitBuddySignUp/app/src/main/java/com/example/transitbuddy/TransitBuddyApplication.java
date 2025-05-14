package com.example.transitbuddy;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Main application class for Transit Buddy Sign Up
 */
public class TransitBuddyApplication extends Application {
    private static final String TAG = "TransitBuddyApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        // Enable persistence for offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        
        // Set up exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception in thread " + thread.getName(), throwable);
        });
    }
} 