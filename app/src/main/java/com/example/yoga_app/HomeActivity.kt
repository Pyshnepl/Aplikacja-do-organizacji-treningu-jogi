package com.example.yoga_app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yoga_app.databinding.ActivityHomeBinding
import com.example.yoga_app.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots

// Wczytanie elementów interfejsu i bazy danych
private lateinit var binding: ActivityHomeBinding
private lateinit var firestore: FirebaseFirestore


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // pobieranie maila z logowania lub rejestracji (mail służy jako ID dokumentu)
        val email = intent.getStringExtra("extra_email").toString()



        // Pobieranie danych z bazy po ID
        val docRef = firestore.collection("Users").document(email)
        docRef.get()
            .addOnSuccessListener {document ->
                if (document != null) {
                    Log.d("FIRESTORE","DocumentSnapshot data: ${document.data}")

                    // Nadpisanie tekstu nazwą użytkownika
                    binding.buttonUsername.setText(document.getString("username"))
                } else {
                    Log.i("FIRESTORE","No such document exist")
                }

            }
            .addOnFailureListener {
               exception -> Log.d("FIRESTORE","get failed with",exception)
            }


        // nadpisanie tekstu przycisku na nick użytkownika

    }
}