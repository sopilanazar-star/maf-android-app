package ua.lviv.maf

import android.content.Intent
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
import com.google.gson.Gson
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
        // Можна використовувати той самий макет, що був для списку, або створити простий з RecyclerView
        val view = inflater.inflate(R.layout.fragment_team_squad, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewSquad)
        progressBar = view.findViewById(R.id.progressBarSquad)
        return view
    }
    // Примітка: Створи fragment_team_squad.xml з RecyclerView та ProgressBar, якщо немає.

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

                        // ... (Логіка парсингу JSON з попередніх відповідей) ...
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
                            // 🔥 ТУТ ВІДБУВАЄТЬСЯ МАГІЯ СОРТУВАННЯ
                            val groupedItems = prepareGroupedList(rawList)
                            recyclerView.adapter = PlayersAdapter(groupedItems) { player ->
                                // Клік по гравцю
                                Toast.makeText(context, "${player.name}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: Exception) { Log.e("Squad", "Error: ${e.message}") }
                }
            }
        })
    }

    // 🔥 ФУНКЦІЯ СОРТУВАННЯ ТА ПЕРЕЙМЕНУВАННЯ
    private fun prepareGroupedList(players: List<Player>): List<Any> {
        val resultList = ArrayList<Any>()

        // 1. Групуємо гравців за їх "сирою" позицією (G, D, M, F)
        val groupedMap = players.groupBy { it.position.trim().lowercase() }

        // 2. Визначаємо правильний порядок: 1-Воротар, 2-Захисник, 3-Півзахисник, 4-Нападник
        // Ми перевіряємо всі можливі варіанти написання (g, gk, goalkeeper, воротар...)
        val sortedKeys = groupedMap.keys.sortedBy { pos ->
            when (pos) {
                "g", "gk", "goalkeeper", "воротар" -> 1
                "d", "def", "defender", "захисник" -> 2
                "m", "mid", "midfielder", "півзахисник" -> 3
                "f", "fwd", "forward", "нападник" -> 4
                else -> 99 // Якщо позиція невідома - в кінець
            }
        }

        // 3. Формуємо фінальний список із правильними назвами заголовків
        for (key in sortedKeys) {
            val playersInGroup = groupedMap[key] ?: continue
            
            // Перейменовуємо "F" -> "НАПАДНИКИ"
            val headerTitle = when (key) {
                "g", "gk", "goalkeeper", "воротар" -> "ВОРОТАРІ"
                "d", "def", "defender", "захисник" -> "ЗАХИСНИКИ"
                "m", "mid", "midfielder", "півзахисник" -> "ПІВЗАХИСНИКИ"
                "f", "fwd", "forward", "нападник" -> "НАПАДНИКИ"
                else -> key.uppercase() // Якщо щось інше, показуємо як є
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
        return Player(getString("id"), getString("name"), getString("number"), getString("position"), getString("photo"))
    }
}
