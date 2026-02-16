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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        // Копіюємо логіку витягування даних
        val league = match.optString("league_name")
        val stage = match.optString("stage_name")
        val team1 = match.optString("team1_name")
        val team2 = match.optString("team2_name")
        val score = match.optString("score")
        val logo1 = match.optString("team1_logo").replace("http://", "https://")
        val logo2 = match.optString("team2_logo").replace("http://", "https://")

        // 🟢 Турнір (Зелений) та Етап
        holder.tvTournament.text = league.uppercase()
        holder.tvTournament.setTextColor(Color.parseColor("#00E676"))
        holder.tvStage.text = stage
        
        holder.tvDate.text = "${match.optString("date")} ${match.optString("time")}"
        holder.tvHomeName.text = team1
        holder.tvAwayName.text = team2

        // 📦 Віконечко рахунку
        if (score.contains(" : ")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
        } else {
            holder.tvScore.text = match.optString("time").ifEmpty { "VS" }
            holder.tvScore.setTextColor(Color.parseColor("#00E676"))
        }

        // Стадіон та Арбітр
        holder.tvStadium.text = "🏟 " + match.optString("stadium_name")
        holder.tvReferee.text = "👮‍♂️ " + match.optString("referee_name")

        // Логотипи
        Glide.with(holder.itemView.context).load(logo1).placeholder(R.drawable.ic_player_placeholder).into(holder.ivHomeLogo)
        Glide.with(holder.itemView.context).load(logo2).placeholder(R.drawable.ic_player_placeholder).into(holder.ivAwayLogo)

        holder.itemView.setOnClickListener { onMatchClick(match) }
    }

    override fun getItemCount() = matches.size
}
