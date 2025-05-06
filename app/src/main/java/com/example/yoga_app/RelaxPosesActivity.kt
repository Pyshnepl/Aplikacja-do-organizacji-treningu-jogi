package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
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
import androidx.core.content.ContextCompat




private lateinit var binding: ActivityRelaxPosesBinding
private lateinit var firestore: FirebaseFirestore

class RelaxPosesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val email = intent.getStringExtra("extra_email").toString()
        val doc_name = intent.getStringExtra("extra_document").toString()
        firestore = FirebaseFirestore.getInstance()

        binding = ActivityRelaxPosesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pobieranie nazw pozycji z danego kursu
        loadButtonsFromFirestore(doc_name)
        

        binding.btnPose1.setOnClickListener{
            hideDisplayDescription("tv_pose1",doc_name)
        }

        binding.btnPose2.setOnClickListener{
            hideDisplayDescription("tv_pose2",doc_name)
        }
        binding.btnPose3.setOnClickListener{
            hideDisplayDescription("tv_pose3",doc_name)
        }
        binding.btnPose4.setOnClickListener{
            hideDisplayDescription("tv_pose4",doc_name)
        }
        binding.btnPose5.setOnClickListener{
            hideDisplayDescription("tv_pose5",doc_name)
        }
        binding.btnPose6.setOnClickListener{
            hideDisplayDescription("tv_pose6",doc_name)
        }
        binding.btnPose7.setOnClickListener{
            hideDisplayDescription("tv_pose7",doc_name)
        }
        binding.btnPose8.setOnClickListener{
            hideDisplayDescription("tv_pose8",doc_name)
        }



        binding.btnStart.setOnClickListener{
            val intent = Intent(this, RelaxCourseActivity::class.java)
            intent.putExtra("extra_email",email)
            intent.putExtra("extra_dcument",doc_name)
            startActivity(intent)
            finish()
        }

    }
}

private fun loadButtonsFromFirestore(doc_name: String) {

    // Zmiana koloru tła
    if (doc_name == "Relax"){
        binding.main.setBackgroundColor(
            ContextCompat.getColor(binding.root.context, R.color.blue)
        )
    }else if (doc_name == "Zdrowy kręgosłup") {
        binding.main.setBackgroundColor(
            ContextCompat.getColor(binding.root.context, R.color.light_green)
        )
    }else{
        binding.main.setBackgroundColor(
            ContextCompat.getColor(binding.root.context, R.color.purple)
        )
    }


    // Przeglądanie kolejnych elementów w kolekcji Courses i dokumencie np.Relax

    firestore.collection("Courses")
        .document(doc_name)
        .get()
        .addOnSuccessListener { document ->
            val data = document.data


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


private fun hideDisplayDescription(tv_id: String,doc_name: String){

    // Szukanie tekstu do rozwinięcia
    val context = binding.root.context
    val textViewId = context.resources.getIdentifier(tv_id, "id", context.packageName)
    val textView = binding.root.findViewById<TextView>(textViewId)


    var pose_string = tv_id.replace("tv_pose","pose_").toString()

    // Przeglądanie kolejnych elementó w kolekcji Courses i dokumencie Relax
    firestore.collection("Courses")
        .document(doc_name)
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

