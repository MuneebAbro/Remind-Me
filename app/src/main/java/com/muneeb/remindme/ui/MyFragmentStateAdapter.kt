package com.muneeb.remindme.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.muneeb.remindme.ui.dashboard.DashboardFragment
import com.muneeb.remindme.ui.task.TaskFragment

class MyFragmentStateAdapter(activity : FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() : Int = 2 // Number of fragments

    override fun createFragment(position : Int) : Fragment {
        return when (position) {
            0    -> DashboardFragment()
            1    -> TaskFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
