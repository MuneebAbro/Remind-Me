@file:Suppress("DEPRECATION")
package com.muneeb.remindme.ui.task

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ContextThemeWrapper
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.muneeb.remindme.R
import java.util.Calendar

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var taskViewModel : TaskViewModel
    private var taskList = mutableListOf<Task>()
    private var taskPosition : Int = - 1 // Position of the task in the list

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        window.statusBarColor = intent.getIntExtra("CARD_COLOR", R.color.blue)

        // View bindings
        val titleEditText : EditText = findViewById(R.id.titleEditText)
        val taskEditText : EditText = findViewById(R.id.taskEditText)
        val backArrow : ImageView = findViewById(R.id.backArrow)
        val tick : ImageView = findViewById(R.id.tick)
        val dueDateTextView : TextView = findViewById(R.id.dueDateTextView)
        val rootLayout : ConstraintLayout = findViewById(R.id.rootLayout)

        // ViewModel initialization
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        taskList = TaskPreferencesHelper.loadTasks(this).toMutableList()

        // Retrieve data from intent
        val title = intent.getStringExtra("TITLE_TEXT")
        val task = intent.getStringExtra("TASK_TEXT")
        val dueDate = intent.getStringExtra("DUE_DATE")
        val cardColor = intent.getIntExtra("CARD_COLOR", R.color.blue)
        taskPosition = intent.getIntExtra("TASK_POSITION", - 1)

        // Set text views
        titleEditText.setText(title)
        taskEditText.setText(task)
        dueDateTextView.text = "$dueDate"

        dueDateTextView.setOnClickListener {

            showDatePicker(dueDateTextView)
        }

        // Set background color
        rootLayout.setBackgroundColor(cardColor)

        // Set the back arrow click listener
        backArrow.setOnClickListener {
            vibratePattern(this)
            finish()
        }
        tick.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newTask = taskEditText.text.toString()
            val newDueDate = dueDateTextView.text.toString()

            if (taskPosition != -1 && taskPosition < taskList.size) {
                val updatedTask = taskList[taskPosition].copy(
                        title = newTitle,
                        description = newTask,
                        dueDate = newDueDate
                                                             )
                taskList[taskPosition] = updatedTask

                // Pass the updated task back to the CalenderActivity
                val resultIntent = Intent().apply {
                    putExtra("UPDATED_TASK", updatedTask)
                    putExtra("TASK_POSITION", taskPosition)
                }
                setResult(RESULT_OK, resultIntent)  // Notify the calling activity that the task was updated
            }

            vibratePattern(this)
            finish()
        }


    }

    private fun showDatePicker(dueDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val themedContext = ContextThemeWrapper(this, R.style.DatePickerLightTheme)
        val datePickerDialog = DatePickerDialog(
                themedContext,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                    dueDateTextView.text = date
                },
                year, month, day
                                               )

        datePickerDialog.show()
    }
    private fun vibratePattern(context : Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }
}