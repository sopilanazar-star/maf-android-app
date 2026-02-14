package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

// ПРАВИЛЬНИЙ ІМПОРТ: тепер ми беремо модель з папки models
import ua.lviv.maf.models.Player 

class TeamPlayersActivity : AppCompatActivity() {

    private var teamId: Int = 0
    private var teamName: String = ""
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlayersAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        teamId = intent.getIntExtra("team_id", 0)
        teamName = intent.getStringExtra("team_name") ?: "Команда"

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val tvTitle = TextView(this).apply {
            text = teamName
            setTextColor(Color.WHITE)
            textSize = 22f
            setPadding(0, 40, 0, 20)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        progressBar = ProgressBar(this).apply {
            visibility = android.view.View.VISIBLE
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@TeamPlayersActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        rootLayout.addView(tvTitle)
        rootLayout.addView(progressBar)
        rootLayout.addView(recyclerView)

        setContentView(rootLayout)

        if (teamId != 0) {
            loadPlayers(teamId)
        } else {
            progressBar.visibility = android.view.View.GONE
            Toast.makeText(this, "Помилка ID команди", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPlayers(id: Int) {
        val client = OkHttpClient()
        val url = "https://maf.lviv.ua/wp-json/maf/v2/team-players?id=$id"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@TeamPlayersActivity, "Помилка мережі", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (response.isSuccessful && json != null) {
                    // Тут Gson тепер точно знає, що Player — це ua.lviv.maf.models.Player
                    val playerType = object : TypeToken<List<Player>>() {}.type
                    val players: List<Player> = Gson().fromJson(json, playerType)

                    runOnUiThread {
                        progressBar.visibility = android.view.View.GONE
                        adapter = PlayersAdapter(players)
                        recyclerView.adapter = adapter
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = android.view.View.GONE
                    }
                }
            }
        })
    }
}
