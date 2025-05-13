package com.example.transitbuddysignup

import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.transitbuddysignup.db.MongoConnector
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    
    private val LANDING_PAGE = 0
    private val LOGIN_PAGE = 1
    private val SIGNUP_PAGE = 2
    private val HOME_PAGE = 3
    
    // MongoDB connector
    private lateinit var mongoConnector: MongoConnector
    private val executor = Executors.newSingleThreadExecutor()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Allow network on main thread temporarily during development
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        
        // Get MongoDB connector instance (initialization is handled in Application class)
        mongoConnector = MongoConnector.getInstance()
        
        // Start with landing page first
        showPage(LANDING_PAGE)
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check MongoDB connection if needed
        checkMongoConnection()
    }
    
    override fun onDestroy() {
        // Close MongoDB connection when app is closed
        mongoConnector.close()
        executor.shutdown()
        super.onDestroy()
    }
    
    private fun checkMongoConnection() {
        // Only try to connect if not already initialized (actual init happens in Application class)
        executor.execute {
            try {
                if (mongoConnector.isConnected()) {
                    runOnUiThread {
                        // Only show success message during development
                        // Toast.makeText(this, "MongoDB connection OK", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Database connection error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showPage(page: Int) {
        when (page) {
            LANDING_PAGE -> {
                setContentView(R.layout.activity_landing)
                setupLandingButtons()
            }
            LOGIN_PAGE -> {
                setContentView(R.layout.activity_login)
                setupLoginButtons()
            }
            SIGNUP_PAGE -> {
                setContentView(R.layout.activity_main) // This is the signup layout
                setupSignupButtons()
            }
            HOME_PAGE -> {
                // This would normally load a home page layout
                // For now, we'll just show a toast and go back to landing
                Toast.makeText(this, "Successfully logged in as Admin!", Toast.LENGTH_LONG).show()
                showPage(LANDING_PAGE)
            }
        }
        
        // Set window insets after setting content view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupLandingButtons() {
        // Landing page buttons
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
        
        findViewById<Button>(R.id.btn_signup).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
    }
    
    private fun setupLoginButtons() {
        // Get input fields
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        
        // Login page buttons
        findViewById<Button>(R.id.login_btn).setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            
            // Validate credentials
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Show progress indicator
                Toast.makeText(this, "Checking credentials...", Toast.LENGTH_SHORT).show()
                
                // Check MongoDB for user credentials
                executor.execute {
                    try {
                        val isValid = mongoConnector.validateUser(email, password)
                        runOnUiThread {
                            if (isValid) {
                                // Successful login
                                Toast.makeText(this, "Successfully logged in as $email!", Toast.LENGTH_LONG).show()
                                showPage(HOME_PAGE)
                            } else {
                                // Failed login
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        
        findViewById<Button>(R.id.signup_redirect_btn).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
    }
    
    private fun setupSignupButtons() {
        // Get signup input fields
        val emailInput = findViewById<EditText>(R.id.email_input)
        val nameInput = findViewById<EditText>(R.id.name_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmpassword_input)
        
        // Signup page buttons
        findViewById<Button>(R.id.signup_btn).setOnClickListener {
            val email = emailInput.text.toString().trim()
            val fullName = nameInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            
            // Validate input
            when {
                email.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Show progress indicator
                    Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()
                    
                    // Create user in MongoDB
                    executor.execute {
                        try {
                            val success = mongoConnector.createUser(email, fullName, password)
                            runOnUiThread {
                                if (success) {
                                    // Clear input fields
                                    emailInput.text.clear()
                                    nameInput.text.clear()
                                    passwordInput.text.clear()
                                    confirmPasswordInput.text.clear()
                                    
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
                                    showPage(LOGIN_PAGE)
                                } else {
                                    Toast.makeText(this, "Failed to create account. Email may already be registered.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(this, "Error creating account: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
        
        findViewById<Button>(R.id.login_redirect_btn).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
    }
}