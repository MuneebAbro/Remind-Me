@file:Suppress("DEPRECATION")
package com.muneeb.remindme.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.muneeb.remindme.databinding.FragmentDashboardBinding
import com.muneeb.remindme.ui.calender.CalenderActivity
import com.muneeb.remindme.ui.task.TaskViewModel
import com.muneeb.remindme.utility.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var taskViewModel : TaskViewModel
    private var _binding : FragmentDashboardBinding? = null
    private val binding get() = _binding !!

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root : View = binding.root
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        val intent = Intent(requireContext(), CalenderActivity::class.java)
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        binding.constraintLayoutDash.setOnClickListener {
            vibrator.vibrate(vibrationEffect)
            startActivity(intent)

        }
        binding.calCardView.setOnClickListener {

            vibrator.vibrate(vibrationEffect)
            startActivity(intent)
        }

        fun getCurrentTime() : String {
            val currentTime = Date()
            val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
            return dateFormat.format(currentTime)
        }
        // Load tasks with context
        taskViewModel.loadTasks(requireContext())

        val greeting : String
        val time = getCurrentTime()
        greeting = if (time >= "06" && time < "12") {
            "Good Morning"
        }
        else if (time >= "12" && time < "17") {
            "Afternoon"
        }
        else if (time >= "17" && time < "21") {
            "Good Evening"
        }
        else {
            "Good Night"
        }

        // Set up the username
        var username = PreferenceManager.getUserName(requireContext())
        username = username?.substringBefore(" ")
        "$greeting ${username?.replaceFirstChar { it.uppercase() }}!".also {
            binding.textView.text = it
        }

        // Date
        val currentDate = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())
        binding.currentDateTV.text = currentDate

        // Observing total tasks
        taskViewModel.totalTasks.observe(viewLifecycleOwner) { count ->
            binding.totalTaskTV.text = count.toString()
        }

        // Observing current date task count
        taskViewModel.currentDateTaskCount.observe(viewLifecycleOwner) { count ->
            "$count Reminders".also { binding.currentDateTaskCountTV.text = it }

        }

        taskViewModel.upcomingTasksCount.observe(viewLifecycleOwner) { count ->
            "$count".also { binding.pendingTaskTV.text = it }
        }


        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh the data or UI when the fragment becomes visible
        taskViewModel.loadTasks(requireContext()) // Reload the tasks from storage
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





