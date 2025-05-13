package com.example.transitbuddysignup

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {
    
    private val LANDING_PAGE = 0
    private val LOGIN_PAGE = 1
    private val SIGNUP_PAGE = 2
    private val HOME_PAGE = 3
    
    // User credentials
    private var users: MutableList<UserCredential> = mutableListOf()
    
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
            val usersList = mutableListOf<UserCredential>()
            
            // First check for users in internal storage
            val internalFile = File(filesDir, "users.json")
            if (internalFile.exists()) {
                try {
                    val internalJsonString = internalFile.readText()
                    val internalJsonObject = JSONObject(internalJsonString)
                    val internalUsersArray = internalJsonObject.getJSONArray("users")
                    
                    for (i in 0 until internalUsersArray.length()) {
                        val userObj = internalUsersArray.getJSONObject(i)
                        val email = userObj.getString("email")
                        val fullName = userObj.getString("fullName")
                        val password = userObj.getString("password")
                        
                        usersList.add(UserCredential(email, fullName, password))
                    }
                    
                    Toast.makeText(this, "Loaded ${usersList.size} users from local storage", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error reading internal file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If internal file doesn't exist, load default users from assets
                try {
                    val jsonString = assets.open("users.json").bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonString)
                    val usersArray = jsonObject.getJSONArray("users")
                    
                    for (i in 0 until usersArray.length()) {
                        val userObj = usersArray.getJSONObject(i)
                        val email = userObj.getString("email")
                        val fullName = userObj.getString("fullName")
                        val password = userObj.getString("password")
                        
                        usersList.add(UserCredential(email, fullName, password))
                    }
                    
                    // Save the assets users to internal storage for future updates
                    users = usersList
                    saveUserCredentials()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading default users: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            users = usersList
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading user credentials: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveUserCredentials() {
        try {
            // Create JSON object from users list
            val jsonObject = JSONObject()
            val jsonArray = JSONArray()
            
            for (user in users) {
                val userObject = JSONObject()
                userObject.put("email", user.email)
                userObject.put("fullName", user.fullName)
                userObject.put("password", user.password)
                jsonArray.put(userObject)
            }
            
            jsonObject.put("users", jsonArray)
            
            // Create/overwrite the JSON file in internal storage since we can't write to assets directly
            val file = File(filesDir, "users.json")
            val writer = FileWriter(file)
            writer.write(jsonObject.toString())
            writer.flush()
            writer.close()
            
            // Debug message to show file location
            Toast.makeText(this, "User saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving user credentials: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace() // Log the error
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
                // Check against users list (which now includes both asset and internal storage users)
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
                    // Check if email already exists
                    val existingUser = users.find { it.email == email }
                    
                    if (existingUser != null) {
                        Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        // Create new user
                        val newUser = UserCredential(email, fullName, password)
                        
                        try {
                            // Add to in-memory list
                            users.add(newUser)
                            
                            // Save to internal storage
                            saveUserCredentials()
                            
                            // Verify the data was saved
                            verifyUserSaved(email)
                            
                            // Clear input fields
                            emailInput.text.clear()
                            nameInput.text.clear()
                            passwordInput.text.clear()
                            confirmPasswordInput.text.clear()
                            
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
                            showPage(LOGIN_PAGE)
                        } catch (e: Exception) {
                            // Remove from memory if save failed
                            users.remove(newUser)
                            Toast.makeText(this, "Error creating account: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        
        findViewById<Button>(R.id.login_redirect_btn).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
    }
    
    private fun verifyUserSaved(email: String) {
        try {
            // Check if the file exists in internal storage
            val internalFile = File(filesDir, "users.json")
            if (internalFile.exists()) {
                val jsonString = internalFile.readText()
                val jsonObject = JSONObject(jsonString)
                val usersArray = jsonObject.getJSONArray("users")
                
                var found = false
                for (i in 0 until usersArray.length()) {
                    val userObj = usersArray.getJSONObject(i)
                    if (userObj.getString("email") == email) {
                        found = true
                        break
                    }
                }
                
                if (found) {
                    Toast.makeText(this, "Verified: User saved to storage successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Warning: User not found in saved file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Warning: Internal storage file not created", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error verifying user: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Data class to hold user credentials
    data class UserCredential(
        val email: String,
        val fullName: String,
        val password: String
    )
}