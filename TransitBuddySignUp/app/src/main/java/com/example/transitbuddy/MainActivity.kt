package com.example.transitbuddy

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// Main Activity for Transit Buddy
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        
        // Page constants
        private const val LANDING_PAGE = 0
        private const val LOGIN_PAGE = 1
        private const val SIGNUP_PAGE = 2
        private const val HOME_PAGE = 3
        
        // Toast duration
        private const val SHORT_DURATION = Toast.LENGTH_SHORT
        private const val LONG_DURATION = Toast.LENGTH_LONG
    }
    
    // Firebase Authentication
    private lateinit var auth: FirebaseAuth
    private val database = Firebase.database.reference
    private lateinit var progressDialog: ProgressDialog
    
    // UI Views that we access frequently
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        initFirebase()
        initProgressDialog()
        
        // Start with landing page first
        showPage(LANDING_PAGE)
    }
    
    override fun onStart() {
        super.onStart()
        checkUserAuthentication()
    }
    
    //region Initialization
    
    private fun initFirebase() {
        auth = Firebase.auth
        // Persistence is already enabled in TransitBuddyApplication
    }
    
    private fun initProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }
    }
    
    //endregion
    
    //region Authentication Logic
    
    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            showPage(HOME_PAGE)
        }
    }
    
    private fun signOut() {
        auth.signOut()
        showToast("Signed out successfully")
        showPage(LANDING_PAGE)
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
                    Log.e("MainActivity", "Auth error", task.exception)
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
            showPage(HOME_PAGE)
        } else {
            auth.signOut()
            showEmailVerificationDialog()
        }
    }
    
    private fun createUser(email: String, fullName: String, password: String, confirmPassword: String) {
        if (!validateSignupInput(email, fullName, password, confirmPassword)) return
        
        showProgress()
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserInfoToDatabase(email, fullName)
                    sendVerificationEmail()
                } else {
                    hideProgress()
                    showToast("Account creation failed: ${task.exception?.message}")
                }
            }
    }
    
    private fun validateSignupInput(email: String, fullName: String, password: String, confirmPassword: String): Boolean {
        when {
            email.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                showToast("Please fill all fields")
                return false
            }
            password != confirmPassword -> {
                showToast("Passwords do not match")
                return false
            }
            !isNetworkAvailable() -> {
                showToast("No internet connection available")
                return false
            }
            else -> return true
        }
    }
    
    private fun sendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { emailTask ->
                hideProgress()
                if (emailTask.isSuccessful) {
                    showVerificationEmailSentDialog()
                    clearSignupFields()
                } else {
                    showToast("Failed to send verification email: ${emailTask.exception?.message}")
                }
            }
    }
    
    private fun resendVerificationEmail() {
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
    }
    
    private fun resetPassword(email: String) {
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }
        
        showProgress()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                hideProgress()
                if (task.isSuccessful) {
                    showToast("Password reset email sent")
                } else {
                    showToast("Failed to send reset email: ${task.exception?.message}")
                }
            }
    }
    
    //endregion
    
    //region Database Operations
    
    private fun saveUserInfoToDatabase(email: String, fullName: String) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userInfo = mapOf(
                "email" to email,
                "fullName" to fullName,
                "createdAt" to System.currentTimeMillis()
            )
            
            database.child("users").child(userId).setValue(userInfo)
                .addOnSuccessListener {
                    // Data saved successfully
                }
                .addOnFailureListener { e ->
                    showToast("Failed to save user info: ${e.message}")
                }
        }
    }
    
    private fun getUserProfile(userId: String, callback: (Map<String, Any>?) -> Unit) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Convert to Map
                    val userData = snapshot.value as? Map<String, Any>
                    callback(userData)
                } else {
                    callback(null)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to get user data: ${error.message}")
                callback(null)
            }
        })
    }
    
    //endregion
    
    //region UI Setup & Navigation
    
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
                setContentView(R.layout.activity_landing) // Replace with actual home page layout when created
                setupHomePage()
            }
        }
        
        setupWindowInsets()
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupHomePage() {
        val currentUser = auth.currentUser
        
        // Change landing page text & buttons for home page functionality
        val titleTextView = findViewById<TextView>(R.id.landing_title)
        if (currentUser != null) {
            updateWelcomeMessage(currentUser.uid, titleTextView)
        }
        
        // Setup profile button
        val loginButton = findViewById<Button>(R.id.btn_login)
        loginButton.text = "My Profile"
        loginButton.setOnClickListener {
            showToast("Profile feature coming soon!")
        }
        
        // Setup sign out button
        val signupButton = findViewById<Button>(R.id.btn_signup)
        signupButton.text = "Sign Out"
        signupButton.setOnClickListener {
            signOut()
        }
        
        // Setup test connection button
        findViewById<Button>(R.id.btn_test_connection)?.setOnClickListener {
            testFirebaseConnection()
        }
    }
    
    private fun updateWelcomeMessage(userId: String, titleTextView: TextView) {
        getUserProfile(userId) { userData ->
            if (userData != null) {
                val fullName = userData["fullName"] as? String ?: "User"
                titleTextView.text = "Welcome, $fullName"
            }
        }
    }
    
    private fun setupLandingButtons() {
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
        
        findViewById<Button>(R.id.btn_signup).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
        
        // Test Firebase Connection button
        findViewById<Button>(R.id.btn_test_connection)?.setOnClickListener {
            testFirebaseConnection()
        }
    }
    
    private fun setupLoginButtons() {
        // Get input fields
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        
        // Setup forgot password functionality
        val forgotPasswordBtn = findViewById<TextView>(R.id.signup_redirect_btn)
        forgotPasswordBtn.setOnClickListener {
            showForgotPasswordDialog()
        }
        
        // Login button
        findViewById<Button>(R.id.login_btn).setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            authenticateUser(email, password)
        }
        
        // Redirect to signup
        findViewById<Button>(R.id.signup_redirect_btn).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
    }
    
    private fun setupSignupButtons() {
        // Get signup input fields
        emailInput = findViewById(R.id.email_input)
        val nameInput = findViewById<EditText>(R.id.name_input)
        passwordInput = findViewById(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmpassword_input)
        
        // Signup button
        findViewById<Button>(R.id.signup_btn).setOnClickListener {
            val email = emailInput.text.toString().trim()
            val fullName = nameInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            
            createUser(email, fullName, password, confirmPassword)
        }
        
        // Redirect to login
        findViewById<Button>(R.id.login_redirect_btn).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
    }
    
    private fun clearSignupFields() {
        val emailInput = findViewById<EditText>(R.id.email_input)
        val nameInput = findViewById<EditText>(R.id.name_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmpassword_input)
        
        emailInput.text.clear()
        nameInput.text.clear()
        passwordInput.text.clear()
        confirmPasswordInput.text.clear()
    }
    
    //endregion
    
    //region Dialogs
    
    private fun showEmailVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Email Verification Required")
            .setMessage("Please verify your email address before logging in. Check your inbox.")
            .setPositiveButton("Resend Email") { _, _ ->
                resendVerificationEmail()
            }
            .setNegativeButton("OK", null)
            .show()
    }
    
    private fun showVerificationEmailSentDialog() {
        AlertDialog.Builder(this)
            .setTitle("Verify Your Email")
            .setMessage("A verification email has been sent to your email address. Please verify before logging in.")
            .setPositiveButton("OK") { _, _ ->
                showPage(LOGIN_PAGE)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_forgot_password, null)
        val emailInput = view.findViewById<EditText>(R.id.email_input)
        
        builder.setView(view)
            .setTitle("Reset Password")
            .setPositiveButton("Reset") { _, _ ->
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
    
    //endregion
    
    //region Utility Methods
    
    private fun testFirebaseConnection() {
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }
        
        showProgress()
        // Test database connectivity
        val testRef = Firebase.database.getReference(".info/connected")
        testRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    showToast("Firebase connection successful!")
                    // Log Firebase details for debugging
                    logFirebaseInfo()
                } else {
                    showToast("Firebase connection issue detected")
                }
                hideProgress()
            }
            
            override fun onCancelled(error: DatabaseError) {
                showToast("Firebase test failed: ${error.message}")
                hideProgress()
            }
        })
    }
    
    private fun logFirebaseInfo() {
        try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: "Not logged in"
            val email = currentUser?.email ?: "No email"
            val isVerified = currentUser?.isEmailVerified ?: false
            val dbURL = Firebase.database.reference.toString()
            
            Log.d(TAG, "===== FIREBASE INFO =====")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Verified: $isVerified")
            Log.d(TAG, "DB URL: $dbURL")
            Log.d(TAG, "=======================")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging Firebase info", e)
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    private fun showToast(message: String, duration: Int = SHORT_DURATION) {
        Toast.makeText(this, message, duration).show()
    }
    
    private fun showProgress() {
        progressDialog.show()
    }
    
    private fun hideProgress() {
        progressDialog.dismiss()
    }
    
}