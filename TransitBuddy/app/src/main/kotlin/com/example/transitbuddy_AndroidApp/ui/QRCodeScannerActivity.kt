package com.example.transitbuddy_AndroidApp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R

class QRCodeScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Set up scan button
        findViewById<Button>(R.id.scanButton).setOnClickListener {
            // TODO: Implement QR scanning functionality
            Toast.makeText(this, "QR Scanner feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Set up navigation buttons
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            onBackPressed()
        }
        
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