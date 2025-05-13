package com.example.transitbuddysignup.service;

import android.util.Log;

import com.example.transitbuddysignup.model.User;
import com.example.transitbuddysignup.repository.UserRepository;
import com.example.transitbuddysignup.util.PasswordUtils;

import org.bson.types.ObjectId;

/**
 * Service for user-related business logic
 */
public class UserService {
    private static final String TAG = "UserService";
    private final UserRepository userRepository;
    
    /**
     * Constructor
     */
    public UserService() {
        userRepository = new UserRepository();
    }
    
    /**
     * Register a new user
     * @param email User email
     * @param fullName User full name
     * @param password User password
     * @return User object if successful, null if failed
     */
    public User registerUser(String email, String fullName, String password) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                Log.d(TAG, "User with email already exists: " + email);
                return null;
            }
            
            // Generate salt and hash password
            String salt = PasswordUtils.generateSalt();
            String passwordHash = PasswordUtils.hashPassword(password, salt);
            
            // Create and save user
            User newUser = new User(email, fullName, passwordHash, salt);
            ObjectId id = userRepository.insertUser(newUser);
            
            if (id != null) {
                newUser.setId(id);
                Log.d(TAG, "User registered successfully: " + email);
                return newUser;
            } else {
                Log.e(TAG, "Failed to register user: " + email);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering user", e);
            return null;
        }
    }
    
    /**
     * Authenticate user
     * @param email User email
     * @param password User password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String email, String password) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                Log.d(TAG, "User not found: " + email);
                return null;
            }
            
            // Verify password
            boolean isPasswordCorrect = PasswordUtils.verifyPassword(
                    password, user.getPasswordHash(), user.getSalt());
            
            if (isPasswordCorrect) {
                Log.d(TAG, "User authenticated successfully: " + email);
                return user;
            } else {
                Log.d(TAG, "Authentication failed for user: " + email);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user", e);
            return null;
        }
    }
    
    /**
     * Get user by ID
     * @param id User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(String id) {
        try {
            return userRepository.findById(new ObjectId(id));
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID", e);
            return null;
        }
    }
    
    /**
     * Get user by email
     * @param email User email
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by email", e);
            return null;
        }
    }
    
    /**
     * Update user information
     * @param user User to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        try {
            return userRepository.updateUser(user);
        } catch (Exception e) {
            Log.e(TAG, "Error updating user", e);
            return false;
        }
    }
} 