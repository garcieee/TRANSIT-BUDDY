package com.example.transitbuddy_AndroidApp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.transitbuddy_AndroidApp.R
import com.google.firebase.auth.FirebaseAuth

class QRCodeGeneratorActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_generator)

        auth = FirebaseAuth.getInstance()
        // TODO: Implement QR code generation
        Toast.makeText(this, "QR code generation coming soon!", Toast.LENGTH_SHORT).show()
        finish()
    }
} 