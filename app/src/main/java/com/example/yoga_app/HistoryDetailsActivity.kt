package com.example.yoga_app

import android.os.Bundle
import android.util.Log
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.yoga_app.databinding.ActivityCustomCourseBinding
import com.example.yoga_app.databinding.ActivityHistoryDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Typeface
import android.view.Gravity
import androidx.core.content.res.ResourcesCompat


class HistoryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("extra_email").toString()
        val docID = intent.getStringExtra("documentID").toString()

        firestore = FirebaseFirestore.getInstance()
        binding = ActivityHistoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadTable(email, docID)

    }

    fun loadTable(email: String, docID: String){

        firestore.collection("History").document(docID)
            .get()
            .addOnSuccessListener{ data ->

                // Ustaw nazwe kursu i czas rozpoczęcia
                binding.tvName.setText(data.getString("course_name"))

                val timestamp = data.getTimestamp("time_start")
                if (timestamp != null){
                    val date = formatter.format(timestamp.toDate())
                    binding.tvDateAndTime.text = date.toString()
                }

                val poses = data.get("poses") as List<String>

                for (item in poses){
                    firestore.collection("Exercise").document(item)
                        .get()
                        .addOnSuccessListener { document ->

                            var name = document.getString("Name").toString()
                            var time = document.getLong("Duration")?.toInt()
                            addRowToTable(name, time)

                        }

                }



            }
            .addOnFailureListener {
                Log.e("ERROR","Problem odczytu danych")
            }




    }

    private fun addRowToTable(name: String, time: Int?) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val textView1 = TextView(this).apply {
            text = name
            setPadding(16, 8, 16, 8)
            textSize = 25f
            gravity = Gravity.START
            typeface = ResourcesCompat.getFont(context, R.font.open_sans_light)
            setTypeface(typeface, Typeface.BOLD)
        }



        val textView2 = TextView(this).apply {
            text = time.toString()
            setPadding(16, 8, 16, 8)
            textSize = 22f
            gravity = Gravity.END
            typeface = ResourcesCompat.getFont(context, R.font.open_sans_light)
            setTypeface(typeface, Typeface.BOLD)
        }

        tableRow.addView(textView1)
        tableRow.addView(textView2)
        binding.tableLayout.addView(tableRow)
    }


}