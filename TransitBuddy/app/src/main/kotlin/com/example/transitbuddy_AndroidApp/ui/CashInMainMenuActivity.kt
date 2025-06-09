package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R

class CashInMainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash_in_main_menu)

        supportActionBar?.hide()
        
        // Set up back button
        findViewById<android.widget.ImageButton>(R.id.back).setOnClickListener {
            onBackPressed()
        }
        
        // Set up information button
        findViewById<android.widget.ImageButton>(R.id.information).setOnClickListener {
            Toast.makeText(this, "Information about cash-in methods coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
} 