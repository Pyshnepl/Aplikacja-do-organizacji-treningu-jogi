package com.example.yoga_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.yoga_app.databinding.ActivityRelaxCourseBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.collections.mutableListOf


class RelaxCourseActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRelaxCourseBinding
    private lateinit var firestore: FirebaseFirestore
    private val exercises = mutableListOf<String>()
    private var currentExerciseIndex = 0
    private var countdownJob: Job? = null
    private var isPaused = false
    private var remainingTime = 0
    private var max_poses = 8
    private var collectiveDuration: Int = 0
    private var doneExercises = mutableListOf<String>()
    private var timestamp: Timestamp = Timestamp.now()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val doc_name = intent.getStringExtra("extra_name").toString()
        var email = intent.getStringExtra("extra_email").toString()
        var extra_list = intent.getStringArrayListExtra("extra_list")
        Log.i("NAME",doc_name)

        firestore = FirebaseFirestore.getInstance()

        binding = ActivityRelaxCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadScreen(doc_name,email,extra_list)


    }

    private fun loadScreen(doc_name: String, email: String, extra_list: ArrayList<String>?){

        // Zmiana koloru tła
        val colorId = when (doc_name) {
            "Relax" -> R.color.blue
            "Zdrowy kręgosłup" -> R.color.light_green
            "Dolne partie ciała" -> R.color.purple
            else -> R.color.custom_red
        }
        binding.main.setBackgroundColor(ContextCompat.getColor(this, colorId))

        // Opróżnianie poprzednich danych po zmianie karty
        collectiveDuration = 0
        exercises.clear()
        doneExercises.clear()
        timestamp = Timestamp.now()

        // Aktualizuj liste o pozycje z CustomCourse, jeśli "extra_list" nie jest puste
        extra_list?.let{
            exercises.addAll(it)
        }

        currentExerciseIndex = 0

        firestore.collection("Courses").document(doc_name)
            .get()
            .addOnSuccessListener { document ->
                // Pobranie wszystkich pozycji do listy
                for (i in 1..max_poses) {
                    val pose = document.getString("pose_$i")
                    if (!pose.isNullOrBlank()) {
                        exercises.add(pose)
                    }
                }
                if (exercises.isNotEmpty()) {
                    loadExercise(currentExerciseIndex, email, doc_name)
                }
            }
            .addOnFailureListener{ exception ->
                Toast.makeText(binding.root.context, "Problem połączenia z bazą Firestore", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadExercise(index: Int,email: String, doc_name: String) {

        val exerciseId = exercises[index]
        firestore.collection("Exercise").document(exerciseId)
            .get()
            .addOnSuccessListener { document ->

                // Nie da się pobrać duration, jako Int, więc mamy ?.Int,
                // co pozwala na ewentualny null
                val duration = document.getDouble("Duration")?.toInt()
                val name = document.getString("Name")
                val image = document.getString("Image")

                // Zmiana nazwy pozycji
                binding.tvPose.text = name
                // Znalezienie id obrazu i załadowanie go
                val imgId = resources.getIdentifier(image, "drawable", packageName)
                binding.ivPose.setImageResource(imgId)

                // Rozpoczenie liczenia czasu (ładowanie progress bar i licznika sekund)
                startCountdown(duration,email, doc_name)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Nie udało się załadować ćwiczenia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startCountdown(duration: Int?,email: String, doc_name: String) {
        if (duration == null) return

        countdownJob?.cancel() // zatrzymaj poprzedni jeśli istniał
        remainingTime = duration
        binding.pb.progress = 0
        isPaused = false


        countdownJob = lifecycleScope.launch {
            var i = 0
            while (i < duration) {
                if (!isPaused) {
                    val percent = ((i + 1) * 100) / duration
                    binding.pb.progress = percent
                    binding.tvTime.text = (duration - i).toString()
                    i++
                    remainingTime = duration - i
                    delay(1000L)
                } else {
                    delay(10L) // krótki delay, żeby nie zjadać CPU
                }
            }
            // Zapisanie łącznego czsu trwania kursu i listy wykonanych pozycji
            collectiveDuration += duration
            doneExercises.add(exercises[currentExerciseIndex])
            currentExerciseIndex++
            // Warunek zakończenia sesji
            if (currentExerciseIndex >= exercises.size){
                FinishActivity(email, doc_name)
            }
            else
            {
            loadExercise(currentExerciseIndex,email, doc_name)
            }
        }

        // Zatrzymanie kursu i zmiana tekstu przycisku
        val iconPause = findViewById<ImageView>(R.id.icon_pause)

        binding.btnPause.setOnClickListener {
            isPaused = !isPaused
            if (isPaused) {
                iconPause.setImageResource(R.drawable.ic_play)
            } else {
                iconPause.setImageResource(R.drawable.ic_pause)
            }
        }



        // Kolejna pozycja kursu
        binding.btnNext.setOnClickListener {
            countdownJob?.cancel() // Zatrzymanie obecnego zadania
            currentExerciseIndex++
            // Warunek zatrzymania sesji
            if (currentExerciseIndex >= exercises.size){
                FinishActivity(email, doc_name)
            }else{
                loadExercise(currentExerciseIndex,email, doc_name)
            }

        }
    }

    // Zakończenie kursu i przkazanie maila jako extra
    private fun FinishActivity(email: String, doc_name: String){

        Toast.makeText(this, "Kurs zakończony!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("extra_email",email)
        Log.i("WYKONANE ĆWICZENIA", doneExercises.toString())

        lifecycleScope.launch {

            // Zapisanie historii użytkownika, i przejście do HomeView
            if (collectiveDuration != 0)
            {
                writeHistory(email, doc_name)
            }
            startActivity(intent)

        }


    }

    suspend fun writeHistory(email: String,doc_name: String){

        // Utworzenie zbioru danych do Firestore
        val dataset = hashMapOf <String, Any>(

            "user_ID" to email,
            "course_name" to doc_name,
            "poses" to doneExercises,
            "duration" to collectiveDuration,
            "time_start" to timestamp
        )

        // Utworzenie dokumentu i czekanie na nadpisanie bazy danych
        firestore.collection("History")
            .add(dataset)
            .await()

        Toast.makeText(this,"Zapisano postęp w historii użytkownika", Toast.LENGTH_SHORT).show()


    }

}

