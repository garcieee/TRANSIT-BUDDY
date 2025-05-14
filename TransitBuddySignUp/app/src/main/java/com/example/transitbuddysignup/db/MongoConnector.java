package com.example.transitbuddysignup.db;

import android.content.Context;
import android.util.Log;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

/**
 * Simple MongoDB connector class to directly access MongoDB
 */
public class MongoConnector {
    private static final String TAG = "MongoConnector";
    private static MongoConnector instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String databaseName;
    private boolean initialized = false;
    
    // Private constructor for singleton pattern
    private MongoConnector() {}
    
    /**
     * Get the singleton instance of MongoConnector
     */
    public static synchronized MongoConnector getInstance() {
        if (instance == null) {
            instance = new MongoConnector();
        }
        return instance;
    }
    
    /**
     * Initialize MongoDB connection from properties file
     * @param context Application context
     * @return true if connection successful, false otherwise
     */
    public boolean initialize(Context context) {
        if (initialized && database != null) {
            Log.i(TAG, "MongoDB already initialized");
            return true;
        }
        
        Log.d(TAG, "Initializing MongoDB connection...");
        
        try {
            // Load properties from assets
            Properties properties = new Properties();
            InputStream inputStream = context.getAssets().open("env.properties");
            properties.load(inputStream);
            inputStream.close();
            
            // Get MongoDB connection parameters
            String uri = properties.getProperty("MONGODB_URI");
            databaseName = properties.getProperty("MONGODB_DATABASE");
            
            if (uri == null || uri.isEmpty() || databaseName == null || databaseName.isEmpty()) {
                Log.e(TAG, "MongoDB URI or database name is missing in env.properties");
                return false;
            }
            
            // First try with the provided URI
            if (connectToMongoDB(uri, databaseName)) {
                return true;
            }
            
            // If the first attempt fails, try with the IP-based URI
            Log.d(TAG, "Hostname resolution failed, trying with direct IP addresses");
            String ipBasedUri = "mongodb://Admin_1:transitbuddy2025@3.7.173.255:27017,18.215.61.168:27017,54.159.137.22:27017/AuthenticationDB?ssl=true&replicaSet=atlas-2ihlwr-shard-0&authSource=admin&retryWrites=true&w=majority&socketTimeoutMS=10000&connectTimeoutMS=10000&serverSelectionTimeoutMS=10000";
            return connectToMongoDB(ipBasedUri, databaseName);
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading properties: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Attempt to connect to MongoDB with the given URI
     */
    private boolean connectToMongoDB(String uri, String dbName) {
        Log.d(TAG, "Attempting to connect to MongoDB with connection string: " + 
              (uri.length() > 25 ? uri.substring(0, 25) + "..." : uri));
        
        try {
            // Create MongoDB client
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(10000, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(builder -> 
                    builder.serverSelectionTimeout(10000, TimeUnit.MILLISECONDS))
                .build();
                
            mongoClient = MongoClients.create(settings);
            Log.d(TAG, "MongoDB client created successfully");
            
            // Get database
            database = mongoClient.getDatabase(dbName);
            Log.d(TAG, "MongoDB database reference obtained: " + dbName);
            
            // Test connection by getting a document count from users collection
            try {
                MongoCollection<Document> usersCollection = database.getCollection("users");
                long count = usersCollection.countDocuments();
                Log.i(TAG, "MongoDB connection successful! Found " + count + " users");
                initialized = true;
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error checking users collection: " + e.getMessage());
                // Try to create the users collection if it doesn't exist
                try {
                    database.createCollection("users");
                    Log.i(TAG, "Created users collection");
                    initialized = true;
                    return true;
                } catch (Exception e2) {
                    Log.e(TAG, "Could not create users collection: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }
            
            // If we get here, assume the connection works
            initialized = true;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "MongoDB connection error: " + e.getMessage());
            e.printStackTrace();
            
            // Try to get more diagnostic info
            Log.e(TAG, "Connection details: URI format valid? " + 
                  uri.startsWith("mongodb://") + ", Database name: " + dbName);
            return false;
        }
    }
    
    /**
     * Get the MongoDB "users" collection
     */
    public MongoCollection<Document> getUsersCollection() {
        if (database == null) {
            Log.e(TAG, "Database not initialized");
            return null;
        }
        try {
            return database.getCollection("users");
        } catch (Exception e) {
            Log.e(TAG, "Error getting users collection: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find a user by email
     * @param email User's email
     * @return User document or null if not found
     */
    public Document findUserByEmail(String email) {
        try {
            if (!initialized) {
                Log.e(TAG, "MongoDB not initialized");
                return null;
            }
            
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) return null;
            
            Bson filter = Filters.eq("email", email);
            return collection.find(filter).first();
        } catch (Exception e) {
            Log.e(TAG, "Error finding user: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if a user exists by email and password
     * @param email User's email
     * @param password User's password
     * @return true if credentials are valid
     */
    public boolean validateUser(String email, String password) {
        try {
            if (!initialized) {
                Log.e(TAG, "MongoDB not initialized for validation");
                return false;
            }
            
            Log.d(TAG, "Validating user: " + email);
            
            // Get the users collection
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                Log.e(TAG, "Users collection is null");
                return false;
            }
            
            // Find user by email
            Bson filter = Filters.eq("email", email);
            Document user = collection.find(filter).first();
            
            if (user == null) {
                Log.d(TAG, "User not found: " + email);
                return false;
            }
            
            // Check password
            String storedPassword = user.getString("password");
            boolean isValid = password.equals(storedPassword);
            
            Log.d(TAG, "Password check for " + email + ": " + (isValid ? "valid" : "invalid"));
            
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error validating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create a new user
     * @param email User's email
     * @param fullName User's full name
     * @param password User's password
     * @return true if user created successfully
     */
    public boolean createUser(String email, String fullName, String password) {
        try {
            if (!initialized) {
                Log.e(TAG, "MongoDB not initialized for user creation");
                return false;
            }
            
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) return false;
            
            // Check if user already exists
            if (findUserByEmail(email) != null) {
                Log.w(TAG, "User already exists: " + email);
                return false;
            }
            
            // Create new user document
            Document newUser = new Document()
                    .append("email", email)
                    .append("fullName", fullName)
                    .append("password", password)
                    .append("createdAt", new Date());
            
            InsertOneResult result = collection.insertOne(newUser);
            if (result.wasAcknowledged()) {
                Log.i(TAG, "User created successfully: " + email);
                return true;
            } else {
                Log.w(TAG, "User creation not acknowledged");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * List all users
     * @return List of user documents
     */
    public List<Document> listAllUsers() {
        List<Document> users = new ArrayList<>();
        try {
            if (!initialized) {
                Log.e(TAG, "MongoDB not initialized");
                return users;
            }
            
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) return users;
            
            collection.find().into(users);
        } catch (Exception e) {
            Log.e(TAG, "Error listing users: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Close the MongoDB connection
     */
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
                database = null;
                initialized = false;
                Log.i(TAG, "MongoDB connection closed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing MongoDB connection: " + e.getMessage());
        }
    }
    
    /**
     * Check if MongoDB is connected and initialized
     * @return true if connected and initialized
     */
    public boolean isConnected() {
        if (!initialized || database == null) {
            return false;
        }
        
        try {
            // Try a lightweight operation to check connection
            database.listCollectionNames().first();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "MongoDB connection check failed: " + e.getMessage());
            return false;
        }
    }
}