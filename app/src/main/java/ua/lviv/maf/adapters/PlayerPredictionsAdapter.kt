package ua.lviv.maf.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.lviv.maf.R
import ua.lviv.maf.models.PlayerMatchPrediction

class PlayerPredictionsAdapter(
    private val matches: List<PlayerMatchPrediction>
) : RecyclerView.Adapter<PlayerPredictionsAdapter.MatchViewHolder>() {

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMatchTeams: TextView = view.findViewById(R.id.tvMatchTeams)
        val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)
        val tvPlayerPrediction: TextView = view.findViewById(R.id.tvPlayerPrediction)
        val tvRealScore: TextView = view.findViewById(R.id.tvRealScore)
        val tvMatchPoints: TextView = view.findViewById(R.id.tvMatchPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_prediction, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = matches[position]

        holder.tvMatchTeams.text = item.teams
        holder.tvMatchDate.text = item.date
        holder.tvPlayerPrediction.text = item.userPrediction
        holder.tvRealScore.text = item.realScore
        holder.tvMatchPoints.text = item.points
    }

    override fun getItemCount(): Int = matches.size
}