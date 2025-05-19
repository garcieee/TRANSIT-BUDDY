package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.example.transitbuddy_AndroidApp.auth.AuthenticationModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainUIActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        // Set up navigation buttons
        findViewById<Button>(R.id.btnTransfer).setOnClickListener {
            val intent = Intent(this, ExpressSendActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnReceive).setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            // TODO: Create ProfileActivity
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLandmarks).setOnClickListener {
            // TODO: Create LandmarksActivity
            Toast.makeText(this, "Landmarks feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStations).setOnClickListener {
            // TODO: Create StationsActivity
            Toast.makeText(this, "Stations feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLoadBalance).setOnClickListener {
            // TODO: Create LoadBalanceActivity
            Toast.makeText(this, "Load Balance feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Set up sign out button
        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            // Navigate back to authentication screen
            val intent = Intent(this, AuthenticationModule::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = database.getReference("users").child(currentUser.uid)
            userRef.get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.child("fullName").getValue(String::class.java)
                    if (fullName != null) {
                        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $fullName"
                    } else {
                        // If fullName is not found, use email as fallback
                        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, ${currentUser.email}"
                    }
                }
                .addOnFailureListener { e ->
                    // If database read fails, use email as fallback
                    findViewById<TextView>(R.id.tvWelcome).text = "Welcome, ${currentUser.email}"
                    Toast.makeText(this, "Could not load user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If no user is logged in, show generic welcome
            findViewById<TextView>(R.id.tvWelcome).text = "Welcome"
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        if (auth.currentUser == null) {
            // If not signed in, launch the AuthenticationModule
            startActivity(Intent(this, AuthenticationModule::class.java))
            finish()
        }
    }
} 