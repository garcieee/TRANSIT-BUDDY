package com.example.transitbuddysignup.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.transitbuddysignup.model.User;

/**
 * Manages user session information
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "TransitBuddySession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;
    
    private static SessionManager instance;
    
    /**
     * Get singleton instance of SessionManager
     * @param context Application context
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    /**
     * Save user login session
     * @param user The user to save
     */
    public void createLoginSession(User user) {
        editor.putString(KEY_USER_ID, user.getId().toString());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        
        Log.d(TAG, "User session created for: " + user.getEmail());
    }
    
    /**
     * Check if user is logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Get user details
     * @return User session details
     */
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setEmail(pref.getString(KEY_EMAIL, null));
        user.setFullName(pref.getString(KEY_FULL_NAME, null));
        return user;
    }
    
    /**
     * Get user ID
     * @return User ID or null if not logged in
     */
    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }
    
    /**
     * Clear session and log out user
     */
    public void logout() {
        editor.clear();
        editor.apply();
        
        Log.d(TAG, "User logged out");
    }
} 