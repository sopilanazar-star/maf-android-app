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
        val tvTournament: TextView = view.findViewById(R.id.tvTournament) // Нове
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

        // 🕵️‍♂️ ГОНЧІ ПСИ: Шукаємо значення по всіх можливих ключах
        fun getValue(vararg keys: String): String {
            for (key in keys) {
                if (match.has(key) && !match.isNull(key)) {
                    val v = match.optString(key)
                    if (v.isNotEmpty() && v != "false" && v != "null") return v
                }
            }
            return ""
        }

        // --- ВИДОБУВАЄМО ДАНІ ---
        // Турнір та Етап
        val tournament = getValue("league_name", "league", "competition_name")
        val stage = getValue("stage_name", "stage", "round")
        
        val date = getValue("date")
        val time = getValue("time")
        
        // Назви команд
        val team1 = getValue("home_team_name", "home_team", "team1_name", "team1")
        val team2 = getValue("away_team_name", "away_team", "team2_name", "team2")
        
        // Логотипи (Шукаємо скрізь!)
        val logo1 = getValue("home_team_logo", "home_logo", "team1_logo", "team1_image")
        val logo2 = getValue("away_team_logo", "away_logo", "team2_logo", "team2_image")
        
        val score = getValue("score", "match_score", "full_time_score")
        val stadium = getValue("stadium_name", "stadium", "place")
        val referee = getValue("referee_name", "referee")

        // --- ЗАПОВНЮЄМО ІНТЕРФЕЙС ---
        
        // Турнір (Зелений)
        holder.tvTournament.text = if (tournament.isNotEmpty()) tournament else "ТУРНІР"
        
        // Етап (Сірий)
        holder.tvStage.text = if (stage.isNotEmpty()) stage else ""
        holder.tvStage.visibility = if (stage.isNotEmpty()) View.VISIBLE else View.GONE
        
        holder.tvDate.text = "$date $time"
        holder.tvHomeName.text = team1
        holder.tvAwayName.text = team2

        // Рахунок / Час
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

        // Логотипи (з перевіркою)
        if (logo1.isNotEmpty()) Glide.with(holder.itemView.context).load(logo1).into(holder.ivHomeLogo)
        else holder.ivHomeLogo.setImageResource(R.drawable.ic_player_placeholder)

        if (logo2.isNotEmpty()) Glide.with(holder.itemView.context).load(logo2).into(holder.ivAwayLogo)
        else holder.ivAwayLogo.setImageResource(R.drawable.ic_player_placeholder)

        // Клік - передаємо весь JSON, щоб нічого не загубити
        holder.itemView.setOnClickListener { onMatchClick(match) }
    }

    override fun getItemCount() = matches.size
}
