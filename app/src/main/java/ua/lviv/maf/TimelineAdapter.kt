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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val event = events[position]
        holder.tvMinute.text = "${event.minute}'"

        // ПРАВИЛЬНІ НАЗВИ ЗМІННИХ (player_name та player_out_name)
        val description = if (!event.player_out_name.isNullOrEmpty()) {
            "${event.player_name}\n(замість ${event.player_out_name})"
        } else {
            event.player_name
        }

        // Логіка розведення сторін (Версія 2.0)
        if (event.side == "left") {
            holder.layoutLeft.visibility = View.VISIBLE
            holder.layoutRight.visibility = View.GONE  // Змінюємо тут
            holder.tvDescriptionLeft.text = description
        } else {
            holder.layoutRight.visibility = View.VISIBLE
            holder.layoutLeft.visibility = View.GONE   // І тут
            holder.tvDescriptionRight.text = description
        }

        setupCenterIcon(holder.ivCenterIcon, event)
    }

    private fun setupCenterIcon(imageView: ImageView, event: TimelineEvent) {
        imageView.clearColorFilter()
        
        // ВИПРАВЛЕНО: Звертаємося до drawable, а не до id
        when (event.type) {
            "goal" -> {
                imageView.setImageResource(R.drawable.ic_ball)
            }
            "goal_og" -> {
                imageView.setImageResource(R.drawable.ic_ball)
                imageView.setColorFilter(Color.RED)
            }
            "yellow_card" -> {
                imageView.setImageResource(R.drawable.ic_ball) 
                imageView.setColorFilter(Color.parseColor("#FFD700"))
            }
            "red_card" -> {
                imageView.setImageResource(R.drawable.ic_ball)
                imageView.setColorFilter(Color.RED)
            }
            "substitution" -> {
                imageView.setImageResource(R.drawable.ic_ball)
                imageView.setColorFilter(Color.GREEN)
            }
        }
    }

    override fun getItemCount() = events.size
    }
    
