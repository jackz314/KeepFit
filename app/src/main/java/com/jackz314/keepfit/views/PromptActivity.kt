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
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import com.jackz314.keepfit.controllers.ExerciseController
import com.jackz314.keepfit.databinding.ActivityPromptBinding

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
                    var selectedCategories = ArrayList<String>()
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
                val exerciseCategoryAdapter: ArrayAdapter<String>
                val recentExercise = ExerciseController.getMostRecentExercise(this)
                val exerciseArr = ExerciseController.getExerciseCategoryArray(this)
                exerciseCategoryAdapter = if (recentExercise != null) {
                    val exerciseCategories = exerciseArr.toMutableList().apply { add(0, "$recentExercise • Most Recent") }
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseCategories)
                } else {
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseArr)
                }
                b.promptCategoryDropdown.adapter = exerciseCategoryAdapter
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
              //check if the exercise is sit up to start that activity
            val exerc = b.promptCategoryDropdown.selectedItem.toString();
            if (exerc.contains("Sit Ups")) {
                val intent = Intent(this, SitUpCountActivity::class.java)
                intent.putExtra(GlobalConstants.EXERCISE_TYPE, b.promptCategoryDropdown.selectedItem.toString())
                intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, getIntensityValue(b.promptExerciseIntensity.checkedChipId))
                startActivity(intent)
            } else {
                val intent = Intent(this, ExerciseActivity::class.java)
                intent.putExtra(GlobalConstants.EXERCISE_TYPE, b.promptCategoryDropdown.selectedItem.toString())
                intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, getIntensityValue(b.promptExerciseIntensity.checkedChipId))
                startActivity(intent)
            }
            
        }
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}
