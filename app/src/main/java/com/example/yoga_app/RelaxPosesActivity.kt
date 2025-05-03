package com.example.yoga_app

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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

        // Rozwijanie/zwijanie opisu pozycji
        binding.btnPose1.setOnClickListener{
            var tv_id = "tv_pose1"
            hideDisplayDescription(tv_id)
        }

        binding.btnPose2.setOnClickListener{
            var tv_id = binding.tvPose2.id.toString()
            hideDisplayDescription(tv_id)
        }
        binding.btnPose3.setOnClickListener{
            var tv_id = binding.tvPose3.id.toString()
            hideDisplayDescription(tv_id)
        }
        binding.btnPose4.setOnClickListener{
            var tv_id = binding.tvPose4.id.toString()
            hideDisplayDescription(tv_id)
        }
        binding.btnPose5.setOnClickListener{
            var tv_id = binding.tvPose5.id.toString()
            hideDisplayDescription(tv_id)
        }
        binding.btnPose6.setOnClickListener{
            var tv_id = binding.tvPose6.id.toString()
            hideDisplayDescription(tv_id)
        }
        binding.btnPose7.setOnClickListener{
            var tv_id = binding.tvPose7.id.toString()
            hideDisplayDescription(tv_id)
        }
        binding.btnPose8.setOnClickListener{
            var tv_id = binding.tvPose8.id.toString()
            hideDisplayDescription(tv_id)
        }

    }
}

private fun loadButtonsFromFirestore() {

    // Przeglądanie kolejnych elementó w kolekcji Courses i dokumencie Relax
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

                            //Dynamiczna edycja id z "pose_x, na "btn_posex
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
        .addOnFailureListener{ exception ->
            Toast.makeText(binding.root.context,"Problem z odczytaniem bazy",Toast.LENGTH_SHORT).show()
        }
}

private fun hideDisplayDescription(tv_id: String){

    // Szukanie tekstu do rozwinięcia
    val context = binding.root.context
    val textViewId = context.resources.getIdentifier(tv_id, "id", context.packageName)
    val textView = binding.root.findViewById<TextView>(textViewId)


    var pose_string = tv_id.replace("tv_pose","pose_").toString()

    // Przeglądanie kolejnych elementó w kolekcji Courses i dokumencie Relax
    firestore.collection("Courses")
        .document("Relax")
        .get()
        .addOnSuccessListener { document ->
            val data = document.get(pose_string).toString() // Pobieranie id pozycji z kursu
            Log.d("CLICK INFO", data)

            // Odwołanie do Exercise/Exercise_idx
                firestore.collection("Exercise").document(data)
                    .get()
                    .addOnSuccessListener{ document ->
                        // Pobieranie tekstu z pola "Description"
                        var description = document.getString("Description")
                        Log.d("DESCRIPTION TEXT", description.toString())

                        if (description != null) {
                            // Nadpisywanie opisu z bazy danych
                            if (textView.text.isEmpty()) {
                                textView.setText(description)
                            } else {
                                textView.setText("")
                            }
                        }
                    }
        }
        .addOnFailureListener{ exception ->
            Toast.makeText(binding.root.context,"Problem z odczytaniem bazy",Toast.LENGTH_SHORT).show()
        }
}