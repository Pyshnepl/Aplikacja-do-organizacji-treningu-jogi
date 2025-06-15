package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableRow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yoga_app.databinding.ActivityCustomCourseBinding
import com.example.yoga_app.databinding.ActivityHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale


class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var firestore: FirebaseFirestore
    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("extra_email").toString()


        firestore = FirebaseFirestore.getInstance()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wczytanie nazwy użytkownika
        firestore.collection("Users").document(email)
            .get()
            .addOnSuccessListener { document ->
                binding.tvUsername.setText(document.getString("username"))
            }
            .addOnFailureListener { exception ->
                Log.e("Problem odczytu","Nie udało się znaleść zmiennej username")

            }

        loadView(email)
    }

    fun loadView (email:String){

        firestore.collection("History").whereEqualTo("user_ID",email)
            .get()
            .addOnSuccessListener { documents ->


                for (document in documents) {

                    val timestamp = document.getTimestamp("time_start")
                    if (timestamp != null) {
                        val docID = document.id
                        val date = timestamp.toDate()
                        val tableRow = TableRow(this).apply {
                            layoutParams = TableRow.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                        val button = Button(this).apply {
                            layoutParams = TableRow.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            text = formatter.format(date)
                            setOnClickListener {
                                val intent = Intent(this@HistoryActivity,HistoryDetailsActivity::class.java)
                                intent.putExtra("documentID",docID)
                                startActivity(intent)
                            }


                        }
                        tableRow.addView(button)
                        binding.tableLayout.addView(tableRow)
                    }

                }
            }
            .addOnFailureListener {
                Log.e("Problem odczytu bazy danych","Nie pobrano żadnych dat")
            }

    }

    fun getDayOnly(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }





}