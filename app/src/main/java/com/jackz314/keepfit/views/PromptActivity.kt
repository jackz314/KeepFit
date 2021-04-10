package com.jackz314.keepfit.views

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityPromptBinding.inflate(layoutInflater)
        setContentView(b.root)

        when (intent.action) {
            GlobalConstants.ACTION_LIVESTREAM -> {
                isLivestream = true
                if(b.promptTitle.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                b.promptExerciseIntensity.visibility = View.GONE
                b.promptIntensityLabel.visibility = View.GONE
            }
            GlobalConstants.ACTION_EXERCISE -> {
                isLivestream = false
                b.promptTitle.visibility = View.GONE
                b.promptDescription.text = "Track Exercise"
                titleValid = true
                val exerciseCategoryAdapter: ArrayAdapter<String>
                val recentExercise = ExerciseController.getMostRecentExercise(this)
                val exerciseArr = resources.getStringArray(R.array.exercise_categories)
                if (recentExercise != null) {
                    val exerciseCategories = exerciseArr.toMutableList().apply { add(0, "$recentExercise • Most Recent") }
                    exerciseCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseCategories)
                } else {
                    exerciseCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseArr)
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                // Specify the layout to use when the list of choices appears
                exerciseCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                b.promptCategory.adapter = exerciseCategoryAdapter
            }
        }


        b.promptStartBtn.isEnabled = true
        b.promptStartBtn.setOnClickListener {
            start()
        }
    }

    private fun validate(str: String): Boolean{
        return str.trim().isNotEmpty()
//        return str.matches("[0-9a-zA-Z?!.,]+".toRegex())
    }

    private fun getIntensityValue(@IdRes id: Int): Int{
        return when (id) {
            R.id.prompt_intensity_low -> 1
            R.id.prompt_intensity_medium -> 2
            R.id.prompt_intensity_high -> 3
            else -> 2
        }
    }

    private fun start() {
        if (isLivestream){
            val intent = Intent(this, StartLivestreamActivity::class.java)
            intent.putExtra(GlobalConstants.MEDIA_TITLE, b.promptTitle.text.toString())
            intent.putExtra(GlobalConstants.EXERCISE_TYPE, b.promptCategory.selectedItem.toString())
            startActivity(intent)
        } else {
            val intent = Intent(this, ExerciseActivity::class.java)
            intent.putExtra(GlobalConstants.EXERCISE_TYPE, b.promptCategory.selectedItem.toString())
            intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, getIntensityValue(b.promptExerciseIntensity.checkedChipId))
            startActivity(intent)
        }
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}