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
    private var teamName: String = ""
    private var teamLogo: String = ""

    companion object {
        fun newInstance(teamId: String, teamName: String, teamLogo: String): TeamSquadFragment {
            val fragment = TeamSquadFragment()
            val args = Bundle()
            args.putString("team_id", teamId)
            args.putString("team_name", teamName)
            args.putString("team_logo", teamLogo)
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
        teamName = arguments?.getString("team_name") ?: "Команда"
        teamLogo = arguments?.getString("team_logo") ?: ""
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadPlayers()
    }

    private fun loadPlayers() {
        val year = AppConfig.selectedYear
        val url = "https://maf.lviv.ua/wp-json/maf/team-players?id=$teamId&year=$year"

        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { 
                    progressBar.visibility = View.GONE 
                    Toast.makeText(context, "Помилка мережі: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val rawJson = response.body?.string()?.trim() ?: ""

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (!response.isSuccessful) {
                        Toast.makeText(context, "Помилка сервера: ${response.code}", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    
                    if (rawJson.isEmpty() || rawJson == "[]") {
                        Toast.makeText(context, "Сервер повернув порожній список для року: $year", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }

                    try {
                        val jsonElement = JsonParser.parseString(rawJson)
                        val rawList = ArrayList<Player>()

                        if (jsonElement.isJsonArray) {
                            val jsonArray = jsonElement.asJsonArray
                            for (element in jsonArray) {
                                if (element.isJsonObject) rawList.add(parsePlayerSafe(element.asJsonObject))
                            }
                        } else if (jsonElement.isJsonObject) {
                            val jsonObject = jsonElement.asJsonObject
                            var foundArray = false
                            for (key in jsonObject.keySet()) {
                                if (jsonObject.get(key).isJsonArray) {
                                    val innerArray = jsonObject.get(key).asJsonArray
                                    for (element in innerArray) {
                                        if (element.isJsonObject) rawList.add(parsePlayerSafe(element.asJsonObject))
                                    }
                                    foundArray = true
                                    break
                                }
                            }
                            if (!foundArray) {
                                for (key in jsonObject.keySet()) {
                                    try {
                                        if (jsonObject.get(key).isJsonObject) 
                                            rawList.add(parsePlayerSafe(jsonObject.get(key).asJsonObject))
                                    } catch (e: Exception) {}
                                }
                            }
                        }

                        if (rawList.isNotEmpty()) {
                            val groupedItems = prepareGroupedList(rawList)
                            recyclerView.adapter = PlayersAdapter(groupedItems, teamName, teamLogo) { player -> }
                        } else {
                            Toast.makeText(context, "Дані отримано, але парсер не знайшов гравців", Toast.LENGTH_LONG).show()
                        }

                    } catch (e: Exception) { 
                        Toast.makeText(context, "Помилка читання JSON: ${e.message}", Toast.LENGTH_LONG).show()
                    }
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
                else -> if (key.isNotBlank()) key.uppercase() else "ІНШІ"
            }
            resultList.add(headerTitle)
            resultList.addAll(playersInGroup)
        }
        return resultList
    }

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
            if (p.isJsonPrimitive && p.asJsonPrimitive.isNumber) return p.asInt
            return 0
        }

        return Player(
            id = getString("id"), 
            name = getString("name"), 
            number = getString("number"), 
            position = getString("position"), 
            photo = getString("photo"),
            birthDate = getString("birth_date"),
            age = getInt("age")
        )
    }
}
