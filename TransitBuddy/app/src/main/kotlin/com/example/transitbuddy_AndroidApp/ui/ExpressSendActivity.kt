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
        setContentView(R.layout.express_send)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setupUI()
    }
    
    private fun setupUI() {
        // Set up back button
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            onBackPressed()
        }
        
        // Set up send button
        findViewById<Button>(R.id.sendButton).setOnClickListener {
            Toast.makeText(this, "Send feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Set up user info
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.getReference("users")
                .child(currentUser.uid)
                .child("fullName")
                .get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.getValue(String::class.java)
                    findViewById<TextView>(R.id.balance).text = "0.00" // TODO: Get actual balance
                }
                .addOnFailureListener {
                    findViewById<TextView>(R.id.balance).text = "0.00"
                }
        }
    }
} 