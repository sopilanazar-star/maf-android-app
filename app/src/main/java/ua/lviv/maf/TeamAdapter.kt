package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class TeamAdapter(private val items: List<StandingRow>) :
    RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    inner class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val positionMarker: View = view.findViewById(R.id.positionMarker)
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val ivTeamLogo: ImageView = view.findViewById(R.id.ivTeamLogo)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val tvGames: TextView = view.findViewById(R.id.tvGames)
        val tvGoalsDiff: TextView = view.findViewById(R.id.tvGoalsDiff)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        
        val layoutForm: LinearLayout = view.findViewById(R.id.layoutForm)
        val layoutFormContainer: LinearLayout = view.findViewById(R.id.layoutFormContainer)
        val ivExpand: TextView = view.findViewById(R.id.ivExpand)
        val expandableLayout: LinearLayout = view.findViewById(R.id.expandableLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_row_redesigned, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val item = items[position]

        holder.tvPosition.text = item.position.toString()
        holder.tvTeamName.text = item.team_name
        holder.tvGames.text = item.games.toString()
        holder.tvGoalsDiff.text = "${item.goals_for}-${item.goals_against}"
        holder.tvPoints.text = item.points.toString()

        // Кольори
        val isFirstPlace = position == 0
        val isLastPlace = position == items.size - 1
        when {
            isFirstPlace -> {
                holder.positionMarker.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.tvPosition.setTextColor(Color.parseColor("#FFFFFF"))
            }
            isLastPlace && items.size > 1 -> {
                holder.positionMarker.setBackgroundColor(Color.parseColor("#F44336"))
                holder.tvPosition.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else -> {
                holder.positionMarker.setBackgroundColor(Color.TRANSPARENT)
                holder.tvPosition.setTextColor(Color.parseColor("#BCBCBC"))
            }
        }

        // Форма
        drawForm(holder.layoutForm, item.form)

        Glide.with(holder.itemView.context)
            .load(item.logo)
            .placeholder(R.drawable.ic_ball)
            .into(holder.ivTeamLogo)

        // ШТОРКА
        val isExpanded = item.isExpanded
        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivExpand.rotation = if (isExpanded) 180f else 0f

        holder.layoutFormContainer.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }

        drawExpandedMatches(holder.expandableLayout, item.form)

        // Клік по всій команді
        holder.itemView.setOnClickListener {
            if (item.team_id.isNotEmpty() && item.team_id != "0") {
                val intent = Intent(it.context, TeamPlayersActivity::class.java)
                intent.putExtra("team_id", item.team_id)
                intent.putExtra("team_name", item.team_name)
                intent.putExtra("team_logo", item.logo)
                it.context.startActivity(intent)
            }
        }
    }

    private fun drawForm(layout: LinearLayout, formList: List<String>?) {
        layout.removeAllViews()
        if (formList.isNullOrEmpty()) return

        val density = layout.context.resources.displayMetrics.density
        val size = (14 * density).toInt()
        val margin = (2 * density).toInt()

        formList.takeLast(5).forEach { result ->
            val circle = TextView(layout.context)
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            circle.layoutParams = params
            circle.gravity = android.view.Gravity.CENTER
            circle.textSize = 8f
            circle.setTextColor(Color.WHITE)
            circle.text = result.take(1).uppercase()

            val color = when (result.uppercase(Locale.getDefault())) {
                "W", "В" -> Color.parseColor("#4CAF50")
                "D", "Н" -> Color.parseColor("#9E9E9E")
                "L", "П" -> Color.parseColor("#F44336")
                else -> Color.TRANSPARENT
            }

            circle.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            layout.addView(circle)
        }
    }

    private fun drawExpandedMatches(layout: LinearLayout, formList: List<String>?) {
        layout.removeAllViews()
        if (formList.isNullOrEmpty()) {
            val tvEmpty = TextView(layout.context)
            tvEmpty.text = "Немає даних"
            tvEmpty.setTextColor(Color.parseColor("#9E9E9E"))
            tvEmpty.textSize = 12f
            layout.addView(tvEmpty)
            return
        }

        formList.takeLast(5).forEachIndexed { index, result ->
            val matchRow = TextView(layout.context)
            val resultWord = when (result.uppercase(Locale.getDefault())) {
                "W", "В" -> "Перемога"
                "D", "Н" -> "Нічия"
                "L", "П" -> "Поразка"
                else -> "Матч"
            }
            matchRow.text = "Матч ${index + 1}: $resultWord"
            matchRow.setTextColor(Color.WHITE)
            matchRow.textSize = 13f
            matchRow.setPadding(0, 8, 0, 8)
            layout.addView(matchRow)
        }
    }

    override fun getItemCount() = items.size
}
