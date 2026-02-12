package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimelineAdapter(private val events: List<TimelineEvent>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMinute: TextView = view.findViewById(R.id.tvEventMinute)
        val ivType: ImageView = view.findViewById(R.id.ivEventType)
        val tvDescription: TextView = view.findViewById(R.id.tvEventDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val event = events[position]
        holder.tvMinute.text = "${event.minute}'"
        holder.tvDescription.text = event.playerName

        holder.ivType.clearColorFilter()

        when (event.type) {
            "goal" -> {
                holder.ivType.setImageResource(android.R.drawable.ic_btn_speak_now) // Схоже на м'яч
                holder.ivType.setColorFilter(Color.WHITE)
            }
            "goal_og" -> {
                holder.ivType.setImageResource(android.R.drawable.ic_btn_speak_now)
                holder.ivType.setColorFilter(Color.RED)
                holder.tvDescription.text = "${event.playerName} (автогол)"
            }
            "yellow_card" -> {
                holder.ivType.setImageResource(android.R.drawable.checkbox_on_background)
                holder.ivType.setColorFilter(Color.YELLOW)
            }
            "red_card" -> {
                holder.ivType.setImageResource(android.R.drawable.checkbox_on_background)
                holder.ivType.setColorFilter(Color.RED)
            }
            "substitution" -> {
                holder.ivType.setImageResource(android.R.drawable.stat_notify_sync)
                holder.ivType.setColorFilter(Color.GREEN)
            }
        }
    }

    override fun getItemCount() = events.size
}
