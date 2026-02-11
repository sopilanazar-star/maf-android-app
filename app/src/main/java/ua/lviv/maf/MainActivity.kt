package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var titleHeader: TextView
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    
    private var allMatches = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        titleHeader = TextView(this).apply {
            text = "Матчі"
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23")))
            setPadding(60, 100, 40, 60)
        }

        // 1. ГОРИЗОНТАЛЬНИЙ КАЛЕНДАР
        dateRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            setPadding(20, 0, 20, 0)
            clipToPadding = false
        }

        // 2. ОСНОВНИЙ СПИСОК МАТЧІВ
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.parseColor("#121417"))
            itemIconTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
            itemTextColor = android.content.res.ColorStateList.valueOf(Color.GRAY)
        }

        mainLayout.addView(titleHeader)
        mainLayout.addView(dateRecyclerView)
        mainLayout.addView(recyclerView)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        loadFromApi()
    }

    private fun loadFromApi() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    allMatches.clear()
                    
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        allMatches.add(TournamentRow(
                            team1 = m.optString("team1", ""),
                            logo1 = m.optString("logo1", ""),
                            team2 = m.optString("team2", ""),
                            logo2 = m.optString("logo2", ""),
                            score = m.optString("score", ""),
                            date  = m.optString("date", ""),
                            league = m.optString("league", "MAF"),
                            stage = m.optString("stage", ""), // Витягуємо етап (тур)
                            isHeader = false
                        ))
                    }

                    val dateList = createDateList(allMatches)

                    runOnUiThread {
                        dateRecyclerView.adapter = DateAdapter(dateList) { selectedDate ->
                            filterMatches(selectedDate)
                        }
                        
                        if (dateList.isNotEmpty()) {
                            dateList[0].isSelected = true
                            filterMatches(dateList[0].date)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }

    private fun createDateList(matches: List<TournamentRow>): List<DateModel> {
        val uniqueDates = matches.map { it.date }.distinct().sortedByDescending { 
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it) 
        }
        
        val calendarList = mutableListOf<DateModel>()
        val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale("uk"))
        val dayNumFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale("uk"))

        uniqueDates.forEach { dateStr ->
            val date = inputFormat.parse(dateStr)
            if (date != null) {
                calendarList.add(DateModel(
                    date = dateStr,
                    dayName = dayNameFormat.format(date).uppercase(),
                    dayNumber = dayNumFormat.format(date),
                    month = monthFormat.format(date)
                ))
            }
        }
        return calendarList
    }

    private fun filterMatches(date: String) {
        // 1. Фільтруємо всі матчі за обраною датою
        val filteredByDate = allMatches.filter { it.date == date }
        
        // 2. Автоматично групуємо їх за лігами та етапами
        val groupedList = groupMatchesByLeagueAndStage(filteredByDate)
        
        runOnUiThread {
            recyclerView.adapter = TournamentAdapter(groupedList)
        }
    }

    private fun groupMatchesByLeagueAndStage(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        
        // Групуємо матчі: ключ складається з Назви Ліги та Етапу
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }
        
        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            val leagueTitle = parts[0]
            val stageTitle = parts.getOrNull(1) ?: ""

            // Додаємо віртуальний рядок-заголовок
            result.add(TournamentRow(
                league = leagueTitle, 
                stage = stageTitle, 
                isHeader = true
            ))
            
            // Додаємо самі матчі, що належать до цієї ліги/етапу
            result.addAll(leagueMatches)
        }
        return result
    }
}
