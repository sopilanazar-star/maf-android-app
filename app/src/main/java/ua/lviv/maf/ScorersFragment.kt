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
        
        // 1. Отримуємо тип ліги та рік з аргументів
        leagueType = arguments?.getString("LEAGUE_TYPE") ?: ""
        selectedYear = arguments?.getString("SELECTED_YEAR") ?: "2025"

        // Ініціалізація View
        recyclerView = view.findViewById(R.id.rvScorers)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvLeagueTitle = view.findViewById(R.id.tvLeagueTitle)
        
        val btnBack = view.findViewById<View>(R.id.btnBack)
        tvLeagueTitle.text = "Бомбардири ($leagueType). Сезон: $selectedYear"

        btnBack?.setOnClickListener { parentFragmentManager.popBackStack() }

        if (leagueType.isEmpty()) {
            showEmptyState()
        } else {
            fetchScorersData()
        }

        return view
    }

    private fun fetchScorersData() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        // 2. Мапа турнірів (Назва ліги -> ID турніру для API)
        // ВАЖЛИВО: Обов'язково підстав свої реальні ID для інших ліг замість 1404 та 1405!
        val tournamentId = when (leagueType) {
            "Вища ліга" -> "1404" 
            "Перша ліга" -> "1405" 
            "U-19" -> "1406"
            else -> "1406" 
        }

        val client = OkHttpClient()
        
        // 3. Використовуємо твоє робоче v2 API зі змінною року
        val apiUrl = "https://maf.lviv.ua/wp-json/maf/v2/top-scorers?tournament_id=$tournamentId&year=$selectedYear"
        
        Log.d("Scorers", "Requesting URL: $apiUrl")

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
            // Тут ми потім додамо перехід на картку гравця
            Log.d("ScorersFragment", "Клік по гравцю з ID: $playerId")
        }
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}

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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_top_scorer, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // ДІСТАЄМО ДАНІ З НОВОГО JSON v2 (вкладені об'єкти)
        val playerObj = item.optJSONObject("player")
        val teamObj = item.optJSONObject("team")

        val playerId = playerObj?.optString("id") ?: ""
        val name = playerObj?.optString("name") ?: "Невідомий гравець"
        val photoUrl = playerObj?.optString("photo") ?: ""

        val teamName = teamObj?.optString("name") ?: "Без команди"
        
        val rankStr = item.optString("rank")
        val goals = item.optString("goals", "0")
        val matches = item.optString("matches", "0")

        // ЗАПОВНЮЄМО UI
        holder.rank.text = if (rankStr.isNotEmpty()) rankStr else "${position + 1}."
        holder.name.text = name
        holder.team.text = teamName
        holder.matches.text = matches
        holder.goals.text = goals

        // ПІДСВІТКА ТОП-3 КОЛЬОРАМИ МЕДАЛЕЙ
        when (position) {
            0 -> holder.rank.setTextColor(Color.parseColor("#FFD700")) // Золото
            1 -> holder.rank.setTextColor(Color.parseColor("#C0C0C0")) // Срібло
            2 -> holder.rank.setTextColor(Color.parseColor("#CD7F32")) // Бронза
            else -> holder.rank.setTextColor(Color.parseColor("#00E676")) // Зелений
        }
        
        if (position < 3) {
            holder.container.setBackgroundColor(Color.parseColor("#2C313C"))
        } else {
            holder.container.setBackgroundColor(Color.parseColor("#252932"))
        }

        // ЗАВАНТАЖУЄМО ФОТО ГРАВЦЯ
        if (photoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(photoUrl)
                .circleCrop() // Робимо фото круглим
                .placeholder(R.drawable.ic_player_placeholder)
                .into(holder.ivPlayerPhoto)
        } else {
            holder.ivPlayerPhoto.setImageResource(R.drawable.ic_player_placeholder)
        }

        // Обробка кліку по рядку
        holder.container.setOnClickListener { onPlayerClick(playerId) }
    }

    override fun getItemCount() = items.size
}
