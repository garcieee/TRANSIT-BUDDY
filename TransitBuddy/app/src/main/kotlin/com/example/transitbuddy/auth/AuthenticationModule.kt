package com.example.transitbuddy_AndroidApp.auth

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
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
                            "Security verification failed. Please check your internet connection and try again."
                        else -> "Authentication failed: ${task.exception?.message}"
                    }
                    showToast(errorMessage)
                    Log.e(TAG, "Auth error", task.exception)
                }
            }
    }
    
    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill all fields")
            return false
        }
        return true
    }
    
    private fun handleSuccessfulLogin() {
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this, MainUIActivity::class.java))
            finish()
        } else {
            auth.signOut()
            showEmailVerificationDialog()
        }
    }
    
    private fun createUser(fullName: String, email: String, password: String) {
        if (!validateSignupInput(email, fullName, password)) return
        
        showProgress()
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserInfoToDatabase(fullName, email)
                    sendVerificationEmail()
                } else {
                    hideProgress()
                    showToast("Account creation failed: ${task.exception?.message}")
                }
            }
    }
    
    private fun validateSignupInput(email: String, fullName: String, password: String): Boolean {
        when {
            email.isEmpty() || fullName.isEmpty() || password.isEmpty() -> {
                showToast("Please fill all fields")
                return false
            }
            !isNetworkAvailable() -> {
                showToast("No internet connection available")
                return false
            }
            else -> return true
        }
    }
    
    private fun saveUserInfoToDatabase(fullName: String, email: String) {
        try {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userInfo = mapOf(
                    "email" to email,
                    "fullName" to fullName,
                    "createdAt" to System.currentTimeMillis()
                )
                
                database.getReference("users").child(userId).setValue(userInfo)
                    .addOnSuccessListener {
                        // Data saved successfully
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving user data", e)
                    }
            }
        } catch (e: Exception) {
            showToast("Firebase unavailable: ${e.message}")
        }
    }
    
    private fun sendVerificationEmail() {
        try {
            auth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { emailTask ->
                    hideProgress()
                    if (emailTask.isSuccessful) {
                        showVerificationEmailSentDialog()
                    } else {
                        showToast("Failed to send verification email: ${emailTask.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            hideProgress()
            showToast("Firebase unavailable: ${e.message}")
        }
    }
    
    private fun resendVerificationEmail() {
        try {
            val user = auth.currentUser
            if (user != null && !isNetworkAvailable()) {
                showToast("No internet connection available")
                return
            }
            
            showProgress()
            user?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    hideProgress()
                    if (task.isSuccessful) {
                        showToast("Verification email sent")
                    } else {
                        showToast("Failed to send verification email: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            hideProgress()
            showToast("Firebase unavailable: ${e.message}")
        }
    }
    
    private fun resetPassword(email: String) {
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }
        
        showProgress()
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    hideProgress()
                    if (task.isSuccessful) {
                        showToast("Password reset email sent")
                    } else {
                        showToast("Failed to send reset email: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            hideProgress()
            showToast("Firebase unavailable: ${e.message}")
        }
    }
    
    private fun showEmailVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Email Verification Required")
            .setMessage("Please verify your email address by clicking the link in the verification email we sent you.")
            .setPositiveButton("Resend Email") { _, _ -> resendVerificationEmail() }
            .setNegativeButton("OK", null)
            .show()
    }
    
    private fun showVerificationEmailSentDialog() {
        AlertDialog.Builder(this)
            .setTitle("Verification Email Sent")
            .setMessage("A verification email has been sent to your email address. Please verify your email to complete registration.")
            .setPositiveButton("OK") { _, _ -> 
                setContentView(R.layout.activity_login)
                setupLoginPage()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.email_input)
        
        AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setView(dialogView)
            .setPositiveButton("Reset Password") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    resetPassword(email)
                } else {
                    showToast("Please enter your email")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showToast(message: String, duration: Int = SHORT_DURATION) {
        Toast.makeText(this, message, duration).show()
    }
    
    private fun showProgress() {
        progressDialog.show()
    }
    
    private fun hideProgress() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
    
    private fun checkUserAuthentication() {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.isEmailVerified) {
                startActivity(Intent(this, MainUIActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            showToast("Firebase unavailable: ${e.message}")
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
} 