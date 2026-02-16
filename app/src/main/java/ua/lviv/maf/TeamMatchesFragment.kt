package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class TeamMatchesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private var teamId: String = ""

    companion object {
        fun newInstance(teamId: String): TeamMatchesFragment {
            val fragment = TeamMatchesFragment()
            val args = Bundle()
            args.putString("team_id", teamId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = android.widget.RelativeLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(android.graphics.Color.parseColor("#1A1D23"))
        }

        progressBar = ProgressBar(context).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            layoutParams = android.widget.RelativeLayout.LayoutParams(-2, -2).apply {
                addRule(android.widget.RelativeLayout.CENTER_IN_PARENT)
            }
        }

        tvEmpty = TextView(context).apply {
            text = "Матчів не знайдено"
            setTextColor(android.graphics.Color.GRAY)
            visibility = View.GONE
            layoutParams = android.widget.RelativeLayout.LayoutParams(-2, -2).apply {
                addRule(android.widget.RelativeLayout.CENTER_IN_PARENT)
            }
        }

        recyclerView = RecyclerView(context!!).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }

        root.addView(recyclerView)
        root.addView(progressBar)
        root.addView(tvEmpty)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamId = arguments?.getString("team_id") ?: ""
        loadMatches()
    }

    private fun loadMatches() {
        // Якщо треба, зміни на: "https://maf.lviv.ua/wp-json/maf/v2/team-matches?id=$teamId"
        val url = "https://maf.lviv.ua/wp-json/maf/v2/matches" 
        
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { 
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Помилка мережі", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonStr = response.body?.string()

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (!response.isSuccessful || jsonStr.isNullOrEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        return@runOnUiThread
                    }

                    try {
                        val allMatches = ArrayList<JSONObject>()
                        
                        if (jsonStr.trim().startsWith("[")) {
                            val jsonArray = JSONArray(jsonStr)
                            for (i in 0 until jsonArray.length()) {
                                allMatches.add(jsonArray.getJSONObject(i))
                            }
                        } else {
                             val jsonObject = JSONObject(jsonStr)
                             val keys = jsonObject.keys()
                             while(keys.hasNext()) {
                                 val key = keys.next()
                                 val item = jsonObject.optJSONObject(key)
                                 if (item != null) allMatches.add(item)
                             }
                        }

                        // Фільтрація для нашої команди
                        val teamMatches = allMatches.filter { match ->
                            val homeId = match.optString("home_team_id")
                            val awayId = match.optString("away_team_id")
                            val team1Id = match.optString("team1_id")
                            val team2Id = match.optString("team2_id")
                            
                            homeId == teamId || awayId == teamId || team1Id == teamId || team2Id == teamId
                        }

                        if (teamMatches.isNotEmpty()) {
                            // Передаємо адаптер
                            recyclerView.adapter = TeamMatchesAdapter(teamMatches) { matchJson ->
                                openMatchDetails(matchJson)
                            }
                        } else {
                            tvEmpty.text = "Матчів для цієї команди немає"
                            tvEmpty.visibility = View.VISIBLE
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        tvEmpty.text = "Помилка даних"
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    // 🔥 ВИПРАВЛЕНИЙ МЕТОД ПЕРЕХОДУ
    private fun openMatchDetails(match: JSONObject) {
        val intent = Intent(context, MatchDetailActivity::class.java)
        
        // Тут ми мапимо ключі з JSON на ключі, які чекає MatchDetailActivity
        // Дивимося в твій код MatchDetailActivity:
        // intent.getStringExtra("team1"), "logo1", "score" і т.д.
        
        intent.putExtra("id", match.optString("id"))
        
        // Назви команд
        intent.putExtra("team1", match.optString("team1_name"))
        intent.putExtra("team2", match.optString("team2_name"))
        
        // Логотипи
        intent.putExtra("logo1", match.optString("team1_logo"))
        intent.putExtra("logo2", match.optString("team2_logo"))
        
        // Рахунок і статус
        intent.putExtra("score", match.optString("score"))
        
        // Дата і час
        val date = match.optString("date")
        val time = match.optString("time")
        intent.putExtra("date", "$date $time")
        
        // Додаткова інфа
        intent.putExtra("league", match.optString("league_name"))
        intent.putExtra("stage", match.optString("stage_name"))
        intent.putExtra("stadium", match.optString("stadium_name")) // Або "stadium"
        intent.putExtra("referee", match.optString("referee_name")) // Або "referee"
        
        // ID команд для Таймлайну (ДУЖЕ ВАЖЛИВО)
        intent.putExtra("home_team_id", match.optString("home_team_id")) // Або team1_id
        intent.putExtra("away_team_id", match.optString("away_team_id")) // Або team2_id
        
        startActivity(intent)
    }
}
