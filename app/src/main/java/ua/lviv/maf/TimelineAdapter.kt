package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimelineAdapter(private val events: List<TimelineEvent>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMinute: TextView = view.findViewById(R.id.tvEventMinute)
        val ivCenterIcon: ImageView = view.findViewById(R.id.ivCenterIcon)
        val layoutLeft: LinearLayout = view.findViewById(R.id.layoutLeft)
        val layoutRight: LinearLayout = view.findViewById(R.id.layoutRight)
        val tvDescriptionLeft: TextView = view.findViewById(R.id.tvDescriptionLeft)
        val tvDescriptionRight: TextView = view.findViewById(R.id.tvDescriptionRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val event = events[position]

        holder.tvMinute.text = "${event.minute}'"

        val description = if (!event.player_out_name.isNullOrEmpty()) {
            "${event.player_name}\n(замість ${event.player_out_name})"
        } else {
            event.player_name
        }

        // 🔴 ВАЖЛИВО: повний reset стану (через RecyclerView recycling)
        holder.layoutLeft.visibility = View.GONE
        holder.layoutRight.visibility = View.GONE
        holder.tvDescriptionLeft.text = ""
        holder.tvDescriptionRight.text = ""

        // Логіка розведення сторін
        if (event.side == "left") {
            holder.layoutLeft.visibility = View.VISIBLE
            holder.tvDescriptionLeft.text = description
        } else {
            holder.layoutRight.visibility = View.VISIBLE
            holder.tvDescriptionRight.text = description
        }

        setupCenterIcon(holder.ivCenterIcon, event)
    }

    private fun setupCenterIcon(imageView: ImageView, event: TimelineEvent) {
    imageView.clearColorFilter() // Очищуємо старі кольори перед встановленням нових

    when (event.type) {
        "goal" -> {
            imageView.setImageResource(R.drawable.ic_ball)
        }
        "goal_og" -> { // Автогол
            imageView.setImageResource(R.drawable.ic_ball)
            imageView.setColorFilter(Color.RED)
        }
        "yellow_card" -> {
            // ЗМІНЕНО: тепер використовуємо іконку картки
            imageView.setImageResource(R.id.ic_card) 
            imageView.setColorFilter(Color.parseColor("#FFD700"))
        }
        "red_card" -> {
            // ЗМІНЕНО: тепер використовуємо іконку картки
            imageView.setImageResource(R.id.ic_card)
            imageView.setColorFilter(Color.RED)
        }
        "second_yellow" -> {
            // ДОДАНО: іконка другої жовтої
            imageView.setImageResource(R.drawable.ic_second_yellow)
        }
        "substitution" -> {
            // ЗМІНЕНО: використовуємо іконку заміни, а не м'яч
            imageView.setImageResource(R.drawable.ic_substitution)
        }
        "penalty_goal" -> {
            // ДОДАНО: м'яч з літерою P
            imageView.setImageResource(R.drawable.ic_penalty_goal)
        }
        else -> {
            imageView.setImageResource(R.drawable.ic_ball)
        }
    }
}

    override fun getItemCount() = events.size
}
