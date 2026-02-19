package ua.lviv.maf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import okhttp3.*
import java.io.IOException
import ua.lviv.maf.models.Player

class TeamSquadFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var teamId: String = ""

    companion object {
        fun newInstance(teamId: String): TeamSquadFragment {
            val fragment = TeamSquadFragment()
            val args = Bundle()
            args.putString("team_id", teamId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_squad, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewSquad)
        progressBar = view.findViewById(R.id.progressBarSquad)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamId = arguments?.getString("team_id") ?: ""
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadPlayers()
    }

    private fun loadPlayers() {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$teamId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { progressBar.visibility = View.GONE }
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
                            val jsonArray = jsonElement.asJsonArray
                            for (element in jsonArray) rawList.add(parsePlayerSafe(element.asJsonObject))
                        } else if (jsonElement.isJsonObject) {
                            val jsonObject = jsonElement.asJsonObject
                            for (key in jsonObject.keySet()) {
                                try {
                                    if (jsonObject.get(key).isJsonObject) 
                                        rawList.add(parsePlayerSafe(jsonObject.get(key).asJsonObject))
                                } catch (e: Exception) {}
                            }
                        }

                        if (rawList.isNotEmpty()) {
                            val groupedItems = prepareGroupedList(rawList)
                            
                            // 🔥 ВИТЯГУЄМО ДАНІ КОМАНДИ З ГОЛОВНОГО ВІКНА
                            val teamName = activity?.intent?.getStringExtra("team_name") ?: "Команда"
                            val teamLogo = activity?.intent?.getStringExtra("team_logo") ?: ""

                            // 🔥 ПЕРЕДАЄМО ЇХ В АДАПТЕР
                            recyclerView.adapter = PlayersAdapter(groupedItems, teamName, teamLogo) { player ->
                                // Клік тепер повноцінно працює всередині самого PlayersAdapter
                            }
                        }

                    } catch (e: Exception) { Log.e("Squad", "Error: ${e.message}") }
                }
            }
        })
    }

    private fun prepareGroupedList(players: List<Player>): List<Any> {
        val resultList = ArrayList<Any>()
        val groupedMap = players.groupBy { it.position.trim().lowercase() }
        val sortedKeys = groupedMap.keys.sortedBy { pos ->
            when (pos) {
                "g", "gk", "goalkeeper", "воротар" -> 1
                "d", "def", "defender", "захисник" -> 2
                "m", "mid", "midfielder", "півзахисник" -> 3
                "f", "fwd", "forward", "нападник" -> 4
                else -> 99
            }
        }

        for (key in sortedKeys) {
            val playersInGroup = groupedMap[key] ?: continue
            val headerTitle = when (key) {
                "g", "gk", "goalkeeper", "воротар" -> "ВОРОТАРІ"
                "d", "def", "defender", "захисник" -> "ЗАХИСНИКИ"
                "m", "mid", "midfielder", "півзахисник" -> "ПІВЗАХИСНИКИ"
                "f", "fwd", "forward", "нападник" -> "НАПАДНИКИ"
                else -> key.uppercase()
            }
            resultList.add(headerTitle)
            resultList.addAll(playersInGroup)
        }
        return resultList
    }

    // 🔥 ОНОВЛЕНИЙ ПАРСЕР (Тепер бачить ДН і Вік)
    private fun parsePlayerSafe(obj: com.google.gson.JsonObject): Player {
        fun getString(key: String): String {
            if (!obj.has(key) || obj.get(key).isJsonNull) return ""
            val p = obj.get(key)
            if (p.isJsonPrimitive) {
                if (p.asJsonPrimitive.isBoolean) return ""
                return p.asString
            }
            return ""
        }
        
        fun getInt(key: String): Int {
            if (!obj.has(key) || obj.get(key).isJsonNull) return 0
            val p = obj.get(key)
            if (p.isJsonPrimitive && p.asJsonPrimitive.isNumber) {
                return p.asInt
            }
            return 0
        }

        return Player(
            id = getString("id"), 
            name = getString("name"), 
            number = getString("number"), 
            position = getString("position"), 
            photo = getString("photo"),
            birthDate = getString("birth_date"), // Читаємо ДН
            age = getInt("age")                  // Читаємо Вік
        )
    }
}
