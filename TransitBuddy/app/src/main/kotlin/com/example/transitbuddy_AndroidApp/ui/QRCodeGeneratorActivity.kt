package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R

class QRCodeGeneratorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_generator)

        supportActionBar?.hide()
        setupUI()
    }

    private fun setupUI() {
        // Set up back button
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            onBackPressed()
        }

        // Set up bottom navigation
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, MainUIActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<ImageView>(R.id.nav_qr).setOnClickListener {
            // Already on QR page
        }
        
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
} 