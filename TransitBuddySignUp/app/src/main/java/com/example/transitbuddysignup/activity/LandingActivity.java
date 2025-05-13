package com.example.transitbuddysignup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.transitbuddysignup.MainActivity;
import com.example.transitbuddysignup.R;
import com.example.transitbuddysignup.controller.AuthController;

/**
 * Landing activity that serves as the entry point to the app
 */
public class LandingActivity extends AppCompatActivity {
    private Button loginButton;
    private Button signupButton;
    
    private AuthController authController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        
        // Initialize AuthController
        authController = AuthController.getInstance(getApplicationContext());
        
        // Check if user is logged in
        if (authController.isLoggedIn()) {
            // User is already logged in, go to main app activity
            startMainAppActivity();
            finish();
            return;
        }
        
        // Initialize views
        loginButton = findViewById(R.id.btn_login);
        signupButton = findViewById(R.id.btn_signup);
        
        // Set click listeners
        setupClickListeners();
    }
    
    /**
     * Set up click listeners for buttons
     */
    private void setupClickListeners() {
        // Login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login
                startActivity(new Intent(LandingActivity.this, LoginActivity.class));
            }
        });
        
        // Sign up button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sign up
                startActivity(new Intent(LandingActivity.this, MainActivity.class));
            }
        });
    }
    
    /**
     * Start the main app activity
     */
    private void startMainAppActivity() {
        // This would be the main app activity after login
        // For now, just redirect to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
} 