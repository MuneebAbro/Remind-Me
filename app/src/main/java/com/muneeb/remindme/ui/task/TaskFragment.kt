@file:Suppress("DEPRECATION")

package com.muneeb.remindme.ui.task

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muneeb.remindme.R
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class TaskFragment : Fragment(), AddTaskBottomSheet.OnTaskAddedListener {

    private lateinit var taskViewModel : TaskViewModel
    private lateinit var taskAdapter : TaskAdapter
    private var taskList = mutableListOf<Task>()
    private val code = 1001

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View? {
        val root = inflater.inflate(R.layout.fragment_task, container, false)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        val taskRecyclerView = root.findViewById<RecyclerView>(R.id.taskRecyclerView)
        val addTV = root.findViewById<FloatingActionButton>(R.id.addTV)

        // Load and sort tasks
        taskList = TaskPreferencesHelper.loadTasks(requireContext()).toMutableList()
        taskList.sortBy { it.getDueDateAsDate() }  // Sort tasks by due date

        taskAdapter = TaskAdapter(taskList, { task, position ->
            showTaskOptionsDialog(task, position)
            vibratePattern()
        }, { task, position ->
                                      showDeleteTaskDialog(task, position)
                                      vibratePattern()
                                  })


        taskRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        taskRecyclerView.adapter = taskAdapter

        addTV.setOnClickListener {
            vibratePattern()
            showAddTaskDialog()
        }

        // Update ViewModel with loaded tasks
        taskViewModel.setTasks(taskList)

        return root
    }

    private fun vibratePattern() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }

    private fun showAddTaskDialog() {
        val bottomSheet = AddTaskBottomSheet()
        bottomSheet.setOnTaskAddedListener(this)  // Set the listener
        bottomSheet.show(parentFragmentManager, "AddTaskBottomSheet")
    }

    private fun showTaskOptionsDialog(task : Task, position : Int) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("TASK_TEXT", task.description)
        intent.putExtra("TITLE_TEXT", task.title)
        intent.putExtra("DUE_DATE", task.dueDate)
        intent.putExtra("TASK_POSITION", position) // Pass the position
        val cardColor = when (position % 5) {
            0    -> ContextCompat.getColor(requireContext(), R.color.light_blue1)
            1    -> ContextCompat.getColor(requireContext(), R.color.purple)
            2    -> ContextCompat.getColor(requireContext(), R.color.gold)
            3    -> ContextCompat.getColor(requireContext(), R.color.sea_green)
            4    -> ContextCompat.getColor(requireContext(), R.color.pink)
            else -> ContextCompat.getColor(requireContext(), R.color.blue)
        }
        intent.putExtra("CARD_COLOR", cardColor)

        startActivityForResult(intent, code)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteTaskDialog(task : Task, position : Int) {
        AlertDialog.Builder(requireContext(), R.style.WhiteDialogTheme)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this Note?")
            .setPositiveButton("Yes") { dialog, _ ->
                taskViewModel.removeTask(task)
                taskList.removeAt(position)
                taskList.sortBy { it.getDueDateAsDate() }  // Sort tasks by due date
                taskAdapter.notifyDataSetChanged()
                TaskPreferencesHelper.saveTasks(requireContext(), taskList)

                MotionToast.createColorToast(
                        requireActivity(),
                        message = "Note Deleted Successfully",
                        style = MotionToastStyle.DELETE,
                        position = MotionToast.GRAVITY_BOTTOM,
                        duration = MotionToast.SHORT_DURATION,
                        font = ResourcesCompat.getFont(requireContext(), R.font.roboto)
                                            )

                val alert = dialog as AlertDialog
                val window = alert.window
                window?.decorView?.setBackgroundColor(Color.WHITE)



            }.setNegativeButton("No", null).show()

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onTaskAdded(task: Task) {
        taskList.add(task)
        taskList.sortBy { it.getDueDateAsDate() }
        taskAdapter.notifyDataSetChanged()
        TaskPreferencesHelper.saveTasks(requireContext(), taskList)
    }


    override fun onResume() {
        super.onResume()
        taskList = TaskPreferencesHelper.loadTasks(requireContext()).toMutableList()
        taskList.sortBy { it.getDueDateAsDate() }
        taskAdapter.updateTasks(taskList)
    }

    @Deprecated("Deprecated in Java")

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == code && resultCode == Activity.RESULT_OK) {
            val updatedTask = data?.getSerializableExtra("UPDATED_TASK") as? Task
            val taskPosition = data?.getIntExtra("TASK_POSITION", -1) ?: -1

            if (updatedTask != null && taskPosition != -1 && taskPosition < taskList.size) {
                taskList[taskPosition] = updatedTask // Update the specific task in the list
                taskAdapter.notifyItemChanged(taskPosition) // Notify the adapter of the change

                // Save the updated task list to preferences
                TaskPreferencesHelper.saveTasks(requireContext(), taskList)
            }
        }
    }

}