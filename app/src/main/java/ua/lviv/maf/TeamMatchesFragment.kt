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
        // Створюємо макет кодом (або використовуй XML, якщо створив)
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
        // 🔥 ВИПРАВЛЕННЯ: Використовуємо загальний endpoint 'matches' з параметром team_id
        val url = "https://maf.lviv.ua/wp-json/maf/v2/matches?team_id=$teamId" 
        
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
                        // Якщо 404, то, можливо, сервер повертає пустий масив [] або помилку
                        tvEmpty.text = "Матчів немає"
                        tvEmpty.visibility = View.VISIBLE
                        return@runOnUiThread
                    }

                    try {
                        val matchesList = ArrayList<JSONObject>()
                        
                        // Обробка масиву [...]
                        if (jsonStr.trim().startsWith("[")) {
                            val jsonArray = JSONArray(jsonStr)
                            for (i in 0 until jsonArray.length()) {
                                matchesList.add(jsonArray.getJSONObject(i))
                            }
                        } 
                        // Обробка об'єкта {...} (якщо PHP віддав асоціативний масив)
                        else if (jsonStr.trim().startsWith("{")) {
                             val jsonObject = JSONObject(jsonStr)
                             // Іноді сервер може віддати {"status": 404}, перевіримо це
                             if (jsonObject.has("code") && jsonObject.getString("code") == "rest_no_route") {
                                 tvEmpty.text = "Помилка API (Маршрут не існує)"
                                 tvEmpty.visibility = View.VISIBLE
                                 return@runOnUiThread
                             }
                             
                             val keys = jsonObject.keys()
                             while(keys.hasNext()) {
                                 val key = keys.next()
                                 val item = jsonObject.optJSONObject(key)
                                 if (item != null) matchesList.add(item)
                             }
                        }

                        if (matchesList.isNotEmpty()) {
                            tvEmpty.visibility = View.GONE
                            // Передаємо адаптер
                            recyclerView.adapter = TeamMatchesAdapter(matchesList) { matchId ->
                                val intent = Intent(context, MatchDetailActivity::class.java)
                                intent.putExtra("id", matchId)
                                intent.putExtra("home_team_id", "0") // Тимчасово, бо в списку матчів може не бути ID
                                startActivity(intent)
                            }
                        } else {
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
}
