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
    private val onMatchClick: (String) -> Unit
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

        val id = match.optString("id")
        val league = match.optString("league")
        val stage = match.optString("stage")
        val date = match.optString("date")
        val team1 = match.optString("team1")
        val team2 = match.optString("team2")
        val logo1 = match.optString("logo1")
        val logo2 = match.optString("logo2")
        val score = match.optString("score")

        // 1. ЕТАП ТА ЛІГА
        val fullInfo = if (league == stage || stage.isEmpty()) league else "$league | $stage"
        holder.tvStage.text = fullInfo
        holder.tvStage.visibility = if (fullInfo.isNotEmpty()) View.VISIBLE else View.GONE

        // 2. ДАТА
        holder.tvDate.text = date

        // 3. КОМАНДИ
        holder.tvHomeName.text = team1
        holder.tvAwayName.text = team2

        // 4. 🔥 РАХУНОК (З ПРАВИЛЬНИМ ФОНОМ)
        if (score.contains(" : ")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
            holder.tvScore.textSize = 20f
            // Вмикаємо той самий файл фону, через який сварився Gradle
            holder.tvScore.setBackgroundResource(R.drawable.bg_score_container)
        } else {
            // Якщо матчу ще не було (час або VS)
            holder.tvScore.text = if (score.isNotEmpty()) score else "VS"
            holder.tvScore.setTextColor(Color.parseColor("#00E676"))
            holder.tvScore.textSize = 16f
            // Прибираємо фон, щоб VS не було в рамці
            holder.tvScore.background = null
        }

        // 5. ЛОГОТИПИ
        Glide.with(holder.itemView.context)
            .load(logo1.replace("http://", "https://"))
            .into(holder.ivHomeLogo)
        Glide.with(holder.itemView.context)
            .load(logo2.replace("http://", "https://"))
            .into(holder.ivAwayLogo)

        // 6. КЛІК
        holder.itemView.setOnClickListener { onMatchClick(id) }
    }

    override fun getItemCount() = matches.size
}
