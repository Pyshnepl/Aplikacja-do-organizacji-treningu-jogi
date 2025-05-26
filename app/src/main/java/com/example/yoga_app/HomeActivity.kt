package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.yoga_app.databinding.ActivityHomeBinding
import com.example.yoga_app.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

// Wczytanie elementów interfejsu i bazy danych
private lateinit var binding: ActivityHomeBinding
private lateinit var firestore: FirebaseFirestore


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CYCLE", "onCreate")

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // pobieranie maila z logowania lub rejestracji (mail służy jako ID dokumentu)
        val email = intent.getStringExtra("extra_email").toString()

        loadView(email)

        // Przejście do okna sesji "Relax"
        binding.buttonRelaksHp.setOnClickListener {
            val intent = Intent(this, RelaxPosesActivity::class.java)
            intent.putExtra("extra_email",email)
            intent.putExtra("extra_document","Relax")
            startActivity(intent)
        }

        // Przejście do okna sesji "Zdrowy kręgosłup"
        binding.buttonZdrowykregoslupHp.setOnClickListener {
            val intent = Intent(this, RelaxPosesActivity::class.java)
            intent.putExtra("extra_email",email)
            intent.putExtra("extra_document","Zdrowy kręgosłup")
            startActivity(intent)
        }

        // Przejście do okna sesji "Dolne partie ciała"
        binding.buttonDolnepartiecialaHp.setOnClickListener {
            val intent = Intent(this, RelaxPosesActivity::class.java)
            intent.putExtra("extra_email",email)
            intent.putExtra("extra_document","Dolne partie ciała")
            startActivity(intent)
        }

        // Przejście do tworzenia kursów
        binding.buttonDodajkursHp.setOnClickListener{
            val intent = Intent(this, CustomCourseActivity::class.java)
            intent.putExtra("extra_email",email)
            startActivity(intent)
            finish()
        }


    }


    fun loadView(email: String){

        // Pobieranie nazwy użytkownika
        firestore.collection("Users").document(email)
            .get()
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

        // Uwidocznianie przycisku kursu
        firestore.collection("Courses").whereEqualTo("user_ID",email)
            .get()
            .addOnSuccessListener{documentList ->

                // Jeśli kurs istnieje, to przycisk widoczny
                if (!documentList.isEmpty){

                    binding.buttonCustomkursHp.visibility = View.VISIBLE
                    binding.buttonCustomkursHp.setText(documentList.documents[0].getString("course_name"))
                }
                else{
                    binding.buttonCustomkursHp.visibility = View.GONE
                }

            }
            .addOnFailureListener{exception ->
                Log.i("COURSE","Problem odczytu bazy danych", exception)
            }

    }
}