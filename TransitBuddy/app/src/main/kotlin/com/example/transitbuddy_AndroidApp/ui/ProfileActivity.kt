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
import android.view.View
import android.widget.ProgressBar
import com.example.transitbuddy_AndroidApp.auth.AuthenticationModule

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        progressBar = findViewById(R.id.progressBar)

        setupDrawerNavigation()
        setupUI()
        setupBottomNavigation()
    }

    private fun setupDrawerNavigation() {
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    navigateToActivity(MainUIActivity::class.java)
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile
                    true
                }
                R.id.nav_transfer -> {
                    navigateToActivity(ExpressSendActivity::class.java)
                    true
                }
                R.id.nav_stations -> {
                    navigateToActivity(StationsMapActivity::class.java)
                    true
                }
                R.id.nav_landmarks -> {
                    Toast.makeText(this, "Station Landmarks coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    navigateToActivity(UserSettingsActivity::class.java)
                    true
                }
                R.id.nav_logout -> {
                    auth.signOut()
                    val intent = Intent(this, AuthenticationModule::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        val accountNameTextView = findViewById<TextView>(R.id.account_name)
        progressBar.visibility = View.VISIBLE

        if (currentUser != null) {
            database.getReference("users")
                .child(currentUser.uid)
                .child("fullName")
                .get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.getValue(String::class.java)
                    accountNameTextView.text = fullName ?: "User"
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener {
                    accountNameTextView.text = "User"
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            accountNameTextView.text = "User"
            progressBar.visibility = View.GONE
        }

        val settingsButton = findViewById<LinearLayout>(R.id.settings_button)
        settingsButton.setOnClickListener {
            navigateToActivity(UserSettingsActivity::class.java)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    navigateToActivity(MainUIActivity::class.java)
                    true
                }
                R.id.navigation_profile -> {
                    // Already on the profile page
                    true
                }
                R.id.navigation_location -> {
                    navigateToActivity(StationsMapActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }

    private fun <T : AppCompatActivity> navigateToActivity(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
} 