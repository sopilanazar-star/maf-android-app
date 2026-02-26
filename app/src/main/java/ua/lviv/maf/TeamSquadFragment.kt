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
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
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
        
        // 🔥 Бронебійне отримання ID: читає як String, навіть якщо передали як Int
        teamId = arguments?.get("team_id")?.toString() ?: ""
        teamName = arguments?.getString("team_name") ?: "Команда"
        teamLogo = arguments?.getString("team_logo") ?: ""

        recyclerView.layoutManager = LinearLayoutManager(context)
        
        if (teamId.isNotEmpty()) {
            loadPlayers()
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Помилка: відсутній ID команди", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPlayers() {
        val year = AppConfig.selectedYear
        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$teamId&year=$year"
        
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
                    if (!response.isSuccessful || rawJson.isEmpty()) {
                        Toast.makeText(context, "Не вдалося завантажити склад", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    try {
                        val rawList = ArrayList<Player>()

                        // 🔥 Надійний вбудований парсер org.json (як у тебе в матчах)
                        if (rawJson.startsWith("[")) {
                            val jsonArray = JSONArray(rawJson)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                rawList.add(Player(
                                    id = obj.optString("id"),
                                    name = obj.optString("name"),
                                    number = obj.optString("number"),
                                    position = obj.optString("position"),
                                    photo = obj.optString("photo"),
                                    birthDate = obj.optString("birth_date"),
                                    age = obj.optInt("age")
                                ))
                            }
                        } else if (rawJson.startsWith("{")) {
                            // Резервний варіант, якщо сервер поверне об'єкт замість масиву
                            val jsonObj = JSONObject(rawJson)
                            val keys = jsonObj.keys()
                            while (keys.hasNext()) {
                                val obj = jsonObj.optJSONObject(keys.next())
                                if (obj != null) {
                                    rawList.add(Player(
                                        id = obj.optString("id"),
                                        name = obj.optString("name"),
                                        number = obj.optString("number"),
                                        position = obj.optString("position"),
                                        photo = obj.optString("photo"),
                                        birthDate = obj.optString("birth_date"),
                                        age = obj.optInt("age")
                                    ))
                                }
                            }
                        }

                        if (rawList.isNotEmpty()) {
                            val groupedItems = prepareGroupedList(rawList)
                            recyclerView.adapter = PlayersAdapter(groupedItems, teamName, teamLogo) { player ->
                                // Можна додати клік по гравцю
                            }
                        } else {
                            Toast.makeText(context, "Склад команди порожній", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) { 
                        Log.e("Squad", "Помилка парсингу: ${e.message}")
                        Toast.makeText(context, "Помилка структури даних", Toast.LENGTH_SHORT).show()
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
                else -> key.uppercase()
            }
            resultList.add(headerTitle)
            resultList.addAll(playersInGroup)
        }
        return resultList
    }
}
