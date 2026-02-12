package ua.lviv.maf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
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
    private lateinit var contentLayout: LinearLayout
    private lateinit var seasonSpinner: Spinner
    
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private var currentYear = "2025"
    private var allMatches = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#121417")

        val rootFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        val mainContentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }

        // --- HEADER ---
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, 
                intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23")))
            setPadding(40, 120, 40, 40)
        }

        // LIVE Button
        val liveButton = LinearLayout(this).apply {
            setPadding(20, 10, 20, 10)
            background = GradientDrawable().apply {
                cornerRadius = 15f
                setStroke(3, Color.parseColor("#E30613"))
            }
            addView(TextView(context).apply {
                text = "● LIVE"
                setTextColor(Color.parseColor("#E30613"))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            })
        }

        titleHeader = TextView(this).apply {
            text = "Новини"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        // Spinner з виправленим адаптером
        seasonSpinner = Spinner(this)
        val seasons = arrayOf("2026", "2025", "2024")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, seasons)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seasonSpinner.adapter = spinnerAdapter
        seasonSpinner.setSelection(1)

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = seasons[position]
                if (currentYear != selectedYear) {
                    currentYear = selectedYear
                    loadFromApi(currentYear)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        headerLayout.addView(liveButton)
        headerLayout.addView(titleHeader)
        headerLayout.addView(seasonSpinner)

        // --- CONTENT ---
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            visibility = View.GONE 
        }

        dateRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            setPadding(0, 0, 0, 200)
            clipToPadding = false
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(recyclerView)
        mainContentContainer.addView(headerLayout)
        mainContentContainer.addView(contentLayout)

        // Navigation
        val navColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(-android.R.attr.state_selected)),
            intArrayOf(Color.parseColor("#E30613"), Color.GRAY)
        )

        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.parseColor("#121417"))
            itemIconTintList = navColors
            itemTextColor = navColors
            layoutParams = FrameLayout.LayoutParams(-1, -2).apply { gravity = Gravity.BOTTOM }

            setOnItemSelectedListener { item ->
                titleHeader.text = item.title
                contentLayout.visibility = if (item.itemId == R.id.nav_matches) View.VISIBLE else View.GONE
                true
            }
        }

        rootFrame.addView(mainContentContainer)
        rootFrame.addView(bottomNav)
        setContentView(rootFrame)

        loadFromApi(currentYear)
    }

    private fun loadFromApi(year: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url("$MAF_API_URL?year=$year").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    allMatches.clear()
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        // Безпечний парсинг, щоб не вилітало
                        allMatches.add(TournamentRow(
                            id = m.optString("id", "0"),
                            team1 = m.optString("team1", ""),
                            logo1 = m.optString("logo1", ""),
                            team2 = m.optString("team2", ""),
                            logo2 = m.optString("logo2", ""),
                            score = m.optString("score", ""),
                            date = m.optString("date", ""),
                            league = m.optString("league", "MAF"),
                            stage = m.optString("stage", ""),
                            stadium = m.optString("stadium", ""),
                            referee = m.optString("referee", ""),
                            isHeader = false
                        ))
                    }
                    runOnUiThread {
                        val dateList = createDateList(allMatches)
                        dateRecyclerView.adapter = DateAdapter(dateList) { filterMatches(it) }
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
        val calendarList = mutableListOf<DateModel>()
        try {
            val uniqueDates = matches.map { it.date }.distinct()
            val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dayNameFormat = SimpleDateFormat("EEE", Locale("uk"))
            val dayNumFormat = SimpleDateFormat("dd", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMM", Locale("uk"))

            uniqueDates.forEach { dateStr ->
                val date = inputFormat.parse(dateStr)
                if (date != null) {
                    calendarList.add(DateModel(dateStr, dayNameFormat.format(date).uppercase(), dayNumFormat.format(date), monthFormat.format(date)))
                }
            }
        } catch (e: Exception) {}
        return calendarList
    }

    private fun filterMatches(date: String) {
        val filtered = allMatches.filter { it.date == date }
        val grouped = groupMatchesByLeagueAndStage(filtered)
        runOnUiThread { recyclerView.adapter = TournamentAdapter(grouped) }
    }

    private fun groupMatchesByLeagueAndStage(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }
        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            result.add(TournamentRow("0", "", "", "", "", "", "", parts[0], parts.getOrNull(1) ?: "", "", "", true))
            result.addAll(leagueMatches)
        }
        return result
    }
}
