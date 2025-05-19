package com.example.transitbuddy_AndroidApp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.google.firebase.auth.FirebaseAuth

class ExpressSendActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_generator)
        
        auth = FirebaseAuth.getInstance()
        setupUI()
    }
    
    private fun setupUI() {
        // Set up navigation buttons
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            onBackPressed()
        }
        
        // Set up toggle buttons
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            // TODO: Switch to scan mode
            Toast.makeText(this, "Switching to scan mode...", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.btnGenerate).setOnClickListener {
            // Already in generate mode
        }
        
        // Set up user info
        val currentUser = auth.currentUser
        findViewById<TextView>(R.id.textUsername).text = "Username: ${currentUser?.email?.split("@")?.get(0) ?: "User"}"
        findViewById<TextView>(R.id.textAccountNumber).text = "Account Number: ${currentUser?.uid?.take(10) ?: "N/A"}"
        findViewById<TextView>(R.id.textUserID).text = "User ID: ${currentUser?.uid?.take(6) ?: "N/A"}"
        
        // Set up bottom navigation
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            // TODO: Navigate to home
        }
        
        findViewById<ImageView>(R.id.nav_qr).setOnClickListener {
            // Already on QR page
        }
        
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            // TODO: Navigate to profile
        }
    }
} 