package ua.lviv.maf

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

class PlayerMatchesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyInfo: TextView
    private var playerId: String = ""

    companion object {
        fun newInstance(playerId: String): PlayerMatchesFragment {
            val fragment = PlayerMatchesFragment()
            val args = Bundle()
            args.putString("player_id", playerId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Використовуємо той самий макет, що й для матчів команди
        val view = inflater.inflate(R.layout.fragment_team_matches, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewMatches)
        progressBar = view.findViewById(R.id.progressBarMatches)
        
        // Знаходимо текст для пустого списку (якщо є в макеті)
        tvEmptyInfo = view.findViewById(R.id.tvEmptyInfo) ?: TextView(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerId = arguments?.getString("player_id") ?: ""
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadPlayerMatches()
    }

    private fun loadPlayerMatches() {
        // 🔥 ПЕРЕВІР ТА ЗМІНИ ЦЕ ПОСИЛАННЯ НА СВОЄ API МАТЧІВ ГРАВЦЯ, ЯКЩО ВОНО ІНШЕ
        val url = "https://maf.lviv.ua/wp-json/maf/v2/player-matches?id=$playerId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { progressBar.visibility = View.GONE }
            }

            override fun onResponse(call: Call, response: Response) {
                val rawJson = response.body?.string()?.trim() ?: ""

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (!response.isSuccessful || rawJson.isEmpty() || rawJson == "[]") {
                        tvEmptyInfo.visibility = View.VISIBLE
                        tvEmptyInfo.text = "Матчів не знайдено"
                        return@runOnUiThread
                    }

                    try {
                        val jsonArray = JSONArray(rawJson)
                        val matchesList = ArrayList<JSONObject>()
                        for (i in 0 until jsonArray.length()) {
                            matchesList.add(jsonArray.getJSONObject(i))
                        }

                        if (matchesList.isNotEmpty()) {
                            tvEmptyInfo.visibility = View.GONE
                            // 🔥 ВИКОРИСТОВУЄМО ТВІЙ ГОТОВИЙ АДАПТЕР
                            recyclerView.adapter = TeamMatchesAdapter(matchesList) { match ->
                                // Клік по матчу вже прописаний всередині адаптера
                            }
                        } else {
                            tvEmptyInfo.visibility = View.VISIBLE
                            tvEmptyInfo.text = "Матчів не знайдено"
                        }
                    } catch (e: Exception) {
                        Log.e("PlayerMatches", "Error parsing: ${e.message}")
                    }
                }
            }
        })
    }
}
