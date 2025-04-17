package com.example.yoga_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yoga_app.databinding.ActivityHomeBinding
import com.example.yoga_app.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

// Wczytanie elementów interfejsu i bazy danych
private lateinit var binding: ActivityHomeBinding
private lateinit var firestore: FirebaseFirestore


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // pobieranie maila z logowania lub rejestracji
        val email = intent.getStringExtra("extra_email")


        // nadpisanie tekstu przycisku na nick użytkownika
        val btn_username = binding.buttonUsername
        btn_username.setText(email)
    }
}