package com.example.transitbuddysignup.repository;

import android.util.Log;

import com.example.transitbuddysignup.config.MongoConfig;
import com.example.transitbuddysignup.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Repository for User operations with MongoDB
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String COLLECTION_NAME = "users";
    
    private final MongoCollection<Document> collection;
    
    /**
     * Constructor
     */
    public UserRepository() {
        collection = MongoConfig.getDatabase().getCollection(COLLECTION_NAME);
    }
    
    /**
     * Insert a new user
     * @param user The user to insert
     * @return ObjectId of the inserted user or null if failed
     */
    public ObjectId insertUser(User user) {
        try {
            Document doc = new Document()
                    .append("email", user.getEmail())
                    .append("fullName", user.getFullName())
                    .append("passwordHash", user.getPasswordHash())
                    .append("salt", user.getSalt())
                    .append("createdAt", user.getCreatedAt());
            
            InsertOneResult result = collection.insertOne(doc);
            if (result.wasAcknowledged() && result.getInsertedId() != null) {
                ObjectId id = result.getInsertedId().asObjectId().getValue();
                Log.d(TAG, "User inserted with ID: " + id);
                return id;
            } else {
                Log.e(TAG, "Failed to insert user");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting user", e);
            return null;
        }
    }
    
    /**
     * Find user by email
     * @param email The email to search for
     * @return User if found, null otherwise
     */
    public User findByEmail(String email) {
        try {
            Document doc = collection.find(Filters.eq("email", email)).first();
            if (doc == null) {
                return null;
            }
            
            User user = new User();
            user.setId(doc.getObjectId("_id"));
            user.setEmail(doc.getString("email"));
            user.setFullName(doc.getString("fullName"));
            user.setPasswordHash(doc.getString("passwordHash"));
            user.setSalt(doc.getString("salt"));
            user.setCreatedAt(doc.getLong("createdAt"));
            
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error finding user by email", e);
            return null;
        }
    }
    
    /**
     * Find user by ID
     * @param id The ObjectId to search for
     * @return User if found, null otherwise
     */
    public User findById(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            if (doc == null) {
                return null;
            }
            
            User user = new User();
            user.setId(doc.getObjectId("_id"));
            user.setEmail(doc.getString("email"));
            user.setFullName(doc.getString("fullName"));
            user.setPasswordHash(doc.getString("passwordHash"));
            user.setSalt(doc.getString("salt"));
            user.setCreatedAt(doc.getLong("createdAt"));
            
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error finding user by ID", e);
            return null;
        }
    }
    
    /**
     * Update user information
     * @param user The user to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        try {
            Bson filter = Filters.eq("_id", user.getId());
            Bson updates = Updates.combine(
                    Updates.set("email", user.getEmail()),
                    Updates.set("fullName", user.getFullName())
            );
            
            UpdateResult result = collection.updateOne(filter, updates);
            return result.wasAcknowledged() && result.getModifiedCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user", e);
            return false;
        }
    }
    
    /**
     * Check if a user with the given email exists
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        try {
            return collection.countDocuments(Filters.eq("email", email)) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user exists", e);
            return false;
        }
    }
} 