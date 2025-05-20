package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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
    }
    
    private fun setupUI() {
        try {
            // Set up welcome message
            val currentUser = auth.currentUser
            val titleText = findViewById<TextView>(R.id.titleText)
            
            // Get user's name from database
            if (currentUser != null) {
                database.getReference("users")
                    .child(currentUser.uid)
                    .child("fullName")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val fullName = snapshot.getValue(String::class.java)
                        titleText.text = "Welcome, ${fullName ?: "User"}!"
                    }
                    .addOnFailureListener {
                        titleText.text = "Welcome, User!"
                    }
            }
            
            // Set up navigation buttons
            findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
                auth.signOut()
                val intent = Intent(this, AuthenticationModule::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    // Button click handlers that match the android:onClick attributes in the layout
    fun onTransferClick(view: android.view.View) {
        val intent = Intent(this, ExpressSendActivity::class.java)
        startActivity(intent)
    }
    
    fun onQRCodeClick(view: android.view.View) {
        val intent = Intent(this, QRCodeScannerActivity::class.java)
        startActivity(intent)
    }
    
    fun onProfileClick(view: android.view.View) {
        Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    fun onLandmarksClick(view: android.view.View) {
        Toast.makeText(this, "Landmarks feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    fun onStationsClick(view: android.view.View) {
        Toast.makeText(this, "Stations feature coming soon!", Toast.LENGTH_SHORT).show()
    }
} 