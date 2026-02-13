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
        
        // Контейнери сторін (мають бути в item_timeline.xml)
        val layoutLeft: LinearLayout = view.findViewById(R.id.layoutLeft)
        val layoutRight: LinearLayout = view.findViewById(R.id.layoutRight)
        
        // Поля лівої сторони
        val ivTypeLeft: ImageView = view.findViewById(R.id.ivTypeLeft)
        val tvDescriptionLeft: TextView = view.findViewById(R.id.tvDescriptionLeft)
        
        // Поля правої сторони
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

        // Визначаємо текст опису (додаємо гравця, що вийшов, якщо це заміна)
        val description = if (!event.playerOutName.isNullOrEmpty()) {
            "${event.playerName} (замість ${event.playerOutName})"
        } else {
            event.playerName
        }

        // Скидаємо видимість обох сторін перед налаштуванням
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
                imageView.setImageResource(android.R.drawable.ic_btn_speak_now) // Тимчасово, поки немає своїх іконок
                imageView.setColorFilter(Color.WHITE)
            }
            "goal_og" -> {
                imageView.setImageResource(android.R.drawable.ic_btn_speak_now)
                imageView.setColorFilter(Color.RED)
                textView.text = "$description (автогол)"
            }
            "yellow_card" -> {
                imageView.setImageResource(android.R.drawable.checkbox_on_background)
                imageView.setColorFilter(Color.YELLOW)
            }
            "red_card" -> {
                imageView.setImageResource(android.R.drawable.checkbox_on_background)
                imageView.setColorFilter(Color.RED)
            }
            "substitution" -> {
                imageView.setImageResource(android.R.drawable.stat_notify_sync)
                imageView.setColorFilter(Color.GREEN)
            }
        }
    }

    override fun getItemCount() = events.size
}
