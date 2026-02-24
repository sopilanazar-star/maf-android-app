package ua.lviv.maf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var titleHeader: TextView
    private lateinit var contentLayout: LinearLayout
    private lateinit var seasonSpinner: Spinner
    private lateinit var fragmentContainer: FrameLayout

    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private val MAF_NEWS_URL = "https://maf.lviv.ua/wp-json/maf/v2/news"

    private var allMatches = mutableListOf<TournamentRow>()

    // Handler для автооновлення LIVE-рахунків
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadFromApi(AppConfig.selectedYear)
            handler.postDelayed(this, 60000L)
        }
    }

    private val seasons: List<String> = generateSeasons()

    private fun generateSeasons(): List<String> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val maxYear = if (currentYear >= 2024) currentYear else 2024
        return (maxYear downTo 2024).map { it.toString() }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Налаштування прозорого статус-бару
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val rootFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        val mainContentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }

        // Header з градієнтом
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23"))
            )
            setPadding(60, 140, 60, 40)
        }

        titleHeader = TextView(this).apply {
            text = "Новини"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        seasonSpinner = Spinner(this).apply {
            val spinnerAdapter = object : ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_spinner_item, seasons) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent) as TextView
                    v.setTextColor(Color.WHITE)
                    v.textSize = 16f
                    v.typeface = Typeface.DEFAULT_BOLD
                    return v
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent) as TextView
                    v.setTextColor(Color.WHITE)
                    v.setBackgroundColor(Color.parseColor("#252932"))
                    v.setPadding(30, 30, 30, 30)
                    return v
                }
            }
            adapter = spinnerAdapter
            val selectedIndex = seasons.indexOf(AppConfig.selectedYear)
            setSelection(if (selectedIndex != -1) selectedIndex else 0)
        }

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYearStr = seasons[position]
                if (AppConfig.selectedYear != selectedYearStr) {
                    AppConfig.selectedYear = selectedYearStr
                    loadFromApi(AppConfig.selectedYear)

                    // Оновлення фрагментів таблиць при зміні сезону
                    val currentFragment = supportFragmentManager.findFragmentById(fragmentContainer.id)
                    if (currentFragment is StandingFragment) currentFragment.refreshData()
                    if (currentFragment is MoreFragment) currentFragment.refreshData()
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        headerLayout.addView(titleHeader)
        headerLayout.addView(seasonSpinner)

        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            visibility = View.GONE
        }

        dateRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            setPadding(20, 0, 20, 0)
            clipToPadding = false
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            setPadding(0, 0, 0, dpToPx(90))
            clipToPadding = false
        }

        newsRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            setPadding(0, 0, 0, dpToPx(90))
            clipToPadding = false
            visibility = View.VISIBLE
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(recyclerView)
        contentLayout.addView(newsRecyclerView)

        fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            visibility = View.GONE
        }

        mainContentContainer.addView(headerLayout)
        mainContentContainer.addView(contentLayout)
        mainContentContainer.addView(fragmentContainer)

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
                handler.removeCallbacks(refreshRunnable)

                when (item.itemId) {
                    R.id.nav_matches -> {
                        fragmentContainer.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        dateRecyclerView.visibility = View.VISIBLE
                        newsRecyclerView.visibility = View.GONE
                        seasonSpinner.visibility = View.VISIBLE
                        checkAutoRefresh()
                    }
                    R.id.nav_news -> {
                        fragmentContainer.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        dateRecyclerView.visibility = View.GONE
                        newsRecyclerView.visibility = View.VISIBLE
                        seasonSpinner.visibility = View.GONE
                        loadNewsFromApi()
                    }
                    R.id.nav_tables -> {
                        contentLayout.visibility = View.GONE
                        seasonSpinner.visibility = View.VISIBLE
                        fragmentContainer.visibility = View.VISIBLE
                        supportFragmentManager.beginTransaction()
                            .replace(fragmentContainer.id, StandingFragment())
                            .commit()
                    }
                    R.id.nav_more -> {
                        contentLayout.visibility = View.GONE
                        seasonSpinner.visibility = View.VISIBLE
                        fragmentContainer.visibility = View.VISIBLE
                        supportFragmentManager.beginTransaction()
                            .replace(fragmentContainer.id, MoreFragment())
                            .commit()
                    }
                }
                true
            }
            selectedItemId = R.id.nav_news
        }

        rootFrame.addView(mainContentContainer)
        rootFrame.addView(bottomNav)
        setContentView(rootFrame)

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        loadFromApi(AppConfig.selectedYear)
        loadNewsFromApi()
    }

    private fun checkAutoRefresh() {
        if (allMatches.isEmpty()) return
        val hasLive = allMatches.any { it.score.contains("'") || it.score == "HT" }
        handler.removeCallbacks(refreshRunnable)
        if (hasLive) handler.postDelayed(refreshRunnable, 60000L)
    }

    private fun loadNewsFromApi() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_NEWS_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    val newsList = mutableListOf<NewsModel>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        newsList.add(NewsModel(
                            obj.optString("id"), obj.optString("title"), 
                            obj.optString("preview"), obj.optString("content"), 
                            obj.optString("date")
                        ))
                    }
                    runOnUiThread { newsRecyclerView.adapter = NewsAdapter(newsList) }
                } catch (e: Exception) {}
            }
        })
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
                    val temp = mutableListOf<TournamentRow>()
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        // 🔴 ВАЖЛИВО: Суворий порядок полів, щоб score не зміщувався в date
                        temp.add(TournamentRow(
                            id = m.optString("id"),
                            home_team_id = m.optString("home_team_id"),
                            away_team_id = m.optString("away_team_id"),
                            team1 = m.optString("team1"),
                            logo1 = m.optString("logo1"),
                            team2 = m.optString("team2"),
                            logo2 = m.optString("logo2"),
                            score = m.optString("score"), // Тут буде час або рахунок
                            date = m.optString("date"),
                            league = m.optString("league"),
                            stage = m.optString("stage"),
                            stadium = m.optString("stadium"),
                            referee = m.optString("referee"),
                            isHeader = false
                        ))
                    }
                    allMatches = temp
                    runOnUiThread {
                        val dateList = createDateList(allMatches)
                        dateRecyclerView.adapter = DateAdapter(dateList) { filterMatches(it) }
                        if (dateList.isNotEmpty()) {
                            dateList[0].isSelected = true
                            filterMatches(dateList[0].date)
                        } else {
                            recyclerView.adapter = TournamentAdapter(emptyList())
                        }
                        checkAutoRefresh()
                    }
                } catch (e: Exception) {}
            }
        })
    }

    private fun filterMatches(date: String) {
        val filtered = allMatches.filter { it.date == date }
        val grouped = mutableListOf<TournamentRow>()
        val groupedByLeague = filtered.groupBy { "${it.league}|${it.stage}" }
        for ((key, matches) in groupedByLeague) {
            val parts = key.split("|")
            grouped.add(TournamentRow(league = parts[0], stage = parts.getOrElse(1) { "" }, isHeader = true))
            grouped.addAll(matches)
        }
        recyclerView.adapter = TournamentAdapter(grouped)
    }

    private fun createDateList(matches: List<TournamentRow>): List<DateModel> {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val uniqueDates = matches.map { it.date }.distinct().sortedByDescending {
            try { format.parse(it) } catch (e: Exception) { null }
        }
        
        val dayNameFormat = SimpleDateFormat("EEE", Locale("uk"))
        val dayNumFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale("uk"))

        return uniqueDates.mapNotNull { dateStr ->
            val date = try { format.parse(dateStr) } catch (e: Exception) { null } ?: return@mapNotNull null
            DateModel(dateStr, dayNameFormat.format(date).uppercase(), dayNumFormat.format(date), monthFormat.format(date))
        }
    }
}
