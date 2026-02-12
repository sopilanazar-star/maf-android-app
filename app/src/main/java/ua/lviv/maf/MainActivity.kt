package ua.lviv.maf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
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
    private lateinit var liveButton: LinearLayout
    
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private var currentYear = "2025"
    private var allMatches = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // 1. ПРИБИРАЄМО ЗЕЛЕНІ СМУЖКИ (Edge-to-Edge)
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
            setPadding(40, 120, 40, 40) // Збільшений відступ зверху для StatusBar
        }

        // КНОПКА LIVE
        liveButton = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(20, 10, 20, 10)
            val stroke = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 15f
                setStroke(3, Color.parseColor("#E30613"))
                setColor(Color.parseColor("#33E30613"))
            }
            background = stroke
            addView(TextView(context).apply {
                text = "● LIVE"
                setTextColor(Color.parseColor("#E30613"))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            })
            setOnClickListener { Toast.makeText(context, "Пошук прямих ефірів...", Toast.LENGTH_SHORT).show() }
        }

        titleHeader = TextView(this).apply {
            text = "Новини"
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        // БІЛИЙ СПІНЕР РОКІВ
        seasonSpinner = Spinner(this).apply {
            val seasons = arrayOf("2026", "2025", "2024")
            val adapter = object : ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_spinner_item, seasons) {
                override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                    val v = super.getView(position, convertView, parent)
                    (v as TextView).setTextColor(Color.WHITE) // Білий текст у закритому стані
                    v.textSize = 14f
                    return v
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            setSelection(1)
        }

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = parent?.getItemAtPosition(position).toString()
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
            setPadding(20, 0, 20, 0)
            clipToPadding = false
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setPadding(0, 0, 0, 180) // Відступ, щоб не перекривало контент меню
            clipToPadding = false
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(recyclerView)

        mainContentContainer.addView(headerLayout)
        mainContentContainer.addView(contentLayout)

        // --- НИЖНЯ НАВІГАЦІЯ ---
        val navColorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(-android.R.attr.state_selected)),
            intArrayOf(Color.parseColor("#E30613"), Color.parseColor("#808080"))
        )

        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.parseColor("#121417"))
            itemIconTintList = navColorStateList
            itemTextColor = navColorStateList
            itemRippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(-1, -2).apply { gravity = Gravity.BOTTOM }

            setOnItemSelectedListener { item ->
                titleHeader.text = item.title
                contentLayout.visibility = if (item.itemId == R.id.nav_matches) View.VISIBLE else View.GONE
                true
            }
            selectedItemId = R.id.nav_news
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
                        allMatches.add(TournamentRow(
                            m.optString("id", "0"), m.optString("team1", ""), m.optString("logo1", ""),
                            m.optString("team2", ""), m.optString("logo2", ""), m.optString("score", ""),
                            m.optString("date", ""), m.optString("league", "MAF"), m.optString("stage", ""),
                            m.optString("stadium", ""), m.optString("referee", ""), false
                        ))
                    }
                    runOnUiThread {
                        val dateList = createDateList(allMatches)
                        dateRecyclerView.adapter = DateAdapter(dateList) { filterMatches(it) }
                        if (dateList.isNotEmpty()) {
                            dateList[0].isSelected = true
                            filterMatches(dateList[0].date)
                        } else { recyclerView.adapter = TournamentAdapter(emptyList()) }
                    }
                } catch (e: Exception) {}
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
                calendarList.add(DateModel(dateStr, dayNameFormat.format(date).uppercase(), dayNumFormat.format(date), monthFormat.format(date)))
            }
        }
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
