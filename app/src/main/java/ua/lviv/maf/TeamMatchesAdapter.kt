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

        val stage = match.optString("stage_name")
        val date = match.optString("date")
        val time = match.optString("time")
        val team1 = match.optString("team1_name")
        val team2 = match.optString("team2_name")
        val logo1 = match.optString("team1_logo")
        val logo2 = match.optString("team2_logo")
        val score = match.optString("score") // Може бути "2 : 1" або просто час, якщо порожньо
        
        val stadium = match.optString("stadium_name")
        val referee = match.optString("referee_name")

        holder.tvStage.text = if (stage.isNotEmpty()) stage else "МАТЧ"
        holder.tvDate.text = "$date $time"
        holder.tvHomeName.text = team1
        holder.tvAwayName.text = team2

        // 🔥 Логіка відображення рахунку або часу
        // Перевіряємо: якщо є двокрапка або дефіс - це рахунок
        if (score.contains(":") || score.contains("-")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
            holder.tvScore.textSize = 20f // Трохи більше для рахунку
        } else {
            // Якщо матчу ще не було - показуємо час зеленим
            holder.tvScore.text = time
            holder.tvScore.setTextColor(Color.parseColor("#00E676"))
            holder.tvScore.textSize = 16f // Трохи менше для часу
        }

        // Стадіон та Арбітр з іконками
        holder.tvStadium.text = if (stadium.isNotEmpty()) "🏟 $stadium" else ""
        holder.tvReferee.text = if (referee.isNotEmpty()) "👮‍♂️ $referee" else ""

        Glide.with(holder.itemView.context).load(logo1).into(holder.ivHomeLogo)
        Glide.with(holder.itemView.context).load(logo2).into(holder.ivAwayLogo)

        holder.itemView.setOnClickListener {
            onMatchClick(match)
        }
    }

    override fun getItemCount() = matches.size
}
