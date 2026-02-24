package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        tvLeagueTitle.text = "Бомбардири ($leagueType). Сезон: $selectedYear"

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

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
        val request = Request.Builder()
            .url("https://your-api.com/scorers?league=$leagueType&year=$selectedYear")
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

    private fun parseJson(json: String): List<Scorer> {
        val list = mutableListOf<Scorer>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(Scorer(
                    obj.getString("name"),
                    obj.getString("team"),
                    obj.getInt("matches"),
                    obj.getInt("goals")
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    private fun setupList(data: List<Scorer>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ScorersAdapter(data)
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}

// Модель даних
data class Scorer(val name: String, val team: String, val matches: Int, val goals: Int)

// Адаптер з виділенням перших трьох
class ScorersAdapter(private val items: List<Scorer>) : RecyclerView.Adapter<ScorersAdapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val rank: TextView = v.findViewById(R.id.tvRank)
        val name: TextView = v.findViewById(R.id.tvPlayerName)
        val team: TextView = v.findViewById(R.id.tvPlayerTeam)
        val matches: TextView = v.findViewById(R.id.tvMatches)
        val goals: TextView = v.findViewById(R.id.tvGoals)
        val container: View = v.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_scorer, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.rank.text = "${position + 1}."
        holder.name.text = item.name
        holder.team.text = item.team
        holder.matches.text = item.matches.toString()
        holder.goals.text = item.goals.toString()

        // ВИДІЛЕННЯ ПЕРШИХ ТРЬОХ (як у ТЗ)
        when (position) {
            0 -> holder.rank.setTextColor(Color.parseColor("#FFD700")) // Золото
            1 -> holder.rank.setTextColor(Color.parseColor("#C0C0C0")) // Срібло
            2 -> holder.rank.setTextColor(Color.parseColor("#CD7F32")) // Бронза
            else -> holder.rank.setTextColor(Color.WHITE)
        }
        
        if (position < 3) {
            holder.container.setBackgroundColor(Color.parseColor("#25252B")) // Легкий акцент
        } else {
            holder.container.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount() = items.size
}
