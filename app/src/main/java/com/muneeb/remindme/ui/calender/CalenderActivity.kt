@file:Suppress("DEPRECATION")
package com.muneeb.remindme.ui.calender

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.muneeb.remindme.R
import com.muneeb.remindme.ui.task.AddTaskBottomSheet
import com.muneeb.remindme.ui.task.Task
import com.muneeb.remindme.ui.task.TaskAdapter
import com.muneeb.remindme.ui.task.TaskDetailActivity
import com.muneeb.remindme.ui.task.TaskPreferencesHelper
import com.muneeb.remindme.ui.task.TaskViewModel
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalenderActivity : AppCompatActivity() {

    private var selectedDate : LocalDate? = null
    private var previousSelectedDate : LocalDate? = null
    private val code = 1001
    private lateinit var taskViewModel : TaskViewModel
    private lateinit var taskAdapter : TaskAdapter
    private var taskList = mutableListOf<Task>()
    private lateinit var calendarYearMonthText : TextView
    private val datesWithTasks = mutableSetOf<LocalDate>()
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calender)

        // Load saved tasks
        taskList = TaskPreferencesHelper.loadTasks(this).filter { task ->
            val taskDate = task.getDueDateAsLocalDate()
            val today = LocalDate.now()
            taskDate.isEqual(today) || taskDate.isAfter(today)

        }.sortedBy { it.getDueDateAsDate() }.toMutableList()

        datesWithTasks.addAll(taskList.map { it.getDueDateAsLocalDate() })

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]


        calendarYearMonthText = findViewById(R.id.calendarYearMonthText)

        val taskRecyclerView = findViewById<RecyclerView>(R.id.calenderTaskRecyclerView)

        val nextMonth = findViewById<ImageButton>(R.id.nextMonthButton)
        val prevMonth = findViewById<ImageButton>(R.id.previousMonthButton)
        taskAdapter = TaskAdapter(taskList, { task, position ->
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("TASK_TEXT", task.description)
            intent.putExtra("TITLE_TEXT", task.title)
            intent.putExtra("DUE_DATE", task.dueDate)
            val cardColor = when (position % 5) {
                0    -> ContextCompat.getColor(this, R.color.light_blue1)
                1    -> ContextCompat.getColor(this, R.color.purple)
                2    -> ContextCompat.getColor(this, R.color.gold)
                3    -> ContextCompat.getColor(this, R.color.sea_green)
                4    -> ContextCompat.getColor(this, R.color.pink)
                else -> ContextCompat.getColor(this, R.color.blue)
            }
            intent.putExtra("CARD_COLOR", cardColor)
            vibrate()
            val taskIndex = taskList.indexOf(task) // Find the task index
            intent.putExtra("TASK_POSITION", taskIndex) // Pass the index

            startActivityForResult(intent, code)
        }, { task, position ->
                                      showDeleteTaskDialog(task, position)
                                      vibrate()
                                  })



        taskRecyclerView.layoutManager = LinearLayoutManager(this)
        taskRecyclerView.adapter = taskAdapter
        val addTV = findViewById<ImageButton>(R.id.addTV)
        addTV.setOnClickListener {
            vibrate()
            showAddTaskDialog()
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        // Set up the DayBinder for the CalendarView
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view : View) : DayViewContainer {
                return DayViewContainer(view)
            }

            override fun bind(container : DayViewContainer, data : CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                val today = LocalDate.now()
                val isToday = data.date == today
                val isSelectedDate = data.date == selectedDate
                val hasTasks = datesWithTasks.contains(data.date)

                container.setBackground(isToday, isSelectedDate, hasTasks)

                if (data.position == DayPosition.MonthDate) {
                    container.textView.setOnClickListener {
                        vibrate()
                        previousSelectedDate = selectedDate
                        selectedDate = data.date
                        filterTasksByDate(data.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))

                        calendarView.post {
                            calendarView.notifyDayChanged(
                                    CalendarDay(
                                            data.date, DayPosition.MonthDate
                                               )
                                                         )
                            previousSelectedDate?.let { prevDate ->
                                calendarView.notifyDayChanged(
                                        CalendarDay(
                                                prevDate, DayPosition.MonthDate
                                                   )
                                                             )
                            }
                        }
                    }
                }
                else {
                    container.textView.setTextColor(
                            ContextCompat.getColor(
                                    this@CalenderActivity, R.color.grey
                                                  )
                                                   )
                    container.textView.setOnClickListener(null)
                }
            }
        }

        // Setup the calendar view for a range of dates
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(5)
        val lastMonth = currentMonth.plusMonths(5)
        val daysOfWeek = daysOfWeek(DayOfWeek.SUNDAY) // or whatever your start day is

        calendarView.setup(firstMonth, lastMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        // Set the initial month and year
        updateMonthYearText(currentMonth)

        // Add a listener to update the month and year text when the user scrolls to a different month
        calendarView.monthScrollListener = { month ->
            updateMonthYearText(month.yearMonth)
        }
        nextMonth.setOnClickListener {

            vibrate()
            // Get the current visible month
            val currentMonths =
                    calendarView.findFirstVisibleMonth()?.yearMonth ?: return@setOnClickListener

            // Calculate the next month
            val nextMonthS = currentMonths.plusMonths(1)

            // Scroll the calendar to the next month
            calendarView.scrollToMonth(nextMonthS)

            // Update the displayed month and year text
            updateMonthYearText(nextMonthS)
        }

        prevMonth.setOnClickListener {

            vibrate()
            // Get the current visible month
            val currentMonths =
                    calendarView.findFirstVisibleMonth()?.yearMonth ?: return@setOnClickListener

            // Calculate the next month
            val prevMonthS = currentMonths.minusMonths(1)

            // Scroll the calendar to the next month
            calendarView.scrollToMonth(prevMonthS)

            // Update the displayed month and year text
            updateMonthYearText(prevMonthS)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }

    private fun showAddTaskDialog() {
        val addTaskBottomSheet = AddTaskBottomSheet()

        addTaskBottomSheet.setOnTaskAddedListener(object : AddTaskBottomSheet.OnTaskAddedListener {
            override fun onTaskAdded(task : Task) {
                taskList.add(task)
                taskList.sortBy { it.getDueDateAsDate() }  // Sort tasks by due date
                taskAdapter.updateTasks(taskList)
                TaskPreferencesHelper.saveTasks(
                        this@CalenderActivity, taskList
                                               )

                // Update the set of dates with tasks
                datesWithTasks.add(task.getDueDateAsLocalDate())

                // Refresh the calendar view
                val calendarView = findViewById<CalendarView>(R.id.calendarView)
                calendarView.notifyCalendarChanged()
            }
        })

        addTaskBottomSheet.show(supportFragmentManager, "AddTaskBottomSheet")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteTaskDialog(task : Task, position : Int) {
        AlertDialog.Builder(this).setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this Note?")
            .setPositiveButton("Yes") { _, _ ->
                taskViewModel.removeTask(task)

                taskList.removeAt(position)
                taskList.sortBy { it.getDueDateAsDate() }  // Sort tasks by due date
                taskAdapter.notifyDataSetChanged()
                TaskPreferencesHelper.saveTasks(this@CalenderActivity, taskList)

                MotionToast.createColorToast(
                        this,
                        message = "Note Deleted Successfully",
                        style = MotionToastStyle.DELETE,
                        position = MotionToast.GRAVITY_BOTTOM,
                        duration = MotionToast.SHORT_DURATION,
                        font = ResourcesCompat.getFont(this, R.font.roboto)
                                            )
            }.setNegativeButton("No", null).show()
    }

    private fun updateMonthYearText(yearMonth : YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        calendarYearMonthText.text = yearMonth.format(formatter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterTasksByDate(selectedDate: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val selectedDateObj = dateFormat.parse(selectedDate) ?: return

        val filteredTasks = taskList.filter { task ->
            val taskDate = dateFormat.parse(task.dueDate) ?: return@filter false
            !taskDate.before(selectedDateObj)  // Show tasks due on or after the selected date
        }.sortedBy { it.getDueDateAsDate() }

        taskAdapter.updateTasks(filteredTasks)
        taskAdapter.notifyDataSetChanged()  // Ensure the adapter is updated
    }



    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n" +
                "     which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n " +
                "     contracts for common intents available in\n" +
                "      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n" +
                "      testing, and allow receiving results in separate, testable classes independent from your\n" +
                "      activity. Use\n" +
                "      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n" +
                "      with the appropriate {@link ActivityResultContract} and handling the result in the\n" +
                "      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == code && resultCode == Activity.RESULT_OK) {
            val updatedTask = data?.getSerializableExtra("UPDATED_TASK") as? Task
            val taskPosition = data?.getIntExtra("TASK_POSITION", -1) ?: -1

            if (updatedTask != null && taskPosition != -1 && taskPosition < taskList.size) {
                taskList[taskPosition] = updatedTask // Update the specific task in the list
                taskAdapter.notifyItemChanged(taskPosition) // Notify the adapter of the change

                // Update the set of dates with tasks
                datesWithTasks.clear()
                datesWithTasks.addAll(taskList.map { it.getDueDateAsLocalDate() })

                // Refresh the calendar view
                val calendarView = findViewById<CalendarView>(R.id.calendarView)
                calendarView.notifyCalendarChanged()
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        TaskPreferencesHelper.saveTasks(this, taskList)
    }

    inner class DayViewContainer(view : View) : ViewContainer(view) {
        val textView : TextView = view.findViewById(R.id.calendarDayText)

        fun setBackground(
                isCurrentDate : Boolean, isSelectedDate : Boolean, hasTasks : Boolean
                         ) {
            val backgroundDrawable = when {
                // Highlight dates with tasks
                isSelectedDate -> R.drawable.round_square_blue // Blue for selected date
                isCurrentDate  -> R.drawable.round_square_red // Red for current date
                hasTasks       -> R.drawable.hollow_round_square_red
                else           -> null
            }

            textView.background =
                    backgroundDrawable?.let { ContextCompat.getDrawable(view.context, it) }
            textView.setTextColor(
                    if (isCurrentDate || isSelectedDate) ContextCompat.getColor(
                            view.context, R.color.white
                                                                               )
                    else ContextCompat.getColor(view.context, R.color.black)
                                 )

        }

    }

}