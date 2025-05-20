package com.example.transitbuddy_AndroidApp.auth

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.example.transitbuddy_AndroidApp.ui.MainUIActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AuthenticationModule : AppCompatActivity() {
    companion object {
        private const val TAG = "AuthenticationModule"
        private const val SHORT_DURATION = Toast.LENGTH_SHORT
        private const val LONG_DURATION = Toast.LENGTH_LONG
    }
    
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase if not already initialized
        if (!FirebaseApp.getApps(this).any()) {
            FirebaseApp.initializeApp(this)
        }
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        // Initialize progress dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }
        
        setContentView(R.layout.activity_landing)
        setupLandingPage()
    }
    
    override fun onStart() {
        super.onStart()
        checkUserAuthentication()
    }
    
    private fun setupLandingPage() {
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            setContentView(R.layout.activity_login)
            setupLoginPage()
        }
        
        findViewById<Button>(R.id.btn_signup).setOnClickListener {
            setContentView(R.layout.activity_signup)
            setupSignupPage()
        }

        // Add Firebase connection test
        findViewById<Button>(R.id.btn_test_connection).setOnClickListener {
            testFirebaseConnection()
        }
    }

    private fun testFirebaseConnection() {
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }

        showProgress()
        try {
            // Create a temporary test user
            auth.createUserWithEmailAndPassword("test@test.com", "test123456")
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        // Try to write to the user's own node
                        val userRef = database.getReference("users").child(user.uid)
                        val testData = mapOf(
                            "email" to "test@test.com",
                            "fullName" to "Test User",
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        userRef.setValue(testData)
                            .addOnSuccessListener {
                                // Clean up: delete the test user
                                user.delete()
                                    .addOnSuccessListener {
                                        hideProgress()
                                        showToast("Firebase connection successful!", LONG_DURATION)
                                    }
                                    .addOnFailureListener { e ->
                                        hideProgress()
                                        showToast("Firebase connection successful, but cleanup failed: ${e.message}")
                                        Log.e(TAG, "User cleanup failed", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                // Clean up: delete the test user
                                user.delete()
                                hideProgress()
                                showToast("Firebase connection failed: ${e.message}")
                                Log.e(TAG, "Firebase connection test failed", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    hideProgress()
                    showToast("Firebase connection failed: ${e.message}")
                    Log.e(TAG, "Firebase connection test failed", e)
                }
        } catch (e: Exception) {
            hideProgress()
            showToast("Firebase connection failed: ${e.message}")
            Log.e(TAG, "Firebase connection test failed", e)
        }
    }
    
    private fun setupLoginPage() {
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_btn)
        val signupLink = findViewById<Button>(R.id.signup_redirect_btn)
        val nameText = findViewById<TextView>(R.id.name_text)
        
        // Add text change listener to email input
        emailInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (email.isNotEmpty()) {
                    // Check if user exists and get their name
                    database.getReference("users")
                        .orderByChild("email")
                        .equalTo(email)
                        .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (userSnapshot in snapshot.children) {
                                        val fullName = userSnapshot.child("fullName").getValue(String::class.java)
                                        if (fullName != null) {
                                            nameText.text = fullName
                                            return
                                        }
                                    }
                                }
                                nameText.text = ""
                            }
                            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                                nameText.text = ""
                            }
                        })
                } else {
                    nameText.text = ""
                }
            }
        })
        
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            authenticateUser(email, password)
        }
        
        signupLink.setOnClickListener {
            setContentView(R.layout.activity_signup)
            setupSignupPage()
        }
    }
    
    private fun setupSignupPage() {
        val fullNameInput = findViewById<EditText>(R.id.name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmpassword_input)
        val signupButton = findViewById<Button>(R.id.signup_btn)
        val loginLink = findViewById<Button>(R.id.login_redirect_btn)
        
        signupButton.setOnClickListener {
            val fullName = fullNameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            
            if (password == confirmPassword) {
                createUser(fullName, email, password)
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
        
        loginLink.setOnClickListener {
            setContentView(R.layout.activity_login)
            setupLoginPage()
        }
    }
    
    private fun authenticateUser(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }
        
        showProgress()
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgress()
                if (task.isSuccessful) {
                    handleSuccessfulLogin()
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("credential is incorrect") == true -> 
                            "Invalid email or password. Please try again."
                        task.exception?.message?.contains("RecaptchaCallWrapper") == true ->
                            "Please try again later. Too many attempts."
                        else -> "Authentication failed: ${task.exception?.message}"
                    }
                    showToast(errorMessage)
                }
            }
    }
    
    private fun createUser(fullName: String, email: String, password: String) {
        if (!validateSignupInput(fullName, email, password)) return
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }
        
        showProgress()
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result.user
                    if (user != null) {
                        // Create user profile in database
                        val userRef = database.getReference("users").child(user.uid)
                        val userData = mapOf(
                            "fullName" to fullName,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                hideProgress()
                                handleSuccessfulLogin()
                            }
                            .addOnFailureListener { e ->
                                hideProgress()
                                showToast("Failed to create user profile: ${e.message}")
                                // Delete the user since profile creation failed
                                user.delete()
                            }
                    }
                } else {
                    hideProgress()
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "This email is already registered. Please login instead."
                        task.exception?.message?.contains("badly formatted") == true ->
                            "Please enter a valid email address."
                        task.exception?.message?.contains("password is too weak") == true ->
                            "Password is too weak. Please use a stronger password."
                        else -> "Failed to create account: ${task.exception?.message}"
                    }
                    showToast(errorMessage)
                }
            }
    }
    
    private fun handleSuccessfulLogin() {
        try {
            val intent = Intent(this, MainUIActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error during login transition: ${e.message}", e)
            showToast("Error during login. Please try again.")
        }
    }
    
    private fun checkUserAuthentication() {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                handleSuccessfulLogin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking authentication: ${e.message}", e)
        }
    }
    
    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showToast("Please enter your email")
            return false
        }
        if (password.isEmpty()) {
            showToast("Please enter your password")
            return false
        }
        return true
    }
    
    private fun validateSignupInput(fullName: String, email: String, password: String): Boolean {
        if (fullName.isEmpty()) {
            showToast("Please enter your full name")
            return false
        }
        if (email.isEmpty()) {
            showToast("Please enter your email")
            return false
        }
        if (password.isEmpty()) {
            showToast("Please enter a password")
            return false
        }
        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return false
        }
        return true
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    private fun showProgress() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    }
    
    private fun hideProgress() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
    
    private fun showToast(message: String, duration: Int = SHORT_DURATION) {
        Toast.makeText(this, message, duration).show()
    }
} 