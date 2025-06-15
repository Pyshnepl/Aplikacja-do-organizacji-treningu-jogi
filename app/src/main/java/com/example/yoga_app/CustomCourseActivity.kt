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
import com.example.yoga_app.databinding.SaveConfirmDialogBinding
import com.example.yoga_app.databinding.StartConfirmDialogBinding
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue



class CustomCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomCourseBinding
    private lateinit var saveConfirmDialogBinding: SaveConfirmDialogBinding
    private lateinit var startConfirmDialogBinding: StartConfirmDialogBinding
    private lateinit var firestore: FirebaseFirestore
    private val checkBoxList = mutableListOf<CheckBox>()
    private val maxSelected = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var email = intent.getStringExtra("extra_email").toString()
        firestore = FirebaseFirestore.getInstance()
        binding = ActivityCustomCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Utworzenie checkboxów, ładowanie nazw pozycji i obrazów z firestore
        loadPoses()
        val course_name = binding.etCourseName.text

        // Zapisz sesje własną
        binding.btnSave.setOnClickListener{


            // Sprawdź czy zaznaczono cokolwiek
            if (getCheckedCount() > 0){

                // Wymagaj nazwy kursu
                if(!course_name.isEmpty()){

                    // Definiowanie dialogBinding
                    saveConfirmDialogBinding = SaveConfirmDialogBinding.inflate(layoutInflater)

                    // Utworzenie okna Dialogu
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(saveConfirmDialogBinding.root)
                        .create()

                    saveConfirmDialogBinding.btnConfirm.setOnClickListener {

                        // Zapisanie kursu do bazy danych i przejście do HomeView
                        lifecycleScope.launch{
                            saveCourse(email,course_name.toString())

                            // Przejście do HomePage
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.putExtra("extra_email",email)
                            dialog.dismiss()
                            Log.i("CUSTOMCOURSE","Finished")
                            startActivity(intent)
                            finish()

                        }

                    }

                    saveConfirmDialogBinding.btnDecline.setOnClickListener {

                        dialog.dismiss()
                    }

                    dialog.show()

                }
                else
                {
                    Toast.makeText(this,"Uzupełnij nazwe kursu", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Nie wybrano żadnej pozycji", Toast.LENGTH_SHORT).show()
            }


        }


        // Zacznij sesje własną
        binding.btnStart.setOnClickListener{


            // Upewnij się, że wybrano cokolwiek
            if(getCheckedCount()>0)
            {
                if (!course_name.isEmpty())
                {
                    startConfirmDialogBinding = StartConfirmDialogBinding.inflate(layoutInflater)

                    // Utworzenie okna Dialogu
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(startConfirmDialogBinding.root)
                        .create()

                    startConfirmDialogBinding.btnConfirm.setOnClickListener{

                        // Implementacja intentu
                        val intent = Intent(this, RelaxCourseActivity::class.java)
                        intent.putExtra("extra_email",email)

                        var extra_list = ArrayList<String>()
                        // Nadpisanie listy "extra_list", o nazwy pozycji
                        var num = 1
                        lifecycleScope.launch{
                            for (item in checkBoxList){

                                if (item.isChecked){

                                    var exerciseDoc = firestore.collection("Exercise").whereEqualTo("Name",item.text)
                                        .get()
                                        .await()

                                    var exerciseId = exerciseDoc.firstOrNull()?.id.toString()
                                    extra_list.add(exerciseId)
                                }
                            }
                            Log.i("EXTRA_LIST",extra_list.toString())
                            intent.putExtra("extra_list",extra_list)
                            intent.putExtra("extra_name",course_name.toString())
                            Log.i("NAME",course_name.toString())
                            dialog.dismiss()
                            startActivity(intent)
                            finish()

                        }

                    }

                    startConfirmDialogBinding.btnDecline.setOnClickListener{
                        dialog.dismiss()
                    }

                    dialog.show()
                }
                else
                {
                    Toast.makeText(this,"Uzupełnij nazwe kursu", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this,"Nie wybrano żadnej pozycji",Toast.LENGTH_SHORT).show()
            }





        }


    }


    // Funkcje pomocnicze
    fun loadPoses(){

        checkBoxList.clear()
        firestore.collection("Exercise")
            .get()
            .addOnSuccessListener{ documents ->

            for (document in documents){
                // Dodanie kolumny z każdą iteracją
                val row = TableRow(this)
                // Potencjalne miejsce na manipulowanie wyglądem Row


                // Dodanie Check Box
                val checkBox = CheckBox(this)
                checkBox.setText(document.getString("Name"))
                // Potencjalne miejsce na manipulowanie wyglądem CheckBox



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
                // Potencjalne miejsce na manipulowanie wyglądem Image View
                //


                //

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


    suspend fun saveCourse(userID: String, courseName:String){

        val dataset = hashMapOf <String, Any>(
            "user_ID" to userID,
            "course_name" to courseName,
        )
        var docID = ""

        //Znajdź kurs i pobierz pose_1
        val existing = firestore.collection("Courses").whereEqualTo("user_ID",userID)
            .limit(1)
            .get()
            .await()


        // Jeśli dokument nie istnieje, to utworzy nowy z danymi z dataset
        if (existing.isEmpty){

        // Utworzenie kursu z id użytkownika i nazwą kursu
            val docRef = firestore.collection("Courses")
                .add(dataset)
                .await()
                docID = docRef.id
        }
        else
        {
            // Pobiera id pierwszego dokumentu
            docID = existing.firstOrNull()?.id.orEmpty()
            Log.i("KURS_ISTNIEJE","ID kursu to :${docID}")
        }

        // Nadpisanie mapy "dataset", o nazwy pozycji
        var num = 1
        for (item in checkBoxList){

            if (item.isChecked){
                var pose_index = "pose_"+num.toString()
                num ++
                var exerciseDoc = firestore.collection("Exercise").whereEqualTo("Name",item.text)
                    .get()
                    .await()

                var exerciseId = exerciseDoc.firstOrNull()?.id.toString()
                dataset[pose_index] = exerciseId
                Log.i("NADPISANIE","${pose_index} = ${exerciseId}")



            }

        }

        while (num < maxSelected + 1){
            var pose_index = "pose_"+num.toString()
            dataset[pose_index] = FieldValue.delete()
            num++

        }

        Log.i("NEW_DATA",dataset.toString())


        // Aktualizuj od pose_1, aż po pose_8
        firestore.collection("Courses").document(docID)
            .update(dataset)
            .await()
        Toast.makeText(this,"Utworzono kurs własny", Toast.LENGTH_SHORT).show()
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