package com.example.transitbuddysignup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.transitbuddysignup.R;
import com.example.transitbuddysignup.controller.AuthController;

/**
 * Activity for user login
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private EditText emailInput;
    private EditText passwordInput;
    private CheckBox rememberMeCheckbox;
    private Button loginButton;
    private Button signUpButton;
    private TextView forgotPasswordText;
    
    private AuthController authController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize AuthController
        authController = AuthController.getInstance(getApplicationContext());
        
        // Initialize views
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me);
        loginButton = findViewById(R.id.login_btn);
        signUpButton = findViewById(R.id.signup_redirect_btn);
        forgotPasswordText = findViewById(R.id.forgot_password);
        
        // If already logged in, go to MainActivity
        if (authController.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        
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
                attemptLogin();
            }
        });
        
        // Sign up button click
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sign up
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
        
        // Forgot password click
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast for now
                Toast.makeText(LoginActivity.this, 
                        "Password reset feature coming soon", 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Attempt to login the user
     */
    private void attemptLogin() {
        // Get input values
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        boolean rememberMe = rememberMeCheckbox.isChecked();
        
        // Attempt login
        AuthController.AuthResult result = authController.login(email, password, rememberMe);
        
        if (result.isSuccess()) {
            // Login successful, navigate to main activity
            Log.d(TAG, "Login successful for user: " + email);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Show error message
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
} 