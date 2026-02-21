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
        // 🔥 ПРАВКА: Додаємо параметр року, який вибрав користувач
        val year = AppConfig.selectedYear
        val url = "https://maf.lviv.ua/wp-json/maf/v2/matches?year=$year" 
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
                            for (i in 0 until jsonArray.length()) allMatches.add(jsonArray.getJSONObject(i))
                        } else {
                             val jsonObject = JSONObject(jsonStr)
                             val keys = jsonObject.keys()
                             while(keys.hasNext()) {
                                 val item = jsonObject.optJSONObject(keys.next())
                                 if (item != null) allMatches.add(item)
                             }
                        }

                        // Фільтруємо
                        val teamMatches = allMatches.filter { match ->
                            val homeId = match.optString("home_team_id")
                            val awayId = match.optString("away_team_id")
                            val team1Id = match.optString("team1_id")
                            val team2Id = match.optString("team2_id")
                            homeId == teamId || awayId == teamId || team1Id == teamId || team2Id == teamId
                        }

                        if (teamMatches.isNotEmpty()) {
                            recyclerView.adapter = TeamMatchesAdapter(teamMatches) { matchJson ->
                                openMatchDetails(matchJson)
                            }
                        } else {
                            tvEmpty.text = "Матчів немає"
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

    // 🔥 ФУНКЦІЯ ПЕРЕХОДУ, ЯКА ЗНАХОДИТЬ ВСЕ
    private fun openMatchDetails(match: JSONObject) {
        val intent = Intent(context, MatchDetailActivity::class.java)

        // Копія функції пошуку з адаптера, щоб Intent був повним
        fun find(vararg keys: String): String {
            for (key in keys) {
                if (match.has(key) && !match.isNull(key)) {
                    val v = match.optString(key)
                    if (v.isNotEmpty() && v != "false" && v != "null") return v
                }
            }
            return ""
        }

        intent.putExtra("id", find("id"))
        
        // 🚀 НАЙВАЖЛИВІШЕ: Передаємо правильні ключі для MatchDetailActivity
        // Назви команд
        intent.putExtra("team1", find("home_team_name", "home_team", "team1_name", "team1"))
        intent.putExtra("team2", find("away_team_name", "away_team", "team2_name", "team2"))
        
        // Логотипи (саме тут була проблема)
        intent.putExtra("logo1", find("home_team_logo", "home_logo", "team1_logo", "team1_image"))
        intent.putExtra("logo2", find("away_team_logo", "away_logo", "team2_logo", "team2_image"))
        
        // Інфо
        intent.putExtra("score", find("score", "match_score"))
        intent.putExtra("date", "${find("date")} ${find("time")}")
        intent.putExtra("league", find("league_name", "competition_name"))
        intent.putExtra("stage", find("stage_name", "round"))
        intent.putExtra("stadium", find("stadium_name", "stadium"))
        intent.putExtra("referee", find("referee_name", "referee"))
        
        // ID для таймлайну
        intent.putExtra("home_team_id", find("home_team_id", "team1_id"))
        intent.putExtra("away_team_id", find("away_team_id", "team2_id"))

        startActivity(intent)
    }
}
