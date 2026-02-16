package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

        // 1. ЛОВИМО ID (І як число, і як текст)
        val idInt = intent.getIntExtra("team_id", 0)
        if (idInt != 0) {
            teamId = idInt.toString()
        } else {
            teamId = intent.getStringExtra("team_id") ?: ""
        }

        teamName = intent.getStringExtra("team_name") ?: "Команда"

        // 2. БУДУЄМО ІНТЕРФЕЙС
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
            progressBar.visibility = ProgressBar.GONE
            Toast.makeText(this, "Помилка: Немає ID команди", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadPlayers() {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$teamId"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this@TeamPlayersActivity, "Помилка мережі", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Читаємо відповідь як простий текст
                val rawJson = response.body?.string()?.trim()

                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE

                    // 1. Якщо прийшло пусто або помилка сервера
                    if (!response.isSuccessful || rawJson.isNullOrEmpty()) {
                        Toast.makeText(this@TeamPlayersActivity, "Сервер не відповідає", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    // 2. 🔥 ГОЛОВНА ПЕРЕВІРКА:
                    // Якщо відповідь це 'false', 'null' або щось інше, що НЕ починається на '['
                    if (rawJson == "false" || rawJson == "null" || !rawJson.startsWith("[")) {
                        Toast.makeText(this@TeamPlayersActivity, "Гравців у базі ще немає", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }

                    // 3. Якщо ми тут — значить це точно список [...]
                    try {
                        val type = object : TypeToken<List<Player>>() {}.type
                        val players: List<Player> = Gson().fromJson(rawJson, type)

                        if (players.isEmpty()) {
                            Toast.makeText(this@TeamPlayersActivity, "Список пустий", Toast.LENGTH_SHORT).show()
                        } else {
                            recyclerView.adapter = PlayersAdapter(players)
                        }

                    } catch (e: Exception) {
                        // Якщо впало тут — значить проблема всередині моделі Player (наприклад, null там де не треба)
                        Log.e("TeamPlayers", "JSON Parse error: ${e.message}")
                        Log.e("TeamPlayers", "RAW DATA: $rawJson") // Дивись в Logcat, що прийшло!
                        Toast.makeText(this@TeamPlayersActivity, "Збій структури даних", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
