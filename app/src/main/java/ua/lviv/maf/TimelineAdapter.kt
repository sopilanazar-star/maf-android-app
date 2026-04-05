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
import android.util.Log

class TimelineAdapter(
    private val events: List<TimelineEvent>,
    private val homeTeamId: Int
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPenaltyHeader: TextView = view.findViewById(R.id.tvPenaltyHeader) // <-- ВСТАВ ЦЕЙ РЯДОК
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
        // --- ВСТАВЛЯЙ ЦЕЙ БЛОК СЮДИ ---
        val type = event.type.trim()
        val isCurrentPen = type == "penalty_goal" || type == "penalty_miss" || type == "missed_penalty"

        if (isCurrentPen) {
            val isFirst = if (position > 0) {
                val prev = events[position - 1].type.trim()
                prev != "penalty_goal" && prev != "penalty_miss" && prev != "missed_penalty"
            } else true
            holder.tvPenaltyHeader.visibility = if (isFirst) View.VISIBLE else View.GONE
        } else {
            holder.tvPenaltyHeader.visibility = View.GONE
        }
        // --- КІНЕЦЬ БЛОКУ ---
        android.util.Log.d("TIMELINE", "BIND EVENT: " + event.type)
        holder.tvMinute.text = "${event.minute}'"

        val description: CharSequence = when (event.type.trim()) {
            "penalty_goal",
            "penalty_miss" -> event.player_name
            "substitution" -> {
                val sb = SpannableStringBuilder()

                val inText = "↑ ${event.player_name}"
                sb.append(inText)
                sb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00E676")),
                    0,
                    inText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

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

        holder.layoutLeft.visibility = View.GONE
        holder.layoutRight.visibility = View.GONE

        holder.tvDescriptionLeft.text = ""
        holder.tvDescriptionRight.text = ""

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
        Log.d("TIMELINE", "TYPE = " + event.type)
        imageView.setImageDrawable(null)
        imageView.clearColorFilter()
        imageView.visibility = View.VISIBLE

        when (event.type.trim()) {

            "goal" ->
                imageView.setImageResource(R.drawable.ic_ball)

            "penalty_goal" ->
                imageView.setImageResource(R.drawable.ic_penalty_goal)

            "penalty_miss", "missed_penalty" ->
                imageView.setImageResource(R.drawable.ic_penalty_missed)

            "own_goal" -> {
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

            "yellow_red" ->
                imageView.setImageResource(R.drawable.ic_second_yellow)

            "substitution" ->
                imageView.setImageResource(R.drawable.ic_substitution)

            else ->
                imageView.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = events.size
}