package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONObject

class TeamMatchesAdapter(
    private val matches: List<JSONObject>,
    private val onMatchClick: (String) -> Unit // Передаємо ID матчу при кліку
) : RecyclerView.Adapter<TeamMatchesAdapter.MatchViewHolder>() {

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStage: TextView = view.findViewById(R.id.tvMatchStage)
        val tvDate: TextView = view.findViewById(R.id.tvMatchDate)
        val tvHomeName: TextView = view.findViewById(R.id.tvHomeName)
        val tvAwayName: TextView = view.findViewById(R.id.tvAwayName)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val ivHomeLogo: ImageView = view.findViewById(R.id.ivHomeLogo)
        val ivAwayLogo: ImageView = view.findViewById(R.id.ivAwayLogo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        // Отримуємо дані з JSON
        val stage = match.optString("stage_name") // Назва етапу
        val date = match.optString("date")
        val time = match.optString("time")
        val team1 = match.optString("team1_name")
        val team2 = match.optString("team2_name")
        val logo1 = match.optString("team1_logo")
        val logo2 = match.optString("team2_logo")
        val score = match.optString("score")

        // 1. ЕТАП (В лівому куті)
        if (stage.isNotEmpty()) {
            holder.tvStage.text = stage
            holder.tvStage.visibility = View.VISIBLE
        } else {
            holder.tvStage.visibility = View.GONE
        }

        // 2. ДАТА
        holder.tvDate.text = "$date $time"

        // 3. НАЗВИ КОМАНД
        holder.tvHomeName.text = team1
        holder.tvAwayName.text = team2

        // 4. РАХУНОК (Якщо матчу ще не було — показуємо VS або час)
        if (score.contains(":")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
            holder.tvScore.textSize = 20f
        } else {
            holder.tvScore.text = time // Або "VS"
            holder.tvScore.setTextColor(Color.parseColor("#00E676"))
            holder.tvScore.textSize = 16f
        }

        // 5. ЛОГОТИПИ
        Glide.with(holder.itemView.context).load(logo1).into(holder.ivHomeLogo)
        Glide.with(holder.itemView.context).load(logo2).into(holder.ivAwayLogo)

        // 6. КЛІК ПО МАТЧУ
        holder.itemView.setOnClickListener {
            onMatchClick(match.optString("id"))
        }
    }

    override fun getItemCount() = matches.size
}
