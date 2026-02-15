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

        // --- ЛОГІКА ВІДОБРАЖЕННЯ ЗАМІНИ ---
        val description = if (event.type == "substitution" && !event.player_out_name.isNullOrEmpty()) {
            // Виводимо гравця, що вийшов, а під ним — замість кого
            "${event.player_name}\n(вийшов замість ${event.player_out_name})"
        } else {
            // Для всіх інших подій (голи, картки) — просто ім'я
            event.player_name
        }

        // Скидання видимості перед заповненням
        holder.layoutLeft.visibility = View.INVISIBLE
        holder.layoutRight.visibility = View.INVISIBLE
        holder.tvDescriptionLeft.text = ""
        holder.tvDescriptionRight.text = ""

        // Розподіл по сторонах (ліва/права команда)
        if (event.side == "left") {
            holder.layoutLeft.visibility = View.VISIBLE
            holder.tvDescriptionLeft.text = description
        } else if (event.side == "right") {
            holder.layoutRight.visibility = View.VISIBLE
            holder.tvDescriptionRight.text = description
        }

        setupCenterIcon(holder.ivCenterIcon, event)
    }

    private fun setupCenterIcon(imageView: ImageView, event: TimelineEvent) {

    // 🔴 повний ресет перед новим drawable
    imageView.setImageDrawable(null)
    imageView.clearColorFilter()
    imageView.imageTintList = null
    imageView.visibility = View.VISIBLE

    when (event.type) {

        "goal" -> {
            imageView.setImageResource(R.drawable.ic_ball)
        }

        "goal_pen" -> {
            imageView.setImageResource(R.drawable.ic_penalty_goal)
        }

        "goal_og" -> {
            imageView.setImageResource(R.drawable.ic_ball)
            imageView.setColorFilter(Color.RED)
        }

        "yellow_card" -> {
            imageView.setImageResource(R.drawable.ic_card)
            imageView.setColorFilter(Color.parseColor("#FFEB3B"))
        }

        "red_card" -> {
            imageView.setImageResource(R.drawable.ic_card)
            imageView.setColorFilter(Color.RED)
        }

        "yellow_red" -> {
            imageView.setImageResource(R.drawable.ic_second_yellow)
        }

        "substitution" -> {
            imageView.setImageResource(R.drawable.ic_substitution)
        }

        else -> {
            imageView.visibility = View.INVISIBLE
        }
    }
}

    override fun getItemCount() = events.size
}
