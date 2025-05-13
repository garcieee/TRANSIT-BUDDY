package com.example.transitbuddysignup.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading environment variables from env.properties file
 */
public class EnvironmentConfig {
    private static Properties properties;
    private static boolean isInitialized = false;

    /**
     * Initialize the environment configuration
     * @param context Application context
     * @throws IOException if the env.properties file cannot be read
     */
    public static void initialize(Context context) throws IOException {
        if (isInitialized) return;
        
        properties = new Properties();
        
        try (InputStream inputStream = context.getAssets().open("env.properties")) {
            properties.load(inputStream);
            isInitialized = true;
        }
    }

    /**
     * Get the MongoDB connection string
     * @return MongoDB connection string
     */
    public static String getMongoDbConnectionString() {
        checkInitialization();
        String mongoHost = properties.getProperty("MONGODB_HOST", "localhost");
        String mongoPort = properties.getProperty("MONGODB_PORT", "27017");
        String mongoDatabase = properties.getProperty("MONGODB_DATABASE", "transitbuddy");
        String mongoUsername = properties.getProperty("MONGODB_USERNAME", "");
        String mongoPassword = properties.getProperty("MONGODB_PASSWORD", "");
        
        if (mongoUsername.isEmpty() || mongoPassword.isEmpty()) {
            return "mongodb://" + mongoHost + ":" + mongoPort;
        } else {
            return "mongodb://" + mongoUsername + ":" + mongoPassword + "@" + 
                   mongoHost + ":" + mongoPort;
        }
    }

    /**
     * Get the MongoDB database name
     * @return MongoDB database name
     */
    public static String getMongoDbDatabaseName() {
        checkInitialization();
        return properties.getProperty("MONGODB_DATABASE", "transitbuddy");
    }

    /**
     * Get the JWT secret for token signing
     * @return JWT secret
     */
    public static String getJwtSecret() {
        checkInitialization();
        return properties.getProperty("JWT_SECRET", "defaultsecret");
    }

    private static void checkInitialization() {
        if (!isInitialized) {
            throw new IllegalStateException("EnvironmentConfig not initialized. Call initialize() first.");
        }
    }
} 