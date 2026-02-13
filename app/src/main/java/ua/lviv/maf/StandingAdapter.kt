package ua.lviv.maf

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Переконайся, що Glide доданий у build.gradle

class StandingAdapter(private var items: List<StandingRow>) :
    RecyclerView.Adapter<StandingAdapter.StandingViewHolder>() {

    class StandingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val ivTeamLogo: ImageView = view.findViewById(R.id.ivTeamLogo)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val tvGames: TextView = view.findViewById(R.id.tvGames)
        val tvGoalsDiff: TextView = view.findViewById(R.id.tvGoalsDiff)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)

        // Ці поля є тільки в layout-land, тому робимо їх опціональними
        val tvWins: TextView? = view.findViewById(R.id.tvWins)
        val tvDraws: TextView? = view.findViewById(R.id.tvDraws)
        val tvLosses: TextView? = view.findViewById(R.id.tvLosses)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_standing, parent, false)
        return StandingViewHolder(view)
    }

    override fun onBindViewHolder(holder: StandingViewHolder, position: Int) {
        val item = items[position]

        holder.tvPosition.text = "${item.position}."
        holder.tvTeamName.text = item.team_name
        holder.tvGames.text = item.games.toString()
        holder.tvGoalsDiff.text = "${item.goals_for}-${item.goals_against}"
        holder.tvPoints.text = item.points.toString()

        // Заповнюємо додаткові поля для горизонтального режиму, якщо вони існують
        holder.tvWins?.text = item.win.toString()
        holder.tvDraws?.text = item.draw.toString()
        holder.tvLosses?.text = item.loss.toString()

        // Завантаження логотипа через Glide
        Glide.with(holder.itemView.context)
            .load(item.logo)
            .placeholder(R.drawable.ic_ball) // Заглушка, поки вантажиться
            .into(holder.ivTeamLogo)

        // Клік по команді — перехід до списку гравців
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, TeamPlayersActivity::class.java).apply {
                putExtra("team_id", item.team_id)
                putExtra("team_name", item.team_name)
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<StandingRow>) {
        items = newItems
        notifyDataSetChanged()
    }
}
