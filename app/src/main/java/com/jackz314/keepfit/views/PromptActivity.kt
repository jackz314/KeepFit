package com.jackz314.keepfit.views

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
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
                if(b.promptTitle.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                b.promptCategory.hint = "Category (use \",\" to separate multiple ones)"
                b.promptExerciseIntensity.visibility = View.GONE
                b.promptIntensityLabel.visibility = View.GONE
            }
            GlobalConstants.ACTION_EXERCISE -> {
                isLivestream = false
                b.promptTitle.visibility = View.GONE
                titleValid = true
                (b.promptCategory.layoutParams as ViewGroup.MarginLayoutParams).topMargin -= 72
                b.promptCategory.requestLayout()
                if(b.promptCategory.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                b.promptDescription.text = "Track exercise"
            }
        }

        b.promptTitle.afterTextChanged {
            if (!validate(it)){
                titleValid = false
                b.promptTitle.error = "Please enter a valid title."
                b.promptStartBtn.isEnabled = false
            } else {
                titleValid = true
                if (categoryValid) b.promptStartBtn.isEnabled = true
            }
        }

        b.promptCategory.apply {
            afterTextChanged {
                // TODO: 3/20/21 auto complete?
                if (!validate(it)){
                    categoryValid = false
                    b.promptCategory.error = "Please enter valid categories."
                    b.promptStartBtn.isEnabled = false
                } else {
                    categoryValid = true
                    if (titleValid) b.promptStartBtn.isEnabled = true
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> start()
                }
                false
            }

            b.promptStartBtn.setOnClickListener {
                start()
            }
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
            intent.putExtra(GlobalConstants.EXERCISE_TYPE, b.promptCategory.text.toString())
            startActivity(intent)
        } else {
            val intent = Intent(this, ExerciseActivity::class.java)
            intent.putExtra(GlobalConstants.EXERCISE_TYPE, b.promptCategory.text.toString())
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