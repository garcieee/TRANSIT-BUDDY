package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.transitbuddy_AndroidApp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class QRCodeScannerActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        setupUI()
        setupDrawer()
    }
    
    private fun setupUI() {
        // Set up scan button
        findViewById<Button>(R.id.scanButton).setOnClickListener {
            // TODO: Implement QR scanning functionality
            Toast.makeText(this, "QR Scanner feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Hamburger button opens drawer
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }
        
        // Set up bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainUIActivity::class.java)
                    startActivity(intent)
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

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, MainUIActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
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
                    Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
} 