package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
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

        teamId = intent.getStringExtra("team_id") ?: ""
        teamName = intent.getStringExtra("team_name") ?: "Команда"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        val title = TextView(this).apply {
            text = teamName
            textSize = 22f
            setTextColor(Color.WHITE)
            setPadding(0, 40, 0, 20)
            gravity = android.view.Gravity.CENTER
        }

        progressBar = ProgressBar(this)

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@TeamPlayersActivity)
        }

        root.addView(title)
        root.addView(progressBar)
        root.addView(recyclerView)

        setContentView(root)

        if (teamId.isNotEmpty() && teamId != "0") {
            loadPlayers()
        } else {
            Toast.makeText(this, "team_id втрачено", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this@TeamPlayersActivity, "Нема складу", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val type = object : TypeToken<List<Player>>() {}.type
                    val players: List<Player> = Gson().fromJson(json, type)

                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        recyclerView.adapter = PlayersAdapter(players)
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@TeamPlayersActivity, "JSON error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
