package com.jackz314.keepfit.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import com.jackz314.keepfit.controllers.ExerciseController
import com.jackz314.keepfit.controllers.SchedulingController
import com.jackz314.keepfit.databinding.ActivityPromptBinding
import com.jackz314.keepfit.models.ScheduledExercise
import java.util.*
import kotlin.collections.ArrayList

class PromptActivity : AppCompatActivity() {

    private lateinit var b: ActivityPromptBinding
    private var isLivestream = false
    private var titleValid = false
    private var categoryValid = false

    private val TAG = "PromptActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityPromptBinding.inflate(layoutInflater)
        setContentView(b.root)

        when (intent.action) {
            GlobalConstants.ACTION_LIVESTREAM -> {
                isLivestream = true
                if (b.promptTitle.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                b.promptExerciseIntensity.visibility = View.GONE
                b.promptIntensityLabel.visibility = View.GONE
                b.promptCategoryDropdown.visibility = View.GONE

                val categoryView = findViewById<TextView>(R.id.prompt_category_select)

                categoryView.setOnClickListener {
                    val builder = AlertDialog.Builder(this@PromptActivity)
                    val categories = applicationContext.resources.getStringArray(R.array.exercise_categories)
                    builder.setTitle("Select Categories")
                    val checked = BooleanArray(categories.size) { false }
                    builder.setMultiChoiceItems(categories, checked) { dialog, which, isChecked ->

                    }

                    // Set the positive/yes button click listener
                    val selectedCategories = ArrayList<String>()
                    builder.setPositiveButton("OK") { dialog, which ->
                        for (i in checked.indices) {
                            val c = checked[i]
                            if (c) {
                                selectedCategories.add(categories[i])
                            }
                        }
                        categoryView.text = selectedCategories.joinToString()
                    }
                    // Set the neutral/cancel button click listener
                    builder.setNeutralButton("Cancel") { dialog, which ->
                        // Do something when click the neutral button
                    }
                    val dialog = builder.create()
                    // Display the alert dialog on interface
                    dialog.show()
                }
            }
            GlobalConstants.ACTION_EXERCISE -> {
                isLivestream = false
                b.promptTitle.visibility = View.GONE
                b.promptCategorySelect.visibility = View.GONE
                titleValid = true
                b.promptDescription.text = "Track Exercise"
                titleValid = true
                val recentExercise = ExerciseController.getMostRecentExercise(this)
                val exerciseCategories = ExerciseController.getExerciseCategoryArray(this).toMutableList()
                if (recentExercise != null) exerciseCategories.add(0, "$recentExercise • Most Recent")
                b.promptCategoryDropdown.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseCategories)
                if (intent.hasExtra(GlobalConstants.SCHEDULED_EXERCISE)) {
                    if (FirebaseApp.getApps(this).isEmpty()) { // just in case
                        FirebaseApp.initializeApp(this)
                    }
                    val scheduledExercise = intent.getSerializableExtra(GlobalConstants.SCHEDULED_EXERCISE) as ScheduledExercise
                    b.promptExerciseIntensity.check(getIntensityChipId(scheduledExercise.intensity))
                    val scheduledCategoryIndex = exerciseCategories.indexOf(scheduledExercise.category.trim().capitalize(Locale.getDefault()))
                    b.promptCategoryDropdown.setSelection(scheduledCategoryIndex)
                }
            }
        }


        b.promptStartBtn.isEnabled = true
        b.promptStartBtn.setOnClickListener {
            start()
        }
    }

    private fun validate(str: String): Boolean {
        return str.trim().isNotEmpty()
//        return str.matches("[0-9a-zA-Z?!.,]+".toRegex())
    }

    private fun getIntensityValue(@IdRes id: Int) = when (id) {
        R.id.prompt_intensity_low -> 1
        R.id.prompt_intensity_medium -> 2
        R.id.prompt_intensity_high -> 3
        else -> 2
    }

    private fun getIntensityChipId(value: Int) = when (value) {
        1 -> R.id.prompt_intensity_low
        2 -> R.id.prompt_intensity_medium
        3 -> R.id.prompt_intensity_high
        else -> R.id.prompt_intensity_medium
    }

    private fun start() {
        if (isLivestream) {
            val intent = Intent(this, StartLivestreamActivity::class.java)
            intent.putExtra(GlobalConstants.MEDIA_TITLE, b.promptTitle.text.toString())
            //val chipGroup = findViewById<ChipGroup>(R.id.prompt_category_button)
            /*val selectedChips = chipGroup.children
                    .filter { (it as Chip).isChecked }
                    .map { (it as Chip).text.toString() }
            val categoryStr = selectedChips.joinToString();*/
            val categoryView = findViewById<TextView>(R.id.prompt_category_select)
            intent.putExtra(GlobalConstants.EXERCISE_TYPE, categoryView.text.toString())
            startActivity(intent)
        } else {
            if (intent.hasExtra(GlobalConstants.SCHEDULED_EXERCISE)) {
                SchedulingController.cancelScheduledExercise(this, intent.getSerializableExtra(GlobalConstants.SCHEDULED_EXERCISE) as ScheduledExercise)
            }
            //check if the exercise is sit up to start that activity
            val exercise = b.promptCategoryDropdown.selectedItem.toString().replace(" • Most Recent", "", true)
            val intent = Intent(this, ExerciseActivity::class.java)
            intent.putExtra(GlobalConstants.EXERCISE_TYPE, exercise)
            intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, getIntensityValue(b.promptExerciseIntensity.checkedChipId))
            startActivity(intent)
        }
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}
