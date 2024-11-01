package com.muneeb.remindme.ui.task

import android.content.Context

object TaskPreferencesHelper {

    private const val PREFS_NAME = "task_prefs"
    private const val TASK_LIST_KEY = "task_list"
    private const val SEPARATOR = "|"
    private const val COMMA_ESCAPE = "##COMMA##"

    fun saveTasks(context: Context, taskList: List<Task>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Serialize tasks to a single string
        val serializedTasks = taskList.joinToString(SEPARATOR) { task ->
            "${task.title.replace(",", COMMA_ESCAPE)}," +
            "${task.description.replace(",", COMMA_ESCAPE)}," +
            task.dueDate.replace(",", COMMA_ESCAPE)
        }
        editor.putString(TASK_LIST_KEY, serializedTasks)
        editor.apply()
    }

    fun loadTasks(context: Context): List<Task> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serializedTasks = sharedPreferences.getString(TASK_LIST_KEY, "") ?: ""

        // Deserialize tasks from the string
        return if (serializedTasks.isNotEmpty()) {
            serializedTasks.split(SEPARATOR).mapNotNull {
                val parts = it.split(",", limit = 3).map { part ->
                    part.replace(COMMA_ESCAPE, ",")
                }
                if (parts.size == 3) {
                    Task(parts[0], parts[1], parts[2])
                } else {
                    null
                }
            }
        } else {
            emptyList()
        }
    }

}

