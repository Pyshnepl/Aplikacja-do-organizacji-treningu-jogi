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
import kotlin.toString

class RegisterActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


        binding.btnRegRegister.setOnClickListener {
            val email = binding.etRegEmail.text.toString()
            val password = binding.etRegPassword.text.toString()
            val confPassword = binding.etRegConfirmPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confPassword.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, LogInActivity::class.java)
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