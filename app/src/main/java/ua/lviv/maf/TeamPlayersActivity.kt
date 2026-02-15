package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
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

        // 🔥 ПРАВКА ТУТ:
        // Спочатку пробуємо отримати як число (Int), бо саме Int ми передавали з MatchDetailActivity
        val idInt = intent.getIntExtra("team_id", 0)

        if (idInt != 0) {
            teamId = idInt.toString()
        } else {
            // Якщо раптом передали як стрічку (String)
            teamId = intent.getStringExtra("team_id") ?: ""
        }

        teamName = intent.getStringExtra("team_name") ?: "Команда"

        // Створення UI кодом (без XML)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
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
            indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.WHITE) // Білий колір
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@TeamPlayersActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        root.addView(title)
        root.addView(progressBar)
        root.addView(recyclerView)

        setContentView(root)

        // Перевірка
        if (teamId.isNotEmpty() && teamId != "0") {
            loadPlayers()
        } else {
            progressBar.visibility = ProgressBar.GONE
            Toast.makeText(this, "Помилка: team_id втрачено ($teamId)", Toast.LENGTH_LONG).show()
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

                val json = response.body?.string()

                if (!response.isSuccessful || json.isNullOrEmpty()) {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@TeamPlayersActivity, "Склад порожній або помилка", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val type = object : TypeToken<List<Player>>() {}.type
                    val players: List<Player> = Gson().fromJson(json, type)

                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        // Передаємо адаптер
                        recyclerView.adapter = PlayersAdapter(players)
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@TeamPlayersActivity, "Помилка обробки даних (JSON)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
