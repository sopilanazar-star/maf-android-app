package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import okhttp3.*
import java.io.IOException
import ua.lviv.maf.models.Player

class TeamSquadFragment : Fragment() {

    private var teamId: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    companion object {
        fun newInstance(teamId: String): TeamSquadFragment {
            val args = Bundle().apply { putString("team_id", teamId) }
            return TeamSquadFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // UI кодом
        val root = android.widget.FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        progressBar = ProgressBar(requireContext()).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            layoutParams = android.widget.FrameLayout.LayoutParams(-2, -2).apply {
                gravity = android.view.Gravity.CENTER
            }
        }

        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }

        root.addView(recyclerView)
        root.addView(progressBar)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamId = arguments?.getString("team_id")

        if (teamId != null && teamId != "0") {
            loadPlayers()
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "ID команди втрачено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPlayers() {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$teamId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Помилка мережі", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val rawJson = response.body?.string()?.trim() ?: ""

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (!response.isSuccessful || rawJson.isEmpty()) return@runOnUiThread

                    try {
                        val jsonElement = JsonParser.parseString(rawJson)
                        val rawList = ArrayList<Player>()

                        if (jsonElement.isJsonArray) {
                            for (el in jsonElement.asJsonArray) rawList.add(parsePlayerSafe(el.asJsonObject))
                        } else if (jsonElement.isJsonObject) {
                            val jsonObj = jsonElement.asJsonObject
                            for (key in jsonObj.keySet()) {
                                try {
                                    if (jsonObj.get(key).isJsonObject) 
                                        rawList.add(parsePlayerSafe(jsonObj.get(key).asJsonObject))
                                } catch (e: Exception) {}
                            }
                        }

                        if (rawList.isNotEmpty()) {
                            // 🔥 СОРТУВАННЯ І ГРУПУВАННЯ
                            val groupedList = prepareGroupedList(rawList)
                            
                            recyclerView.adapter = PlayersAdapter(groupedList) { player ->
                                // Обробка кліку на гравця
                                Toast.makeText(context, "Гравець: ${player.name}", Toast.LENGTH_SHORT).show()
                                // Тут потім відкриємо PlayerStatsActivity
                            }
                        } else {
                            Toast.makeText(context, "Склад порожній", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e("Squad", "Err: ${e.message}")
                    }
                }
            }
        })
    }

    private fun prepareGroupedList(players: List<Player>): List<Any> {
        val positionOrder = mapOf("Воротар" to 1, "Захисник" to 2, "Півзахисник" to 3, "Нападник" to 4)
        val groupedMap = players.groupBy { it.position.trim().replaceFirstChar { c -> c.uppercase() } }
        val sortedKeys = groupedMap.keys.sortedBy { positionOrder[it] ?: 99 }

        val result = ArrayList<Any>()
        for (pos in sortedKeys) {
            if (pos.isNotEmpty()) {
                result.add(pos) // Заголовок (String)
                result.addAll(groupedMap[pos] ?: emptyList()) // Гравці
            }
        }
        return result
    }

    private fun parsePlayerSafe(obj: com.google.gson.JsonObject): Player {
        fun getString(key: String): String {
            if (!obj.has(key) || obj.get(key).isJsonNull) return ""
            val p = obj.get(key)
            if (p.isJsonPrimitive && !p.asJsonPrimitive.isBoolean) return p.asString
            return ""
        }
        return Player(
            id = getString("id"), name = getString("name"),
            number = getString("number"), position = getString("position"),
            photo = getString("photo")
        )
    }
}
