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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class RelaxPosesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRelaxPosesBinding
    private lateinit var firestore: FirebaseFirestore
    private val max_poses = 8


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val email = intent.getStringExtra("extra_email").toString()
        var doc_name = intent.getStringExtra("extra_document").toString()
        firestore = FirebaseFirestore.getInstance()

        binding = ActivityRelaxPosesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadButtonsFromFirestore(doc_name)

        lifecycleScope.launch {
            hideButtonPose(doc_name)
        }


        binding.btnPose1.setOnClickListener {
            hideDisplayDescription("tv_pose1", doc_name)
        }
        binding.btnPose2.setOnClickListener {
            hideDisplayDescription("tv_pose2", doc_name)
        }
        binding.btnPose3.setOnClickListener {
            hideDisplayDescription("tv_pose3", doc_name)
        }
        binding.btnPose4.setOnClickListener {
            hideDisplayDescription("tv_pose4", doc_name)
        }
        binding.btnPose5.setOnClickListener {
            hideDisplayDescription("tv_pose5", doc_name)
        }
        binding.btnPose6.setOnClickListener {
            hideDisplayDescription("tv_pose6", doc_name)
        }
        binding.btnPose7.setOnClickListener {
            hideDisplayDescription("tv_pose7", doc_name)
        }
        binding.btnPose8.setOnClickListener {
            hideDisplayDescription("tv_pose8", doc_name)
        }


        binding.btnStart.setOnClickListener {
            val intent = Intent(this, RelaxCourseActivity::class.java)
            intent.putExtra("extra_email", email)
            intent.putExtra("extra_document", doc_name)
            startActivity(intent)
            finish()
        }

    }


    private fun loadButtonsFromFirestore(doc_name: String) {

        // Zmiana koloru tła
        val colorId = when (doc_name) {
            "Relax" -> R.color.blue
            "Zdrowy kręgosłup" -> R.color.light_green
            "Dolne partie ciała" -> R.color.purple
            else -> R.color.custom_red
        }
        binding.main.setBackgroundColor(ContextCompat.getColor(binding.root.context, colorId))

        firestore.collection("Courses")
            .document(doc_name)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data


                if (data != null) {
                    Log.i("FIREBASE MAP", "Wynik: ${document.data}")//

                    for ((key, value) in data) {

                        Log.d("PRINT", value.toString())
                        firestore.collection("Exercise").document(value.toString())
                            .get()
                            .addOnSuccessListener { document ->

                                //Pobieranie danych z Exercise_idx
                                var pose_name = document.getString("Name")
                                Log.i("POSE_NAME", pose_name.toString())

                                //Dynamiczna edycja id z "pose_x, na "btn_posex
                                var index = key.replace("pose_", "btn_pose")
                                Log.i("INDEX_EDITED", index.toString())

                                // Nadpisywanie tekstu odpowiedniego przycisku
                                val context = binding.root.context
                                val buttonId = context.resources.getIdentifier(
                                    index,
                                    "id",
                                    context.packageName
                                )
                                val button = binding.root.findViewById<Button>(buttonId)


                                if (button != null) {
                                    button.text = pose_name
                                    Log.d(
                                        "BUTTON",
                                        "Zmieniono tekst przycisku $index na: $pose_name"
                                    )
                                } else {
                                    Log.e("BUTTON", "Nie znaleziono przycisku o ID: $index")
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    binding.root.context,
                    "Problem z odczytaniem bazy",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    suspend fun hideButtonPose(doc_name: String) {
        // Pobieranie dokumentu aktualnej sesji
        val document = firestore.collection("Courses").document(doc_name)
            .get()
            .await()

        // numer iteracji
        var num = 1
        while (num < max_poses + 1) {

            var string_field = "pose_" + num.toString() //nazwa zmiennej w firestore
            var pose_id = document.getString(string_field)

            // Jeśli zmienna nie istnieje, ustaw przycisk na "View.GONE"
            if (pose_id == null) {
                // nazwa przycisku w layout
                string_field = string_field.replace("pose_", "btn_pose")

                // Znajdź przycisk
                val context = binding.root.context
                val buttonId =
                    context.resources.getIdentifier(string_field, "id", context.packageName)
                val button = binding.root.findViewById<Button>(buttonId)

                // Ustaw przycisk na niewidzialny
                button.visibility = View.GONE
            }


            num++
        }
    }

    private fun hideDisplayDescription(tvId: String, doc_name: String) {
        val cardId = tvId.replace("tv_", "card_")
        val ctx = binding.root.context
        val cardView = binding.root
            .findViewById<androidx.cardview.widget.CardView>(
                ctx.resources.getIdentifier(cardId, "id", ctx.packageName)
            )

        if (cardView.visibility == View.GONE) {
            val poseKey = tvId.replace("tv_pose", "pose_")
            firestore.collection("Courses").document(doc_name)
                .get()
                .addOnSuccessListener { doc ->
                    val exerciseId = doc.getString(poseKey) ?: return@addOnSuccessListener
                    firestore.collection("Exercise").document(exerciseId)
                        .get()
                        .addOnSuccessListener { exDoc ->
                            val desc = exDoc.getString("Description") ?: ""
                            val tv = cardView.findViewById<TextView>(
                                ctx.resources.getIdentifier(tvId, "id", ctx.packageName)
                            )
                            tv.text = desc
                            cardView.visibility = View.VISIBLE
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                ctx,
                                "Problem z odczytaniem ćwiczenia",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Firestore", "Błąd przy pobieraniu Exercise", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(ctx, "Problem z odczytaniem kursu", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Błąd przy pobieraniu Courses", exception)
                }
        } else {
            cardView.visibility = View.GONE
        }
    }
}
