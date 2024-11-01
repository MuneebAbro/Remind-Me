package com.muneeb.remindme.ui.task

import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

data class Task(
        val title: String,
        val description: String,
        val dueDate: String,
        val completed: Boolean = false // Added the 'completed' property with a default value of false
               ) : Serializable {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun getDueDateAsDate(): Date {
        return try {
            if (dueDate.isEmpty() || dueDate == "Select a Due Date") {
                Date() // Return the current date
            } else {
                dateFormat.parse(dueDate) ?: Date() // Return current date if parsing fails
            }
        } catch (e: ParseException) {
            Date() // Return current date if there's a parsing error
        }
    }


    fun getDueDateAsLocalDate(): LocalDate {
        return try {
            val date = if (dueDate.isEmpty() || dueDate == "Select a Due Date") {
                Date() // Use current date if no due date is selected
            } else {
                dateFormat.parse(dueDate) ?: Date() // Use current date if parsing fails
            }
            date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (e: ParseException) {

            LocalDate.now() // Use current date if there's a parsing error
        }
    }
}
