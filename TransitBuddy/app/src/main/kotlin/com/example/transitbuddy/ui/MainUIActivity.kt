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
        setContentView(R.layout.activity_main_ui)
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        // Set up navigation buttons
        findViewById<Button>(R.id.btnTransfer).setOnClickListener {
            // TODO: Navigate to TransferActivity
            Toast.makeText(this, "Transfer feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnReceive).setOnClickListener {
            // TODO: Navigate to ReceiveActivity
            Toast.makeText(this, "Receive feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            // TODO: Navigate to ProfileActivity
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLandmarks).setOnClickListener {
            // TODO: Navigate to LandmarksActivity
            Toast.makeText(this, "Landmarks feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStations).setOnClickListener {
            // TODO: Navigate to StationsActivity
            Toast.makeText(this, "Stations feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLoadBalance).setOnClickListener {
            // TODO: Navigate to LoadBalanceActivity
            Toast.makeText(this, "Load Balance feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = database.getReference("users").child(currentUser.uid)
            userRef.get().addOnSuccessListener { snapshot ->
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $fullName"
            }
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