package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, MainUIActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile
                    true
                }
                R.id.nav_transfer -> {
                    val intent = Intent(this, ExpressSendActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stations -> {
                    val intent = Intent(this, StationsMapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_landmarks -> {
                    Toast.makeText(this, "Station Landmarks coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, UserSettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_logout -> {
                    // TODO: Implement logout logic
                    Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        setupUI()
        setupBottomNavigation()
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        val accountNameTextView = findViewById<TextView>(R.id.account_name)

        if (currentUser != null) {
            database.getReference("users")
                .child(currentUser.uid)
                .child("fullName")
                .get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.getValue(String::class.java)
                    accountNameTextView.text = fullName ?: "User"
                }
                .addOnFailureListener {
                    accountNameTextView.text = "User"
                }
        } else {
            accountNameTextView.text = "User"
        }

        val settingsButton = findViewById<LinearLayout>(R.id.settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, UserSettingsActivity::class.java)
            startActivity(intent)
        }

        // You can add listeners for other buttons here (e.g., terms_button)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainUIActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    // Already on the profile page
                    true
                }
                R.id.navigation_location -> {
                    // Assuming navigation_location goes to a new activity
                    // Replace NewLocationActivity::class.java with the actual Activity class
                    // val intent = Intent(this, NewLocationActivity::class.java)
                    // startActivity(intent)
                    Toast.makeText(this, "Location feature coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
} 