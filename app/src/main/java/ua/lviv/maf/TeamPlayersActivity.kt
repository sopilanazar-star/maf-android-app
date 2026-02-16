package ua.lviv.maf

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import okhttp3.*
import java.io.IOException
import ua.lviv.maf.models.Player

class TeamPlayersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var teamId: String = ""
    private var teamName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idInt = intent.getIntExtra("team_id", 0)
        teamId = if (idInt != 0) idInt.toString() else intent.getStringExtra("team_id") ?: ""
        teamName = intent.getStringExtra("team_name") ?: "Команда"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        val title = TextView(this).apply {
            text = teamName
            textSize = 22f
            setTextColor(Color.WHITE)
            setPadding(0, 40, 0, 20)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        progressBar = ProgressBar(this).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@TeamPlayersActivity)
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        root.addView(title)
        root.addView(progressBar)
        root.addView(recyclerView)

        setContentView(root)

        if (teamId.isNotEmpty() && teamId != "0") {
            loadPlayers()
        } else {
            showError("Помилка", "Немає ID команди")
        }
    }

    private fun loadPlayers() {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$teamId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this@TeamPlayersActivity, "Помилка мережі", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val rawJson = response.body?.string()?.trim() ?: ""

                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
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
                                    val item = jsonObject.get(key)
                                    if (item.isJsonObject) rawList.add(parsePlayerSafe(item.asJsonObject))
                                } catch (e: Exception) {}
                            }
                        }

                        if (rawList.isNotEmpty()) {
                            val groupedItems = prepareGroupedList(rawList)
                            recyclerView.adapter = PlayersAdapter(groupedItems) { player ->
                                // Клік на гравця
                                openPlayerStats(player)
                            }
                        } else {
                            Toast.makeText(this@TeamPlayersActivity, "Список пустий", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        showError("Помилка обробки", rawJson)
                    }
                }
            }
        })
    }

    private fun prepareGroupedList(players: List<Player>): List<Any> {
        // Пріоритети для сортування
        val order = mapOf("воротар" to 1, "захисник" to 2, "півзахисник" to 3, "нападник" to 4)

        val grouped = players.groupBy { it.position.lowercase().trim() }
        val sortedPositions = grouped.keys.sortedBy { order[it] ?: 99 }

        val result = ArrayList<Any>()
        for (pos in sortedPositions) {
            val title = when(pos) {
                "воротар" -> "Воротарі"
                "захисник" -> "Захисники"
                "півзахисник" -> "Півзахисники"
                "нападник" -> "Нападники"
                else -> pos.replaceFirstChar { it.uppercase() }
            }
            result.add(title.uppercase())
            result.addAll(grouped[pos] ?: emptyList())
        }
        return result
    }

    private fun parsePlayerSafe(obj: com.google.gson.JsonObject): Player {
        fun getS(k: String) = if (obj.has(k) && !obj.get(k).isJsonNull) {
            val p = obj.get(k)
            if (p.isJsonPrimitive && !p.asJsonPrimitive.isBoolean) p.asString else ""
        } else ""

        return Player(id=getS("id"), name=getS("name"), number=getS("number"), position=getS("position"), photo=getS("photo"))
    }

    private fun openPlayerStats(player: Player) {
        // Поки що просто повідомлення, але Intent вже готовий
        Toast.makeText(this, "Гравець: ${player.name}", Toast.LENGTH_SHORT).show()
        /* val intent = Intent(this, PlayerStatsActivity::class.java)
        intent.putExtra("player_id", player.id)
        startActivity(intent)
        */
    }

    private fun showError(t: String, m: String) {
        progressBar.visibility = ProgressBar.GONE
        AlertDialog.Builder(this).setTitle(t).setMessage(m.take(500)).setPositiveButton("OK", null).show()
    }
}
