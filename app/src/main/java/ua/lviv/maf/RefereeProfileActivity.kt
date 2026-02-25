package ua.lviv.maf

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class RefereeProfileActivity : AppCompatActivity() {

    private lateinit var rvMatches: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private val client = OkHttpClient()

    private var refereeName: String = ""
    private var selectedYear: String = "2025"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referee_profile)

        // Отримуємо дані з Інтенту
        refereeName = intent.getStringExtra("REF_NAME") ?: "Арбітр"
        selectedYear = intent.getStringExtra("YEAR") ?: "2025"
        val refPhoto = intent.getStringExtra("REF_PHOTO")
        val refCity = intent.getStringExtra("REF_CITY") ?: ""
        val matchesCount = intent.getIntExtra("REF_MATCHES", 0)
        val yellowCards = intent.getIntExtra("REF_YELLOW", 0)
        val redCards = intent.getIntExtra("REF_RED", 0)

        // Знаходимо в'юшки
        rvMatches = findViewById(R.id.rvRefereeMatches)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        // Заповнюємо шапку
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Сезон $selectedYear"
        findViewById<TextView>(R.id.tvProfileName).text = refereeName
        findViewById<TextView>(R.id.tvProfileRole).text = if (refCity.isNotEmpty()) "м. $refCity" else "Арбітр МАФ"
        
        findViewById<TextView>(R.id.tvProfileMatches).text = matchesCount.toString()
        findViewById<TextView>(R.id.tvProfileYellow).text = yellowCards.toString()
        findViewById<TextView>(R.id.tvProfileRed).text = redCards.toString()

        Glide.with(this)
            .load(refPhoto)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_player_placeholder)
            .into(findViewById<ImageView>(R.id.ivProfilePhoto))

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        // Завантажуємо матчі
        loadRefereeMatches()
    }

    private fun loadRefereeMatches() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvMatches.visibility = View.GONE

        // Робимо запит до глобального списку матчів за вибраний рік
        val url = "https://maf.lviv.ua/wp-json/maf/v2/matches?year=$selectedYear"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    try {
                        val array = JSONArray(jsonData)
                        val allMatches = mutableListOf<TournamentRow>()
                        
                        for (i in 0 until array.length()) {
                            val m = array.getJSONObject(i)
                            allMatches.add(TournamentRow(
                                id = m.optString("id"), 
                                team1 = m.optString("team1"), 
                                logo1 = m.optString("logo1"),
                                team2 = m.optString("team2"), 
                                logo2 = m.optString("logo2"), 
                                score = m.optString("score"), 
                                date = m.optString("date"),   
                                league = m.optString("league"), 
                                stage = m.optString("stage"),
                                isHeader = false,
                                home_team_id = m.optString("home_team_id"),
                                away_team_id = m.optString("away_team_id"),
                                stadium = m.optString("stadium"), 
                                referee = m.optString("referee"), // Отримуємо арбітра з матчу!
                                status = m.optString("status")
                            ))
                        }

                        // 🔥 ФІЛЬТРУЄМО ТІЛЬКИ МАТЧІ ЦЬОГО АРБІТРА 🔥
                        val filteredMatches = allMatches.filter { 
                            it.referee.contains(refereeName, ignoreCase = true) 
                        }

                        if (filteredMatches.isEmpty()) {
                            showEmptyState()
                        } else {
                            // Групуємо по лігах для красивих заголовків (як у MainActivity)
                            val grouped = mutableListOf<TournamentRow>()
                            val groupedByLeague = filteredMatches.groupBy { "${it.league}|${it.stage}" }
                            for ((key, matches) in groupedByLeague) {
                                val parts = key.split("|")
                                grouped.add(TournamentRow(league = parts[0], stage = parts.getOrElse(1) { "" }, isHeader = true))
                                grouped.addAll(matches)
                            }

                            // Використовуємо твій готовий TournamentAdapter!
                            rvMatches.visibility = View.VISIBLE
                            rvMatches.layoutManager = LinearLayoutManager(this@RefereeProfileActivity)
                            rvMatches.adapter = TournamentAdapter(grouped)
                        }
                    } catch (e: Exception) {
                        showEmptyState()
                    }
                }
            }
        })
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        rvMatches.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}
