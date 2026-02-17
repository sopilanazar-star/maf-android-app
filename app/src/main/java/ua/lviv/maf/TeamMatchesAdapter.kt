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
        val tvTournament: TextView = view.findViewById(R.id.tvTournament)
        val tvStage: TextView = view.findViewById(R.id.tvMatchStage)
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvStadium: TextView = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView = view.findViewById(R.id.tvReferee)
        
        // 🔥 ОСЬ ЦЕЙ РЯДОК ВИПРАВЛЯЄ ПОМИЛКУ В ЦЬОМУ ФАЙЛІ
        val ivTimeIcon: ImageView = view.findViewById(R.id.ivTimeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        fun getValue(vararg keys: String): String {
            for (key in keys) {
                if (match.has(key) && !match.isNull(key)) {
                    val v = match.optString(key)
                    if (v.isNotEmpty() && v != "false" && v != "null") return v
                }
            }
            return ""
        }

        val tournament = getValue("league_name", "league", "competition_name")
        val stage = getValue("stage_name", "stage", "round")
        val time = getValue("time", "start_time")
        
        val team1 = getValue("home_team_name", "team1_name", "home_team")
        val team2 = getValue("away_team_name", "team2_name", "away_team")
        val logo1 = getValue("home_team_logo", "team1_logo", "home_logo")
        val logo2 = getValue("away_team_logo", "team2_logo", "away_logo")
        
        val score = getValue("score", "match_score", "full_time_score")
        val stadium = getValue("stadium_name", "stadium", "place")
        val referee = getValue("referee_name", "referee")

        holder.tvTournament.text = if (tournament.isNotEmpty()) tournament.uppercase() else "ТУРНІР"
        holder.tvStage.text = if (stage.isNotEmpty()) stage else ""

        holder.tvTeam1.text = team1
        holder.tvTeam2.text = team2

        if (score.contains(":") || score.contains("-")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
            holder.ivTimeIcon.visibility = View.GONE
        } else {
            holder.tvScore.text = if (time.isNotEmpty()) time else "VS"
            holder.tvScore.setTextColor(Color.parseColor("#00E676"))
            holder.ivTimeIcon.visibility = View.VISIBLE
        }

        holder.tvStadium.text = if (stadium.isNotEmpty()) "🏟 $stadium" else ""
        holder.tvReferee.text = if (referee.isNotEmpty()) "Арбітр: $referee" else ""

        fun loadLogo(url: String, imageView: ImageView) {
            if (url.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(url.replace("http://", "https://"))
                    .placeholder(R.drawable.ic_player_placeholder)
                    .error(R.drawable.ic_player_placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_player_placeholder)
            }
        }

        loadLogo(logo1, holder.ivLogo1)
        loadLogo(logo2, holder.ivLogo2)

        holder.itemView.setOnClickListener { onMatchClick(match) }
    }

    override fun getItemCount() = matches.size
}
