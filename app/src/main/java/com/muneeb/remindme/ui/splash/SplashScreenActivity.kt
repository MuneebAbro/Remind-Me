@file:Suppress("DEPRECATION")

package com.muneeb.remindme.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.muneeb.remindme.MainActivity
import com.muneeb.remindme.R
import com.muneeb.remindme.utility.PreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user's name is already saved
        val userName = PreferenceManager.getUserName(this)
        if (userName != null) {
            // If the name is already saved, proceed to MainActivity
            proceedToMainActivity()
            return
        }

        // Set the splash screen layout
        setContentView(R.layout.activity_splash_screen)

        // Initialize views
        val nameInputLayout = findViewById<EditText>(R.id.editText)
        val okButton = findViewById<Button>(R.id.okButton)

        // Set the OK button click listener
        okButton.setOnClickListener {
            // Get the EditText inside the TextInputLayout and retrieve the text
            val enteredName = nameInputLayout.text.toString()
            if (enteredName.isNotBlank()) {
                // Save the entered name in SharedPreferences
                PreferenceManager.saveUserName(this, enteredName)
                // Proceed to MainActivity
                proceedToMainActivity()
            }
            else {
                nameInputLayout.error = "Please enter your name"
            }
        }
    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()

    }
}
