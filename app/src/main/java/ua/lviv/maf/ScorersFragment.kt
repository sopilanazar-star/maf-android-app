package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ScorersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvLeagueTitle: TextView
    
    private var leagueType: String = ""
    private var selectedYear: String = "2025"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scorers, container, false)
        
        // Отримуємо тип ліги з аргументів (I ліга, II ліга, U-19 тощо)
        leagueType = arguments?.getString("LEAGUE_TYPE") ?: ""

        // Ініціалізація View
        recyclerView = view.findViewById(R.id.rvScorers)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvLeagueTitle = view.findViewById(R.id.tvLeagueTitle)
        
        val btnBack = view.findViewById<View>(R.id.btnBack)
        tvLeagueTitle.text = "Бомбардири ($leagueType). Сезон: $selectedYear"

        btnBack?.setOnClickListener { parentFragmentManager.popBackStack() }

        // Якщо це U-19 — показуємо пусту сторінку згідно з ТЗ
        if (leagueType.contains("U-19")) {
            showEmptyState()
        } else {
            fetchScorersData()
        }

        return view
    }

    private fun fetchScorersData() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        val client = OkHttpClient()
        
        // Вставляємо реальний URL твого API на WordPress
        // Потім зможеш додати параметри турніру: "?competition_id=12"
        val apiUrl = "https://maf.lviv.ua/wp-json/maf/v1/top-scorers"
        
        val request = Request.Builder()
            .url(apiUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Log.e("Scorers", "Error: ${e.message}")
                    showEmptyState()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (jsonData != null) {
                        val scorers = parseJson(jsonData)
                        if (scorers.isEmpty()) showEmptyState() else setupList(scorers)
                    } else {
                        showEmptyState()
                    }
                }
            }
        })
    }

    // Змінено на JSONObject, щоб не створювати зайвих класів і легко читати відповідь WP API
    private fun parseJson(json: String): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getJSONObject(i))
            }
        } catch (e: Exception) { 
            e.printStackTrace() 
        }
        return list
    }

    private fun setupList(data: List<JSONObject>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ScorersAdapter(data) { playerId ->
            // Тут буде логіка відкриття картки гравця, яку ми обговорювали раніше
            Log.d("ScorersFragment", "Клік по гравцю: $playerId")
        }
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}

// Адаптер з виділенням перших трьох (адаптовано під item_top_scorer.xml)
class ScorersAdapter(
    private val items: List<JSONObject>,
    private val onPlayerClick: (String) -> Unit
) : RecyclerView.Adapter<ScorersAdapter.ViewHolder>() {
    
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val rank: TextView = v.findViewById(R.id.tvRank)
        val name: TextView = v.findViewById(R.id.tvPlayerName)
        val team: TextView = v.findViewById(R.id.tvPlayerTeam)
        val matches: TextView = v.findViewById(R.id.tvMatches)
        val goals: TextView = v.findViewById(R.id.tvGoals)
        val ivPlayerPhoto: ImageView = v.findViewById(R.id.ivPlayerPhoto)
        val container: View = v.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Зв'язуємо з нашим новим макетом
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_top_scorer, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Функція "пилосос", як у твоїх попередніх адаптерах
        fun getValue(vararg keys: String): String {
            for (key in keys) {
                if (item.has(key) && !item.isNull(key)) {
                    val v = item.optString(key)
                    if (v.isNotEmpty() && v != "false" && v != "null") return v
                }
            }
            return ""
        }

        val playerId = getValue("player_id", "id")
        val rankStr = getValue("rank")
        val name = getValue("name", "player_name")
        val matches = getValue("matches", "total_matches", "played")
        val goals = getValue("goals", "total_goals")
        val photoUrl = getValue("photo", "player_photo")
        
        // WP API повертає team як об'єкт, дістаємо звідти name
        var teamName = getValue("team_name")
        if (teamName.isEmpty() && item.has("team")) {
            val teamObj = item.optJSONObject("team")
            teamName = teamObj?.optString("name") ?: ""
        }

        holder.rank.text = if (rankStr.isNotEmpty()) rankStr else "${position + 1}."
        holder.name.text = name
        holder.team.text = teamName
        holder.matches.text = matches
        holder.goals.text = goals

        // ВИДІЛЕННЯ ПЕРШИХ ТРЬОХ (як у ТЗ)
        when (position) {
            0 -> holder.rank.setTextColor(Color.parseColor("#FFD700")) // Золото
            1 -> holder.rank.setTextColor(Color.parseColor("#C0C0C0")) // Срібло
            2 -> holder.rank.setTextColor(Color.parseColor("#CD7F32")) // Бронза
            else -> holder.rank.setTextColor(Color.parseColor("#00E676")) // Стандартний зелений
        }
        
        if (position < 3) {
            holder.container.setBackgroundColor(Color.parseColor("#2C313C")) // Легкий акцент для ТОП-3
        } else {
            holder.container.setBackgroundColor(Color.parseColor("#252932")) // Стандартний фон
        }

        // Завантаження фотографії
        if (photoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_player_placeholder)
                .into(holder.ivPlayerPhoto)
        } else {
            holder.ivPlayerPhoto.setImageResource(R.drawable.ic_player_placeholder)
        }

        // Обробка кліку
        holder.container.setOnClickListener { onPlayerClick(playerId) }
    }

    override fun getItemCount() = items.size
}
