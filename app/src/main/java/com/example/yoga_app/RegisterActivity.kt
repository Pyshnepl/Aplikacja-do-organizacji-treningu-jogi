package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yoga_app.databinding.ActivityLoginBinding
import com.example.yoga_app.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.toString

class RegisterActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnRegRegister.setOnClickListener {
            val email = binding.etRegEmail.text.toString()
            val username = binding.etRegUsername.text.toString()
            val password = binding.etRegPassword.text.toString()
            val confPassword = binding.etRegConfirmPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confPassword.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Zapis użytkownika w formie obiektu
                        val user = hashMapOf(
                            "email" to email,
                            "username" to username,
                            "password" to password
                        )

                        firestore.collection("Users").document(email).set(user)
                            .addOnSuccessListener { Toast.makeText(this, "Poprawnie dodano użytkownika do bazy danych", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { e -> Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show() }



                        // Przejście do strony głównej aplikacji
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("extra_email",email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Fill the boxes", Toast.LENGTH_SHORT).show()
            }
        }


    }
}