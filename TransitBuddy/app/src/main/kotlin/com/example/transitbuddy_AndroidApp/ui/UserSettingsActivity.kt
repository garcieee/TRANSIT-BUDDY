package com.example.transitbuddy_AndroidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.example.transitbuddy_AndroidApp.auth.AuthenticationModule
import com.google.firebase.auth.FirebaseAuth

class UserSettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        setupUI()
    }

    private fun setupUI() {
        // Top Left Button (Back/Menu)
        findViewById<ImageButton>(R.id.topLeftButton).setOnClickListener {
            onBackPressed() // Go back to the previous activity
        }

        // Notification Button
        findViewById<ImageButton>(R.id.notification).setOnClickListener {
            Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show()
        }

        val logoutButton = findViewById<LinearLayout>(R.id.logout_button)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, AuthenticationModule::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // You can add listeners for other settings options here
    }
} 