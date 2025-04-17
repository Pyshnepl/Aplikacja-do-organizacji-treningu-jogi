package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yoga_app.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.time.Instant


class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()


        binding.btnLgnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLgnLogin.setOnClickListener {
            val email = binding.etLgnEmail.text.toString()
            val password = binding.etLgnPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("extra_email",email)
                        startActivity(intent)
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