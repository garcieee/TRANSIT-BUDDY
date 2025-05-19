package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainUIActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val database = Firebase.database.reference
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        auth = FirebaseAuth.getInstance()
        
        // Initialize UI elements
        setupUI()
        loadUserData()
    }
    
    private fun setupUI() {
        // Set up navigation buttons
        findViewById<Button>(R.id.transferButton).setOnClickListener {
            // TODO: Implement transfer functionality
        }
        
        findViewById<Button>(R.id.receiveButton).setOnClickListener {
            // TODO: Implement QR code functionality
        }
        
        findViewById<Button>(R.id.profileButton).setOnClickListener {
            // TODO: Implement profile functionality
        }
        
        findViewById<Button>(R.id.landmarksButton).setOnClickListener {
            // TODO: Implement landmarks functionality
        }
        
        findViewById<Button>(R.id.stationButton).setOnClickListener {
            // TODO: Implement stations functionality
        }
        
        findViewById<Button>(R.id.actionButton).setOnClickListener {
            // TODO: Implement load balance functionality
        }
    }
    
    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                
                // Update UI with user data
                findViewById<TextView>(R.id.titleText).text = "Welcome, $fullName"
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    
    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthenticationModule::class.java))
            finish()
        }
    }
} 