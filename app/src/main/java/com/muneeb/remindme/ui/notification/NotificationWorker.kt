package com.muneeb.remindme.ui.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.muneeb.remindme.MainActivity
import com.muneeb.remindme.R
import com.muneeb.remindme.ui.task.TaskPreferencesHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationWorker(
        context: Context, workerParams: WorkerParameters
                        ) : Worker(context, workerParams) {

    override fun doWork(): Result {

        return try {
            val upcomingTasks = getPendingTasksForUpcomingWeek()
            if (upcomingTasks.isNotEmpty()) {
                createNotification(upcomingTasks)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun getPendingTasksForUpcomingWeek(): List<String> {
        val tasks = TaskPreferencesHelper.loadTasks(applicationContext)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Get today's date and the date seven days from now
        val calendar = Calendar.getInstance()
        val today = dateFormat.parse(dateFormat.format(calendar.time))!!
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val nextWeek = dateFormat.parse(dateFormat.format(calendar.time))!!

        // Filter tasks that are due within the next seven days
        return tasks.filter { task ->
            val taskDate = task.getDueDateAsDate()
            taskDate.after(today) && taskDate.before(nextWeek)
        }
            // Take the first three tasks only
            .take(3)
            .map { it.title }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun createNotification(taskTitles: List<String>) {
        val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminder_channel"

        // Create a PendingIntent that will open the MainActivity when the notification is clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                                     )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId, "Task Reminders", NotificationManager.IMPORTANCE_DEFAULT
                                             )
            notificationManager.createNotificationChannel(channel)
        }

        // Join the first three task titles into a single string for the notification content
        val tasksSummary = taskTitles.joinToString(separator = "\n") { "- $it" }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Reminder: Tasks Pending This Week")
            .setContentText("You have the following tasks pending this week:")
            .setStyle(NotificationCompat.BigTextStyle().bigText(tasksSummary))
            .setSmallIcon(R.drawable.icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
