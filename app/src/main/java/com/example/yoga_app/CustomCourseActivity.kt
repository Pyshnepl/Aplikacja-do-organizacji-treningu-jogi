package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.yoga_app.databinding.ActivityCustomCourseBinding
import com.example.yoga_app.databinding.ActivityRelaxCourseBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch


private lateinit var binding: ActivityCustomCourseBinding
private lateinit var firestore: FirebaseFirestore
private val checkBoxList = mutableListOf<CheckBox>()
private val maxSelected = 8

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


    // Funkcje pomocnicze
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


                // Odwołanie do checkBoxów
                checkBox.setOnCheckedChangeListener { _, isChecked ->

                    // Ograniczenie wyboru do 8 pozycji
                    if (isChecked && getCheckedCount() > maxSelected) {
                        checkBox.isChecked = false
                        Toast.makeText(this, "Możesz wybrać maksymalnie $maxSelected pozycji.", Toast.LENGTH_SHORT).show()

                        for (item in checkBoxList){
                            if (item.isChecked) {
                                Log.i("CHECKED_ITEMS", item.text.toString())
                            }
                        }

                    }

                    //Nadpisanie liczby pozycji
                    binding.tvPosesCount.setText(getCheckedCount().toString())

                    // Nadpisanie czasu
                    lifecycleScope.launch {
                        val duration = getDuration()
                        val min = floor(duration / 60.0).toInt()
                        val sec = duration - min * 60
                        val time = min.toString()+"min "+sec.toString()+"sec"
                        binding.tvEstimatedTime.text = time
                    }

                }

                checkBoxList.add(checkBox) // Dodaj do listy zaznaczonych pozycji


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

    // Zlicza zaznaczone pozycje
    fun getCheckedCount(): Int {
        return checkBoxList.count { it.isChecked }
    }


    fun saveCourse(){

    }

    // Coroutine, w celu użycia drugiej filtracji po firestore
    suspend fun getDuration(): Int{

        var sum = 0
        val checkedItems = checkBoxList.filter { it.isChecked }

        for (item in checkedItems) {
            val querySnapshot = firestore.collection("Exercise")
                .whereEqualTo("Name", item.text)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val duration = document.getDouble("Duration")?.toInt() ?: 0
                sum += duration
            }
        }

        return sum
    }

}