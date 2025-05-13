package com.example.transitbuddysignup.config;

import android.content.Context;
import android.util.Log;

import com.example.transitbuddysignup.util.EnvironmentConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;

/**
 * MongoDB configuration and connection management
 */
public class MongoConfig {
    private static final String TAG = "MongoConfig";
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static boolean isInitialized = false;

    /**
     * Initialize MongoDB connection
     * @param context Application context
     * @return true if initialization successful, false otherwise
     */
    public static boolean initialize(Context context) {
        if (isInitialized) return true;
        
        try {
            // Initialize environment configuration
            EnvironmentConfig.initialize(context);
            
            // Get MongoDB connection details
            String connectionString = EnvironmentConfig.getMongoDbConnectionString();
            String databaseName = EnvironmentConfig.getMongoDbDatabaseName();
            
            Log.d(TAG, "Connecting to MongoDB at: " + connectionString);
            
            // Configure MongoDB client
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build();
            
            // Create MongoDB client and get database
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            
            // Test connection
            database.runCommand(com.mongodb.client.model.Filters.exists("_id"));
            
            isInitialized = true;
            Log.i(TAG, "MongoDB connection initialized successfully");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to load environment configuration", e);
            return false;
        } catch (MongoException e) {
            Log.e(TAG, "Failed to connect to MongoDB", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error initializing MongoDB", e);
            return false;
        }
    }

    /**
     * Get MongoDB database instance
     * @return MongoDB database
     */
    public static MongoDatabase getDatabase() {
        if (!isInitialized) {
            throw new IllegalStateException("MongoDB not initialized. Call initialize() first.");
        }
        return database;
    }

    /**
     * Close MongoDB connection
     */
    public static void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                Log.i(TAG, "MongoDB connection closed");
            } catch (Exception e) {
                Log.e(TAG, "Error closing MongoDB connection", e);
            } finally {
                mongoClient = null;
                database = null;
                isInitialized = false;
            }
        }
    }
} 