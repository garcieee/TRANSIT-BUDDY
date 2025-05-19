package com.example.transitbuddy_AndroidApp

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
                    Log.e(TAG, "Error saving user data", e)
                }
        }
    }
    
    //endregion
    
    //region UI Logic
    
    private fun showPage(pageId: Int) {
        val layoutResId = when (pageId) {
            LANDING_PAGE -> R.layout.activity_landing
            LOGIN_PAGE -> R.layout.activity_login
            SIGNUP_PAGE -> R.layout.activity_main
            HOME_PAGE -> R.layout.activity_home
            else -> throw IllegalArgumentException("Unknown page ID: $pageId")
        }
        
        setContentView(layoutResId)
        setupPageUI(pageId)
    }
    
    private fun setupPageUI(pageId: Int) {
        when (pageId) {
            LANDING_PAGE -> setupLandingPage()
            LOGIN_PAGE -> setupLoginPage()
            SIGNUP_PAGE -> setupSignupPage()
            HOME_PAGE -> setupHomePage()
        }
        
        // Common UI setup for all pages
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupLandingPage() {
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
        
        findViewById<Button>(R.id.btn_signup).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
        
        findViewById<Button>(R.id.btn_test_connection).setOnClickListener {
            testFirebaseConnection()
        }
    }
    
    private fun setupLoginPage() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        
        findViewById<Button>(R.id.login_btn).setOnClickListener {
            authenticateUser(emailInput.text.toString().trim(), passwordInput.text.toString())
        }
        
        findViewById<TextView>(R.id.forgot_password).setOnClickListener {
            showForgotPasswordDialog()
        }
        
        findViewById<Button>(R.id.signup_redirect_btn).setOnClickListener {
            showPage(SIGNUP_PAGE)
        }
    }
    
    private fun setupSignupPage() {
        findViewById<Button>(R.id.signup_btn).setOnClickListener {
            val email = findViewById<EditText>(R.id.email_input).text.toString().trim()
            val fullName = findViewById<EditText>(R.id.name_input).text.toString().trim()
            val password = findViewById<EditText>(R.id.password_input).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.confirmpassword_input).text.toString()
            
            createUser(email, fullName, password, confirmPassword)
        }
        
        findViewById<Button>(R.id.login_redirect_btn).setOnClickListener {
            showPage(LOGIN_PAGE)
        }
    }
    
    private fun setupHomePage() {
        val currentUser = auth.currentUser
        findViewById<TextView>(R.id.txt_welcome).text = "Welcome, ${currentUser?.email}"
        
        findViewById<Button>(R.id.btn_sign_out).setOnClickListener {
            signOut()
        }
        
        // Load and display user data
        loadUserData()
    }
    
    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                
                // Update UI with user data
                findViewById<TextView>(R.id.txt_user_details).text = "Name: $fullName\nEmail: $email"
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading user data", error.toException())
                showToast("Failed to load user data")
            }
        })
    }
    
    //endregion
    
    //region Dialogs
    
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
            .setPositiveButton("OK") { _, _ -> showPage(LOGIN_PAGE) }
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
    
    //endregion
    
    //region Helper Methods
    
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
    
    private fun clearSignupFields() {
        findViewById<EditText>(R.id.email_input).text.clear()
        findViewById<EditText>(R.id.name_input).text.clear()
        findViewById<EditText>(R.id.password_input).text.clear()
        findViewById<EditText>(R.id.confirmpassword_input).text.clear()
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
    
    //endregion
    
    //region Firebase Connection Test
    
    private fun testFirebaseConnection() {
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }
        
        showProgress()
        
        // Test Firebase connectivity by attempting to access the database
        val testRef = Firebase.database.getReference(".info/connected")
        testRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hideProgress()
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                showFirebaseConnectionResult(isConnected)
            }
            
            override fun onCancelled(error: DatabaseError) {
                hideProgress()
                showFirebaseConnectionResult(false, error.message)
            }
        })
    }
    
    private fun showFirebaseConnectionResult(isConnected: Boolean, errorMessage: String? = null) {
        val title = if (isConnected) "Connection Successful" else "Connection Failed"
        val message = if (isConnected) {
            "Successfully connected to Firebase!"
        } else {
            "Failed to connect to Firebase. ${errorMessage ?: "Please check your internet connection and Firebase configuration."}"
        }
        
        val icon = if (isConnected) android.R.drawable.ic_dialog_info else android.R.drawable.ic_dialog_alert
        
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(icon)
            .setPositiveButton("OK", null)
            .show()
    }

} 