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
    
    private fun loadUserCredentials() {
        try {
            // First load default users from assets
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
            
            // Then check for users in internal storage and add them
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
                        
                        // Only add if not already in the list
                        if (users.none { it.email == email }) {
                            users.add(UserCredential(email, fullName, password))
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors reading internal file
                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error loading user credentials", Toast.LENGTH_SHORT).show()
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
            FileWriter(file).use { it.write(jsonObject.toString()) }
            
            // Copy the file to assets directory during next app startup
            // Note: Can't directly write to assets at runtime, this is a workaround
            // A real app would use a database like Room or Firebase
            
            Toast.makeText(this, "User credentials saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving user credentials: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        // Ensure MongoDB is initialized before trying to validate
                        if (!mongoConnector.isConnected()) {
                            val initialized = mongoConnector.initialize(applicationContext)
                            if (!initialized) {
                                runOnUiThread {
                                    Toast.makeText(this, "Could not connect to database. Please restart the app.", Toast.LENGTH_LONG).show()
                                }
                                return@execute
                            }
                        }
                        
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
                            
                            // Clear input fields
                            emailInput.text.clear()
                            nameInput.text.clear()
                            passwordInput.text.clear()
                            confirmPasswordInput.text.clear()
                            
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
                            showPage(LOGIN_PAGE)
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
    
    // Data class to hold user credentials
    data class UserCredential(
        val email: String,
        val fullName: String,
        val password: String
    )
}