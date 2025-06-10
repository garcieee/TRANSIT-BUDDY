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
import com.example.transitbuddy_AndroidApp.ui.UserSettingsActivity
import com.example.transitbuddy_AndroidApp.ui.CashInMainMenuActivity
import com.example.transitbuddy_AndroidApp.ui.QRCodeGeneratorActivity
import com.example.transitbuddy_AndroidApp.ui.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainUIActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        setupUI()
        setupBottomNavigation()
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
                        titleText.text = "Dashboard"
                    }
                    .addOnFailureListener {
                        titleText.text = "Dashboard"
                    }
            } else {
                titleText.text = "Dashboard"
            }
            
            // Set up navigation buttons
            findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
                val intent = Intent(this, UserSettingsActivity::class.java)
                startActivity(intent)
            }

            // Set up notification button
            findViewById<ImageButton>(R.id.notification).setOnClickListener {
                Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show()
            }

            // Set up Load button
            findViewById<Button>(R.id.actionButton).setOnClickListener {
                val intent = Intent(this, CashInMainMenuActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    // Already on the home page
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_location -> {
                    val intent = Intent(this, StationsMapActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    
    // Button click handlers that match the android:onClick attributes in the layout
    fun onTransferClick(view: android.view.View) {
        val intent = Intent(this, ExpressSendActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    fun onQRCodeClick(view: android.view.View) {
        val intent = Intent(this, QRCodeGeneratorActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    fun onLoadClick(view: android.view.View) {
        val intent = Intent(this, CashInMainMenuActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    fun onProfileClick(view: android.view.View) {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    fun onLandmarksClick(view: android.view.View) {
        Toast.makeText(this, "Landmarks feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    fun onStationsClick(view: android.view.View) {
        Toast.makeText(this, "Stations feature coming soon!", Toast.LENGTH_SHORT).show()
    }
} 