package ua.lviv.maf

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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

        // 🔥 ОПИС ПОДІЇ
        val description: CharSequence = when (event.type) {

            "substitution" -> {
                val sb = SpannableStringBuilder()

                // ↑ гравець зайшов
                val inText = "↑ ${event.player_name}"
                sb.append(inText)
                sb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00E676")),
                    0,
                    inText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // ↓ кого замінили
                if (!event.player_out_name.isNullOrBlank()) {
                    sb.append("\n")

                    val start = sb.length
                    val outText = "↓ ${event.player_out_name}"
                    sb.append(outText)

                    sb.setSpan(
                        ForegroundColorSpan(Color.parseColor("#FF5252")),
                        start,
                        start + outText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                sb
            }

            else -> event.player_name
        }

        // reset
        holder.layoutLeft.visibility = View.INVISIBLE
        holder.layoutRight.visibility = View.INVISIBLE
        holder.tvDescriptionLeft.text = ""
        holder.tvDescriptionRight.text = ""

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

        imageView.setImageDrawable(null)
        imageView.clearColorFilter()
        imageView.imageTintList = null
        imageView.visibility = View.VISIBLE

        when (event.type) {

            "goal" -> imageView.setImageResource(R.drawable.ic_ball)

            "goal_pen" -> imageView.setImageResource(R.drawable.ic_penalty_goal)

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

            "yellow_red" -> imageView.setImageResource(R.drawable.ic_second_yellow)

            "substitution" -> imageView.setImageResource(R.drawable.ic_substitution)

            else -> imageView.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = events.size
}
