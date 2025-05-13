package com.example.transitbuddysignup

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    
    private val LANDING_PAGE = 0
    private val LOGIN_PAGE = 1
    private val SIGNUP_PAGE = 2
    private val HOME_PAGE = 3
    
    // User credentials
    private var users: List<UserCredential> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Load user credentials from JSON file
        loadUserCredentials()
        
        // Start with landing page
        showPage(LANDING_PAGE)
    }
    
    private fun loadUserCredentials() {
        try {
            val jsonString = assets.open("users.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val usersArray = jsonObject.getJSONArray("users")
            
            val usersList = mutableListOf<UserCredential>()
            
            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                val email = userObj.getString("email")
                val fullName = userObj.getString("fullName")
                val password = userObj.getString("password")
                
                usersList.add(UserCredential(email, fullName, password))
            }
            
            users = usersList
        } catch (e: IOException) {
            Toast.makeText(this, "Error loading user credentials", Toast.LENGTH_SHORT).show()
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
                val user = users.find { it.email == email && it.password == password }
                if (user != null) {
                    // Successful login
                    Toast.makeText(this, "Successfully logged in as ${user.fullName}!", Toast.LENGTH_LONG).show()
                    showPage(HOME_PAGE)
                } else {
                    // Failed login
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        findViewById<Button>(R.id.signup_redirect_btn).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
    }
    
    private fun setupSignupButtons() {
        // Signup page buttons
        findViewById<Button>(R.id.signup_btn).setOnClickListener {
            // In a real app, we would create a new account here
            // For now, just pretend signup was successful
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
            showPage(LOGIN_PAGE)
        }
        
        // Now we can use the ID for the login button
        findViewById<Button>(R.id.login_redirect_btn).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
    }
    
    // Data class to hold user credentials
    data class UserCredential(
        val email: String,
        val fullName: String,
        val password: String
    )
}