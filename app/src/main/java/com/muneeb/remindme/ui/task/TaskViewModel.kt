package com.muneeb.remindme.ui.task

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskViewModel : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>().apply { value = emptyList() }
    private val tasks : LiveData<List<Task>> get() = _tasks

    private val _totalTasks = MutableLiveData<Int>().apply { value = 0 }
    val totalTasks : LiveData<Int> get() = _totalTasks

    private val _currentDateTaskCount = MutableLiveData<Int>().apply { value = 0 }
    val currentDateTaskCount : LiveData<Int> get() = _currentDateTaskCount

    private val _upcomingTasksCount = MutableLiveData<Int>().apply { value = 0 }
    val upcomingTasksCount : LiveData<Int> get() = _upcomingTasksCount

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val currentDate = dateFormat.format(Date())

    // Method to load tasks from storage
    fun loadTasks(context : Context) {
        val loadedTasks = TaskPreferencesHelper.loadTasks(context)
        setTasks(loadedTasks)
    }

    // Method to set tasks
    fun setTasks(taskList : List<Task>) {
        _tasks.value = taskList
        updateTotalTasks(taskList.size)
        updateCurrentDateTaskCount(taskList)
        updateUpcomingTasksCount(taskList)
    }

    // Method to remove a task
    fun removeTask(task : Task) {
        val updatedList = tasks.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(task)
        setTasks(updatedList)  // This will automatically update the counts
    }

    // Update the total number of tasks
    private fun updateTotalTasks(count : Int) {
        _totalTasks.value = count
    }

    // Update the count of tasks for the current date
    private fun updateCurrentDateTaskCount(taskList : List<Task>) {
        val count = taskList.count { task ->
            dateFormat.format(task.getDueDateAsDate()) == currentDate
        }
        _currentDateTaskCount.value = count
    }

    // Update the count of upcoming tasks (those after the current date)
    private fun updateUpcomingTasksCount(taskList: List<Task>) {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                                                             )
        val count = taskList.count { task ->
            val taskDate = task.getDueDateAsDate()
            !taskDate.before(today) // Include tasks that are today or in the future
        }
        _upcomingTasksCount.value = count
    }


}
