package com.example.yoga_app

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yoga_app.databinding.ActivityHomeBinding
import com.example.yoga_app.databinding.ActivityRelaxPosesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference




private lateinit var binding: ActivityRelaxPosesBinding
private lateinit var firestore: FirebaseFirestore

class RelaxPosesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // pobieranie maila z logowania lub rejestracji (mail służy jako ID dokumentu)
        val email = intent.getStringExtra("extra_email").toString()

        firestore = FirebaseFirestore.getInstance()

        binding = ActivityRelaxPosesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pobieranie nazw pozycji z danego kursu
        loadButtonsFromFirestore()


    }
}

private fun loadButtonsFromFirestore() {
    firestore.collection("Courses")
        .document("Relax")
        .get()
        .addOnSuccessListener { document ->
            val data = document.data // Pobieranie id pozycji z kursu

            // Wygamany "if", w celu potwierdzenia że data jest zmienną non-null,
            // w przeciwnym wypadku nie można zrobić pętli "for"
            if (data != null) {
                Log.i("FIREBASE MAP","Wynik: ${document.data}")//

                for ((key,value) in data) {

                    Log.d("PRINT",value.toString())
                    firestore.collection("Exercise").document(value.toString())
                        .get()
                        .addOnSuccessListener { document ->

                            //Pobieranie danych z Exercise_idx
                            var pose_name = document.getString("Name")
                            Log.i("POSE_NAME",pose_name.toString())

                            //Dynamiczna edycja id
                            var index = key.replace("pose_","btn_pose")
                            Log.i("INDEX_EDITED",index.toString())

                            // Nadpisywanie tekstu odpowiedniego przycisku
                            val context = binding.root.context
                            val buttonId = context.resources.getIdentifier(index, "id", context.packageName)
                            val button = binding.root.findViewById<Button>(buttonId)


                            if (button != null) {
                                button.text = pose_name
                                Log.d("BUTTON", "Zmieniono tekst przycisku $index na: $pose_name")
                            } else {
                                Log.e("BUTTON", "Nie znaleziono przycisku o ID: $index")
                            }
                        }
                }
            }

        }
        .addOnFailureListener{
            // NAPRAWIĆ TOAST
            // Toast.makeText(this,"Problem z odczytaniem bazy",Toast.LENGTH_SHORT).show()
        }
}



