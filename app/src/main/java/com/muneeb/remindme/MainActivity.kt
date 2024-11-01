package com.muneeb.remindme

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.muneeb.remindme.databinding.ActivityMainBinding
import com.muneeb.remindme.ui.MyFragmentStateAdapter
import com.muneeb.remindme.ui.notification.NotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager : ViewPager2 = binding.viewPager
        val adapter = MyFragmentStateAdapter(this)
        viewPager.adapter = adapter

        val navView : BottomNavigationView = binding.navView

        // Set up listener to change the bottom navigation selection based on ViewPager2's current item
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position : Int) {
                when (position) {
                    0 -> navView.selectedItemId = R.id.navigation_dashboard
                    1 -> navView.selectedItemId = R.id.navigation_task
                }
            }
        })

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> viewPager.currentItem = 0
                R.id.navigation_task      -> viewPager.currentItem = 1
            }
            true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        else {
            scheduleNotificationWorker()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode : Int, permissions : Array<out String>, grantResults : IntArray
                                           ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleNotificationWorker()
        }
    }

    private fun scheduleNotificationWorker() {
        // Create constraints to ensure the work can run under specified conditions
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresDeviceIdle(false)
            // Work can run without network
            .build()

        // Build the PeriodicWorkRequest with the defined constraints
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(6, TimeUnit.HOURS)

            .setConstraints(constraints) // Apply the constraints
            .build()

        // Enqueue the work request with a unique name to ensure only one instance is running
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "remind",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
                                                                      )
    }


}
