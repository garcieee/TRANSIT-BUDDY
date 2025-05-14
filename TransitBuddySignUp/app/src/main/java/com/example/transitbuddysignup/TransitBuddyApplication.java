package com.example.transitbuddysignup;

import android.app.Application;
import android.util.Log;

/**
 * Main application class for Transit Buddy Sign Up
 */
public class TransitBuddyApplication extends Application {
    private static final String TAG = "TransitBuddyApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set up exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception in thread " + thread.getName(), throwable);
        });
    }
} 