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
        val layoutLeft: LinearLayout = view.findViewById(R.id.layoutLeft)
        val layoutRight: LinearLayout = view.findViewById(R.id.layoutRight)
        
        val ivTypeLeft: ImageView = view.findViewById(R.id.ivTypeLeft)
        val tvDescriptionLeft: TextView = view.findViewById(R.id.tvDescriptionLeft)
        
        val ivTypeRight: ImageView = view.findViewById(R.id.ivTypeRight)
        val tvDescriptionRight: TextView = view.findViewById(R.id.tvDescriptionRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val event = events[position]
        holder.tvMinute.text = "${event.minute}'"

        val description = if (!event.playerOutName.isNullOrEmpty()) {
            "${event.playerName} (замість ${event.playerOutName})"
        } else {
            event.playerName
        }

        if (event.side == "left") {
            holder.layoutLeft.visibility = View.VISIBLE
            holder.layoutRight.visibility = View.GONE
            setupEventView(holder.ivTypeLeft, holder.tvDescriptionLeft, event, description)
        } else {
            holder.layoutRight.visibility = View.VISIBLE
            holder.layoutLeft.visibility = View.GONE
            setupEventView(holder.ivTypeRight, holder.tvDescriptionRight, event, description)
        }
    }

    private fun setupEventView(imageView: ImageView, textView: TextView, event: TimelineEvent, description: String) {
        textView.text = description
        imageView.clearColorFilter()
        
        when (event.type) {
            "goal" -> {
                // ВИПРАВЛЕНО: Використовуємо твій новий м'яч
                imageView.setImageResource(R.drawable.ic_ball) 
                imageView.setColorFilter(null) // Прибираємо фільтр, якщо іконка вже біла
            }
            "goal_og" -> {
                imageView.setImageResource(R.drawable.ic_ball)
                imageView.setColorFilter(Color.RED) // Червоний м'яч для автогола
                textView.text = "$description (автогол)"
            }
            "yellow_card" -> {
                // ВИПРАВЛЕНО: Спеціальна іконка картки
                imageView.setImageResource(R.drawable.ic_card)
                imageView.setColorFilter(Color.parseColor("#FFD700")) // Золотистий/Жовтий
            }
            "red_card" -> {
                imageView.setImageResource(R.drawable.ic_card)
                imageView.setColorFilter(Color.RED)
            }
            "substitution" -> {
                // ВИПРАВЛЕНО: Стрілочки заміни
                imageView.setImageResource(R.drawable.ic_substitution)
                imageView.setColorFilter(null)
            }
        }
    }

    override fun getItemCount() = events.size
}
