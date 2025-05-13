package com.example.transitbuddysignup.controller;

import android.content.Context;
import android.util.Log;

import com.example.transitbuddysignup.config.MongoConfig;
import com.example.transitbuddysignup.model.User;
import com.example.transitbuddysignup.service.UserService;
import com.example.transitbuddysignup.util.SessionManager;

/**
 * Controller for authentication operations
 */
public class AuthController {
    private static final String TAG = "AuthController";
    
    private final Context context;
    private final UserService userService;
    private final SessionManager sessionManager;
    
    private static AuthController instance;
    
    /**
     * Get singleton instance of AuthController
     * @param context Application context
     * @return AuthController instance
     */
    public static synchronized AuthController getInstance(Context context) {
        if (instance == null) {
            instance = new AuthController(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private AuthController(Context context) {
        this.context = context;
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance(context);
        
        // Initialize MongoDB connection
        if (!MongoConfig.initialize(context)) {
            Log.e(TAG, "Failed to initialize MongoDB");
        }
    }
    
    /**
     * Register a new user
     * @param email User email
     * @param fullName User full name
     * @param password User password
     * @param confirmPassword Confirm password
     * @return Result message
     */
    public AuthResult register(String email, String fullName, String password, String confirmPassword) {
        try {
            // Validate input
            if (email == null || email.isEmpty()) {
                return new AuthResult(false, "Email is required");
            }
            
            if (fullName == null || fullName.isEmpty()) {
                return new AuthResult(false, "Full name is required");
            }
            
            if (password == null || password.isEmpty()) {
                return new AuthResult(false, "Password is required");
            }
            
            if (!password.equals(confirmPassword)) {
                return new AuthResult(false, "Passwords do not match");
            }
            
            // Register user
            User user = userService.registerUser(email, fullName, password);
            
            if (user != null) {
                return new AuthResult(true, "Registration successful");
            } else {
                return new AuthResult(false, "Email already exists or registration failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering user", e);
            return new AuthResult(false, "Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * Login user
     * @param email User email
     * @param password User password
     * @param rememberMe Remember user
     * @return Result message
     */
    public AuthResult login(String email, String password, boolean rememberMe) {
        try {
            // Validate input
            if (email == null || email.isEmpty()) {
                return new AuthResult(false, "Email is required");
            }
            
            if (password == null || password.isEmpty()) {
                return new AuthResult(false, "Password is required");
            }
            
            // Authenticate user
            User user = userService.authenticateUser(email, password);
            
            if (user != null) {
                // Create session
                sessionManager.createLoginSession(user);
                return new AuthResult(true, "Login successful");
            } else {
                return new AuthResult(false, "Invalid email or password");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging in user", e);
            return new AuthResult(false, "Login failed: " + e.getMessage());
        }
    }
    
    /**
     * Logout user
     */
    public void logout() {
        sessionManager.logout();
    }
    
    /**
     * Check if user is logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    /**
     * Get current user
     * @return User if logged in, null otherwise
     */
    public User getCurrentUser() {
        if (!sessionManager.isLoggedIn()) {
            return null;
        }
        
        String userId = sessionManager.getUserId();
        return userService.getUserById(userId);
    }
    
    /**
     * Class to hold authentication result
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        
        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 