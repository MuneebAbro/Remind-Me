@file:Suppress("DEPRECATION")

package com.muneeb.remindme.ui.task

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.muneeb.remindme.R
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskBottomSheet : BottomSheetDialogFragment() {

    interface OnTaskAddedListener {
        fun onTaskAdded(task : Task)
    }

    private var onTaskAddedListener : OnTaskAddedListener? = null

    fun setOnTaskAddedListener(listener : OnTaskAddedListener) {
        onTaskAddedListener = listener
    }

    private lateinit var taskAdapter : TaskAdapter
    private var taskList = mutableListOf<Task>()

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View? {
        taskList = TaskPreferencesHelper.loadTasks(requireContext()).toMutableList()
        taskList.sortBy { it.getDueDateAsDate() }
        return inflater.inflate(R.layout.add_task, container, false)
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        taskAdapter = TaskAdapter(taskList, ::onTaskClick, ::onTaskLongClick)

        val titleEditText = view.findViewById<EditText>(R.id.titleEditText)
        val taskEditText = view.findViewById<EditText>(R.id.taskEditText)
        val dueDateTextView = view.findViewById<TextView>(R.id.dueDateTextView)
        val addTaskButton = view.findViewById<Button>(R.id.addButton)

        dueDateTextView.setOnClickListener {
            showDatePicker(dueDateTextView)
        }

        addTaskButton.setOnClickListener {
            val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect =
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
            val title = titleEditText.text.toString().trim()
            var task = taskEditText.text.toString().trim()
            val dueDate = dueDateTextView.text.toString()

            // Use current date if no due date is selected
            val finalDueDate = if (dueDate.isEmpty() || dueDate == "Select a Due Date") {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }
            else {
                dueDate
            }
            if (task.isEmpty()) {
                task = "(Empty)"
            }

            if (title.isNotEmpty()) {
                val newTask = Task(title, task, finalDueDate)
                taskList.add(newTask)
                taskList.sortBy { it.getDueDateAsDate() }
                taskAdapter.updateTasks(taskList)
                TaskPreferencesHelper.saveTasks(requireContext(), taskList)

                MotionToast.createColorToast(
                        requireActivity(),
                        message = "Reminder Added Successfully",
                        style = MotionToastStyle.SUCCESS,
                        position = MotionToast.GRAVITY_BOTTOM,
                        duration = MotionToast.SHORT_DURATION,
                        font = ResourcesCompat.getFont(requireContext(), R.font.roboto)

                                            )
                onTaskAddedListener?.onTaskAdded(newTask)
                dismiss()
            }
            else {
                if (title.isEmpty()) titleEditText.error = "Please enter a title"
                if (task.isEmpty()) taskEditText.error = "Please enter a task"
            }

        }
    }

    private fun showDatePicker(dueDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val themedContext = ContextThemeWrapper(requireContext(), R.style.DatePickerLightTheme)
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


    private fun onTaskClick(task : Task, position : Int) {
        println(task)
        println(position)
    }

    private fun onTaskLongClick(task : Task, position : Int) {
        println(task)
        println(position)
    }
}
