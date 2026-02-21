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

class StandingAdapter(private var items: List<StandingRow>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TEAM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].is_group_header) TYPE_HEADER else TYPE_TEAM
    }

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val ivTeamLogo: ImageView = view.findViewById(R.id.ivTeamLogo)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val tvGames: TextView = view.findViewById(R.id.tvGames)
        val tvGoalsDiff: TextView = view.findViewById(R.id.tvGoalsDiff)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val layoutForm: LinearLayout = view.findViewById(R.id.layoutForm)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupName: TextView = view.findViewById(R.id.tvGroupName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(inflater.inflate(R.layout.item_standing_header, parent, false))
        } else {
            TeamViewHolder(inflater.inflate(R.layout.item_standing, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is HeaderViewHolder) {
            holder.tvGroupName.text = item.group_name ?: "Група"
            return
        }

        if (holder is TeamViewHolder) {
            holder.tvPosition.text = "${item.position}."
            holder.tvTeamName.text = item.team_name
            holder.tvGames.text = item.games.toString()
            holder.tvGoalsDiff.text = "${item.goals_for}-${item.goals_against}"
            holder.tvPoints.text = item.points.toString()

            drawForm(holder.layoutForm, item.form)

            Glide.with(holder.itemView.context)
                .load(item.logo)
                .placeholder(R.drawable.ic_ball)
                .into(holder.ivTeamLogo)

            // 🔥 ТЕПЕР ПЕРЕДАЄМО ЛОГОТИП КОМАНДИ
            holder.itemView.setOnClickListener {
                if (item.team_id.isNotEmpty() && item.team_id != "0") {
                    val intent = Intent(it.context, TeamPlayersActivity::class.java)
                    intent.putExtra("team_id", item.team_id)
                    intent.putExtra("team_name", item.team_name)
                    intent.putExtra("team_logo", item.logo) // ОСЬ ЦЕЙ РЯДОК ВСЕ ПОЛАГОДИТЬ
                    it.context.startActivity(intent)
                }
            }
        }
    }

    private fun drawForm(layout: LinearLayout, formList: List<String>?) {
        layout.removeAllViews()
        if (formList.isNullOrEmpty()) return
        val density = layout.context.resources.displayMetrics.density
        val size = (10 * density).toInt()
        val margin = (2 * density).toInt()

        formList.forEach { result ->
            val circle = View(layout.context)
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            circle.layoutParams = params
            val color = when (result.uppercase()) {
                "W" -> Color.parseColor("#4CAF50")
                "D" -> Color.parseColor("#9E9E9E")
                "L" -> Color.parseColor("#F44336")
                else -> Color.TRANSPARENT
            }
            circle.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            layout.addView(circle)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<StandingRow>) {
        items = newItems
        notifyDataSetChanged()
    }
}
