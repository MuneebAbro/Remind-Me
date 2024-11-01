@file:Suppress("DEPRECATION")

package com.muneeb.remindme.ui.task

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.muneeb.remindme.R.color
import com.muneeb.remindme.R.id
import com.muneeb.remindme.R.layout

class TaskAdapter(
        private var taskList: List<Task>,
        private val onItemClick: (Task, Int) -> Unit,
        private val onItemLongClick: (Task, Int) -> Unit
                 ) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(id.titleEditText)
        val taskTextView: TextView = itemView.findViewById(id.taskEditText)
        val deadlineTextView: TextView = itemView.findViewById(id.dueDateTextView)
        val cardView: CardView = itemView as CardView

        init {
            itemView.setOnClickListener {
                onItemClick(taskList[adapterPosition], adapterPosition)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(taskList[adapterPosition], adapterPosition)
                true
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTasks(newTasks: List<Task>) {
        taskList = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val (title, task, deadline, completed) = taskList[position]

        holder.titleTextView.text = title.replaceFirstChar { it.uppercase() }
        holder.taskTextView.text = task.replaceFirstChar { it.uppercase() }
        holder.deadlineTextView.text = "Due: $deadline"

        // Apply strikethrough for completed tasks
        if (completed) {
            holder.titleTextView.paintFlags = holder.titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskTextView.paintFlags = holder.taskTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.titleTextView.paintFlags = holder.titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskTextView.paintFlags = holder.taskTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        val colorRes = when (position % 5) {
            0 -> color.light_blue1
            1 -> color.purple
            2 -> color.gold
            3 -> color.sea_green
            4 -> color.pink
            else -> color.gold
        }
        holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                        holder.cardView.context,
                        colorRes
                                      )
                                              )
    }

    override fun getItemCount(): Int = taskList.size
}

