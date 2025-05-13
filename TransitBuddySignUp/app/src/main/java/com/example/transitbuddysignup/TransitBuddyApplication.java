package com.example.transitbuddysignup;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.example.transitbuddysignup.db.MongoConnector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class with MultiDex support
 */
public class TransitBuddyApplication extends MultiDexApplication {
    private static final String TAG = "TransitBuddyApp";
    private ExecutorService executor;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set up thread pool
        executor = Executors.newFixedThreadPool(2);
        
        // Allow network on main thread during development
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build();
        StrictMode.setThreadPolicy(policy);
        
        // Set up exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception in thread " + thread.getName(), throwable);
        });
        
        // Initialize MongoDB in background
        initMongoDB();
    }
    
    private void initMongoDB() {
        executor.execute(() -> {
            try {
                MongoConnector connector = MongoConnector.getInstance();
                boolean result = connector.initialize(getApplicationContext());
                Log.i(TAG, "MongoDB initialization: " + (result ? "successful" : "failed"));
            } catch (Exception e) {
                Log.e(TAG, "MongoDB initialization error", e);
            }
        });
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            Log.e(TAG, "MultiDex installation failed", e);
        }
    }
    
    @Override
    public void onTerminate() {
        // Clean up resources
        if (executor != null) {
            executor.shutdown();
        }
        
        // Close MongoDB connection
        try {
            MongoConnector.getInstance().close();
        } catch (Exception e) {
            Log.e(TAG, "Error closing MongoDB connection", e);
        }
        
        super.onTerminate();
    }
} 