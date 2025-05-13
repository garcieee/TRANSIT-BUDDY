package com.example.transitbuddysignup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.transitbuddysignup.activity.LoginActivity
import com.example.transitbuddysignup.controller.AuthController

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    private lateinit var emailInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var signupButton: Button
    private lateinit var loginButton: Button
    
    private lateinit var authController: AuthController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Initialize AuthController
        authController = AuthController.getInstance(applicationContext)
        
        // Initialize views
        emailInput = findViewById(R.id.email_input)
        nameInput = findViewById(R.id.name_input)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirmpassword_input)
        signupButton = findViewById(R.id.signup_btn)
        loginButton = findViewById(R.id.login_btn)
        
        // Set up insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Set click listeners
        setupClickListeners()
    }
    
    /**
     * Set up click listeners for buttons
     */
    private fun setupClickListeners() {
        // Signup button click
        signupButton.setOnClickListener {
            attemptSignup()
        }
        
        // Login button click
        loginButton.setOnClickListener {
            // Navigate to login
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    
    /**
     * Attempt to signup the user
     */
    private fun attemptSignup() {
        // Get input values
        val email = emailInput.text.toString().trim()
        val fullName = nameInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()
        
        // Attempt signup
        val result = authController.register(email, fullName, password, confirmPassword)
        
        if (result.isSuccess) {
            // Registration successful, navigate to login activity
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Show error message
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }
}