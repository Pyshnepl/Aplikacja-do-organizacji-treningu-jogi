package com.example.yoga_app

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yoga_app.databinding.ActivityCustomCourseBinding
import com.example.yoga_app.databinding.ActivityRelaxCourseBinding
import com.google.firebase.firestore.FirebaseFirestore


private lateinit var binding: ActivityCustomCourseBinding
private lateinit var firestore: FirebaseFirestore


class CustomCourseActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var email = intent.getStringExtra("extra_email").toString()
        firestore = FirebaseFirestore.getInstance()

        binding = ActivityCustomCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Utworzenie checkboxów, ładowanie nazw pozycji i obrazów z firestore
        loadPoses()

        // Zapisz sesje własną
        binding.btnSave.setOnClickListener{

            // Wymagaj nazwy kursu
            saveCourse()

        }


        // Zacznij sesje własną
        binding.btnStart.setOnClickListener{

            // Intent albo do opisu pozycji albo do przebiegu kursu
        }

    }

    fun loadPoses(){

    firestore.collection("Exercise")
        .get()
        .addOnSuccessListener{ documents ->

            for (document in documents){
                // Dodanie kolumny z każdą iteracją
                val row = TableRow(this)

                // Dodanie Check Box
                val checkBox = CheckBox(this)
                checkBox.setText(document.getString("Name"))

                // Dodanie obrazu
                val imageView = ImageView(this) // tworzenie imageView
                imageView.layoutParams = TableRow.LayoutParams(200,200) // ustawianie rozmiarów obrazu
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE // wybór skalowania

                val image_name = document.getString("Image") // pobieranie z firestore nazwy obrazu
                val imgId = resources.getIdentifier(image_name, "drawable", packageName) // pobieranie id obrazu z Android Studio
                imageView.setImageResource(imgId) // Nałożenie obrazu na imageView


                // Składanie elementów do wiersza
                row.addView(checkBox)
                row.addView(imageView)


                // Wstawianie wiersza do tabelii
                binding.tableLayout.addView(row)

            }

        }
        .addOnFailureListener{
            Toast.makeText(this,"Błąd ładowania danych", Toast.LENGTH_SHORT).show()
        }

    }

    fun saveCourse(){

    }

}