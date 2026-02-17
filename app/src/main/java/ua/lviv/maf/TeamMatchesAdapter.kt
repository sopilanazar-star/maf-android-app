package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
) : RecyclerView.Adapter<TeamMatchesAdapter.TeamMatchViewHolder>() {

    // Використовуємо унікальне ім'я ViewHolder, щоб не плутатись
    class TeamMatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTournament: TextView = view.findViewById(R.id.tvTournament)
        val tvStage: TextView = view.findViewById(R.id.tvMatchStage)
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvStadium: TextView = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView = view.findViewById(R.id.tvReferee)
        
        // Обов'язково додаємо іконку, щоб не було помилок
        val ivTimeIcon: ImageView = view.findViewById(R.id.ivTimeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMatchViewHolder {
        // Переконайся, що тут правильне ім'я XML файлу (item_team_match або item_match)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_match, parent, false) 
        return TeamMatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamMatchViewHolder, position: Int) {
        val match = matches[position]

        // Функція "пилосос": шукає значення по всіх можливих ключах
        fun getValue(vararg keys: String): String {
            for (key in keys) {
                if (match.has(key) && !match.isNull(key)) {
                    val v = match.optString(key)
                    if (v.isNotEmpty() && v != "false" && v != "null") return v
                }
            }
            return ""
        }

        // 1. ВИТЯГУЄМО ДАНІ (Тут ми беремо дані з JSON)
        val id = getValue("id")
        val tournament = getValue("league_name", "league", "competition_name")
        val stage = getValue("stage_name", "stage", "round")
        val dateFull = "${getValue("date")} ${getValue("time")}"
        
        val team1Name = getValue("home_team_name", "team1_name", "home_team", "team1")
        val team2Name = getValue("away_team_name", "team2_name", "away_team", "team2")
        
        val logo1Url = getValue(
    "logo1",                 // ← ДОДАТИ
    "home_team_logo",
    "team1_logo",
    "home_logo",
    "team1_image"
)

val logo2Url = getValue(
    "logo2",                 // ← ДОДАТИ
    "away_team_logo",
    "team2_logo",
    "away_logo",
    "team2_image"
)
        
        val score = getValue("score", "match_score", "full_time_score")
        val stadium = getValue("stadium_name", "stadium", "place")
        val referee = getValue("referee_name", "referee")
        
        val homeTeamId = getValue("home_team_id", "team1_id")
        val awayTeamId = getValue("away_team_id", "team2_id")
        val time = getValue("time", "start_time")

        // 2. ЗАПОВНЮЄМО КАРТКУ (UI)
        holder.tvTournament.text = if (tournament.isNotEmpty()) tournament.uppercase() else "ТУРНІР"
        holder.tvStage.text = stage
        holder.tvTeam1.text = team1Name
        holder.tvTeam2.text = team2Name

        // Логіка відображення рахунку (як у твоєму прикладі)
        if (score.contains(":") || score.contains("-")) {
            holder.tvScore.text = score
            holder.tvScore.setTextColor(Color.WHITE)
            holder.tvScore.textSize = 18f
            holder.tvScore.setTypeface(null, Typeface.BOLD)
            holder.ivTimeIcon.visibility = View.GONE 
        } else {
            holder.tvScore.text = if (time.isNotEmpty()) time else "VS"
            holder.tvScore.setTextColor(Color.parseColor("#BCBCBC"))
            holder.tvScore.textSize = 14f
            holder.tvScore.setTypeface(null, Typeface.NORMAL)
            holder.ivTimeIcon.visibility = View.VISIBLE 
        }

        holder.tvStadium.text = if (stadium.isNotEmpty()) "🏟 $stadium" else ""
        holder.tvReferee.text = if (referee.isNotEmpty()) "Арбітр: $referee" else ""

        // Логотипи через Glide
        fun loadLogo(url: String, imageView: ImageView) {
            if (url.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(url.replace("http://", "https://")) // Фікс для http
                    .placeholder(R.drawable.ic_player_placeholder)
                    .error(R.drawable.ic_player_placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_player_placeholder)
            }
        }
        loadLogo(logo1Url, holder.ivLogo1)
        loadLogo(logo2Url, holder.ivLogo2)

        // 3. 🔥 ГОЛОВНЕ: КЛІК ПО МАТЧУ (ПЕРЕДАЄМО ДАНІ В ШАПКУ)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MatchDetailActivity::class.java).apply {
                // Передаємо ключі ТОЧНО ТАК, як їх чекає Activity
                putExtra("id", id)
                
                // Назви команд
                putExtra("team1", team1Name) // Activity чекає "team1"
                putExtra("team2", team2Name) // Activity чекає "team2"
                
                // Логотипи
                putExtra("logo1", logo1Url)  // Activity чекає "logo1"
                putExtra("logo2", logo2Url)  // Activity чекає "logo2"
                
                // Інше
                putExtra("score", score)
                putExtra("league", tournament)
                putExtra("stage", stage)
                putExtra("date", dateFull)
                putExtra("stadium", stadium)
                putExtra("referee", referee)
                
                // ID для таймлайну
                putExtra("home_team_id", homeTeamId)
                putExtra("away_team_id", awayTeamId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = matches.size
}
