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
    private val onMatchClick: (JSONObject) -> Unit
) : RecyclerView.Adapter<TeamMatchesAdapter.MatchViewHolder>() {

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStage: TextView = view.findViewById(R.id.tvMatchStage)
        val tvDate: TextView = view.findViewById(R.id.tvMatchDate)
        val tvHomeName: TextView = view.findViewById(R.id.tvHomeName)
        val tvAwayName: TextView = view.findViewById(R.id.tvAwayName)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val ivHomeLogo: ImageView = view.findViewById(R.id.ivHomeLogo)
        val ivAwayLogo: ImageView = view.findViewById(R.id.ivAwayLogo)
        val tvStadium: TextView = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView = view.findViewById(R.id.tvReferee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        // 🔥 ФУНКЦІЯ-ШУКАЧ: Перевіряє декілька ключів, поки не знайде значення
        fun getValue(vararg keys: String): String {
            for (key in keys) {
                if (match.has(key) && !match.isNull(key)) {
                    val value = match.optString(key)
                    if (value.isNotEmpty() && value != "false" && value != "null") return value
                }
            }
            return ""
        }

        // 1. ШУКАЄМО ДАНІ ПО ВСІХ МОЖЛИВИХ КЛЮЧАХ
        val stage = getValue("stage_name", "league_name", "stage")
        val date = getValue("date")
        val time = getValue("time")
        
        // Назви команд (спочатку пробуємо team1_name, якщо нема - home_team_name, і т.д.)
        val team1 = getValue("team1_name", "home_team_name", "home_team", "team1")
        val team2 = getValue("team2_name", "away_team_name", "away_team", "team2")
        
        // Логотипи
        val logo1 = getValue("team1_logo", "home_team_logo", "home_logo", "team1_image")
        val logo2 = getValue("team2_logo", "away_team_logo", "away_logo", "team2_image")
        
        val score = getValue("score", "match_score")
        val stadium = getValue("stadium_name", "stadium", "place")
        val referee = getValue("referee_name", "referee", "official")

        // 2. ЗАПОВНЮЄМО UI
        holder.tvStage.text = if (stage.isNotEmpty()) stage else "ТУРНІР"
        holder.tvDate.text = "$date $time"
        
        holder.tvHomeName.text = team1
        holder.tvAwayName.text = team2

        // Рахунок
        if (score.contains(":") || score.contains("-")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
            holder.tvScore.textSize = 18f
        } else {
            holder.tvScore.text = if (time.isNotEmpty()) time else "VS"
            holder.tvScore.setTextColor(Color.parseColor("#00E676"))
            holder.tvScore.textSize = 16f
        }

        // Стадіон та Арбітр
        holder.tvStadium.text = if (stadium.isNotEmpty()) "🏟 $stadium" else ""
        holder.tvReferee.text = if (referee.isNotEmpty()) "👮‍♂️ $referee" else ""

        // Логотипи (з захистом від пустоти)
        if (logo1.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(logo1).into(holder.ivHomeLogo)
        } else {
            holder.ivHomeLogo.setImageResource(R.drawable.ic_player_placeholder)
        }

        if (logo2.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(logo2).into(holder.ivAwayLogo)
        } else {
            holder.ivAwayLogo.setImageResource(R.drawable.ic_player_placeholder)
        }

        // Клік
        holder.itemView.setOnClickListener {
            onMatchClick(match)
        }
    }

    override fun getItemCount() = matches.size
}
