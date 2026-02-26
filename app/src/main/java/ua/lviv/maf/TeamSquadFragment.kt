package ua.lviv.maf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_team_squad, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewSquad)
        progressBar = view.findViewById(R.id.progressBarSquad)
        recyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamId = arguments?.getString("team_id") ?: ""
        teamName = arguments?.getString("team_name") ?: "Команда"
        teamLogo = arguments?.getString("team_logo") ?: ""

        if (teamId.isEmpty()) {
            progressBar.visibility = View.GONE
            Log.e("TEAM", "teamId is empty")
            return
        }

        loadPlayers()
    }

    private fun loadPlayers() {

        // 🔥 Якщо рік не вибраний — ставимо 2025
        var year = AppConfig.selectedYear
        if (year <= 0) year = 2025

        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$teamId&year=$year"

        Log.e("API_URL", url)

        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    Log.e("API_ERROR", e.message ?: "Unknown error")
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val rawJson = response.body?.string()?.trim() ?: ""

                activity?.runOnUiThread {

                    progressBar.visibility = View.GONE

                    if (!response.isSuccessful || rawJson.isEmpty()) {
                        Log.e("API_ERROR", "Response code: ${response.code}")
                        return@runOnUiThread
                    }

                    try {
                        val jsonElement = JsonParser.parseString(rawJson)
                        val players = ArrayList<Player>()

                        if (jsonElement.isJsonArray) {
                            val array = jsonElement.asJsonArray
                            for (element in array) {
                                players.add(parsePlayerSafe(element.asJsonObject))
                            }
                        }

                        if (players.isNotEmpty()) {
                            val grouped = prepareGroupedList(players)
                            recyclerView.adapter =
                                PlayersAdapter(grouped, teamName, teamLogo) { }
                        } else {
                            Log.e("API", "Players list empty")
                        }

                    } catch (e: Exception) {
                        Log.e("JSON_ERROR", e.message ?: "Parse error")
                    }
                }
            }
        })
    }

    private fun prepareGroupedList(players: List<Player>): List<Any> {
        val result = ArrayList<Any>()
        val grouped = players.groupBy { it.position.trim().lowercase() }

        val order = listOf("g", "d", "m", "f")

        for (key in order) {
            val group = grouped[key] ?: continue

            val title = when (key) {
                "g" -> "ВОРОТАРІ"
                "d" -> "ЗАХИСНИКИ"
                "m" -> "ПІВЗАХИСНИКИ"
                "f" -> "НАПАДНИКИ"
                else -> key.uppercase()
            }

            result.add(title)
            result.addAll(group)
        }

        return result
    }

    private fun parsePlayerSafe(obj: com.google.gson.JsonObject): Player {

        fun getString(key: String): String {
            if (!obj.has(key) || obj.get(key).isJsonNull) return ""
            return obj.get(key).asString
        }

        fun getInt(key: String): Int {
            if (!obj.has(key) || obj.get(key).isJsonNull) return 0
            return try { obj.get(key).asInt } catch (e: Exception) { 0 }
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
