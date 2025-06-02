package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ExpressSendActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_generator)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setupUI()
    }
    
    private fun setupUI() {
        // Set up navigation buttons
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            val intent = Intent(this, UserSettingsActivity::class.java)
            startActivity(intent)
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
        
        val textAccountNumber = findViewById<TextView>(R.id.textAccountNumber)
        textAccountNumber.text = "Name: Loading..."
        
        if (currentUser != null) {
            database.getReference("users")
                .child(currentUser.uid)
                .child("fullName")
                .get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.getValue(String::class.java)
                    textAccountNumber.text = "Name: ${fullName ?: "User"}"
                }
                .addOnFailureListener {
                    textAccountNumber.text = "Name: User"
                }
        } else {
            textAccountNumber.text = "Name: User"
        }
        
        findViewById<TextView>(R.id.textUserID).text = "User ID: ${currentUser?.uid?.take(6) ?: "N/A"}"
        
        // Set up bottom navigation
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, MainUIActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<ImageView>(R.id.nav_qr).setOnClickListener {
            // Already on QR page, no action needed or can restart for simplicity if desired
            // val intent = Intent(this, QRCodeGeneratorActivity::class.java)
            // startActivity(intent)
        }
        
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Assuming there's a location ImageView with ID nav_location
        // findViewById<ImageView>(R.id.nav_location).setOnClickListener {
        //     Toast.makeText(this, "Location feature coming soon!", Toast.LENGTH_SHORT).show()
        // }
    }
} 