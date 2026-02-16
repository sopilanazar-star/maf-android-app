package ua.lviv.maf

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonParser
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

        val idInt = intent.getIntExtra("team_id", 0)
        teamId = if (idInt != 0) idInt.toString() else intent.getStringExtra("team_id") ?: ""
        teamName = intent.getStringExtra("team_name") ?: "Команда"

        // UI
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
                    
                    if (!response.isSuccessful || rawJson.isEmpty()) {
                        Toast.makeText(this@TeamPlayersActivity, "Сервер мовчить", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    try {
                        // 🔥 РУЧНЕ КЕРУВАННЯ:
                        // 1. Парсимо структуру як загальний елемент
                        val jsonElement = JsonParser.parseString(rawJson)

                        val playersList = ArrayList<Player>()

                        if (jsonElement.isJsonArray) {
                            // ВАРІАНТ А: Це нормальний список [...]
                            val type = object : TypeToken<List<Player>>() {}.type
                            playersList.addAll(Gson().fromJson(jsonElement, type))

                        } else if (jsonElement.isJsonObject) {
                            // ВАРІАНТ Б: Це об'єкт (наприклад, PHP array "0":{}, "1":{})
                            val jsonObject = jsonElement.asJsonObject
                            // Проходимося по всіх ключах і пробуємо витягнути гравців
                            for (key in jsonObject.keySet()) {
                                try {
                                    val item = jsonObject.get(key)
                                    val player = Gson().fromJson(item, Player::class.java)
                                    playersList.add(player)
                                } catch (e: Exception) {
                                    // Ігноруємо ключі, які не є гравцями (наприклад "status": "ok")
                                }
                            }
                        } else {
                            // ВАРІАНТ В: Це false, null або примітив
                            Toast.makeText(this@TeamPlayersActivity, "Дані гравців відсутні", Toast.LENGTH_SHORT).show()
                            return@try
                        }

                        if (playersList.isNotEmpty()) {
                            recyclerView.adapter = PlayersAdapter(playersList)
                        } else {
                            Toast.makeText(this@TeamPlayersActivity, "Список гравців порожній", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e("TeamPlayers", "Error: ${e.message}")
                        // 🔥 ПОКАЗУЄМО ТОБІ, ЩО ПРИЙШЛО, ЯКЩО ЗНОВУ ПОМИЛКА
                        showError("Що надіслав сервер?", rawJson)
                    }
                }
            }
        })
    }

    // Діалог для відладки
    private fun showError(title: String, message: String) {
        progressBar.visibility = ProgressBar.GONE
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message.take(500)) // Показуємо перші 500 символів
            .setPositiveButton("OK", null)
            .show()
    }
}
