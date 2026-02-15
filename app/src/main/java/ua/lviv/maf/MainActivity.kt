package ua.lviv.maf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var titleHeader: TextView
    private lateinit var contentLayout: LinearLayout
    private lateinit var seasonSpinner: Spinner
    private lateinit var fragmentContainer: FrameLayout

    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private val MAF_NEWS_URL = "https://maf.lviv.ua/wp-json/maf/v2/news"
    
    private var currentYear = "2025"
    private var allMatches = mutableListOf<TournamentRow>()
    private val seasons = arrayOf("2026", "2025", "2024")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
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
                    val v = super.getView(position, convertView, parent)
                    (v as TextView).apply {
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    return v
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent)
                    (v as TextView).apply {
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#252932"))
                        setPadding(30, 30, 30, 30)
                    }
                    return v
                }
            }
            this.adapter = spinnerAdapter
            setSelection(1) 
        }

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

        headerLayout.addView(titleHeader)
        headerLayout.addView(seasonSpinner)

        // --- CONTENT LAYOUT ---
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

        viewPager = ViewPager2(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER 
        }

        newsRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            setPadding(0, 0, 0, 0)
            clipToPadding = false
            visibility = View.VISIBLE
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(viewPager) // Додаємо ViewPager
        contentLayout.addView(newsRecyclerView)

        // --- FRAGMENT CONTAINER ---
        fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            visibility = View.GONE
        }
        
        mainContentContainer.addView(headerLayout)
        mainContentContainer.addView(contentLayout)
        mainContentContainer.addView(fragmentContainer)

        // --- NAVIGATION ---
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
                when (item.itemId) {
                    R.id.nav_matches -> {
                        fragmentContainer.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        viewPager.visibility = View.VISIBLE
                        dateRecyclerView.visibility = View.VISIBLE
                        newsRecyclerView.visibility = View.GONE
                        seasonSpinner.visibility = View.VISIBLE
                    }
                    R.id.nav_news -> {
                        fragmentContainer.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        viewPager.visibility = View.GONE
                        dateRecyclerView.visibility = View.GONE
                        newsRecyclerView.visibility = View.VISIBLE
                        seasonSpinner.visibility = View.GONE
                        loadNewsFromApi()
                    }
                    R.id.nav_tables -> {
                        contentLayout.visibility = View.GONE
                        seasonSpinner.visibility = View.GONE
                        fragmentContainer.visibility = View.VISIBLE
                        supportFragmentManager.beginTransaction()
                            .replace(fragmentContainer.id, StandingFragment())
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

        loadFromApi(currentYear)
        loadNewsFromApi()
    }

    private fun loadNewsFromApi() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_NEWS_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }
            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    val newsList = mutableListOf<NewsModel>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        newsList.add(NewsModel(
                            obj.optString("id", "0"),
                            obj.optString("title", ""),
                            obj.optString("preview", ""),
                            obj.optString("content", ""),
                            obj.optString("date", "")
                        ))
                    }
                    runOnUiThread { newsRecyclerView.adapter = NewsAdapter(newsList) }
                } catch (e: Exception) { e.printStackTrace() }
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
                    allMatches.clear()
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        allMatches.add(TournamentRow(
                            id = m.optString("id", "0"),
                            home_team_id = m.optString("home_team_id", "0"),
                            away_team_id = m.optString("away_team_id", "0"),
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
                        
                        val swipeAdapter = MatchSwipeAdapter(dateList)
                        viewPager.adapter = swipeAdapter

                        dateRecyclerView.adapter = DateAdapter(dateList) { selectedDate ->
                            val pos = dateList.indexOfFirst { it.date == selectedDate }
                            if (pos != -1) {
                                viewPager.setCurrentItem(pos, true)
                            }
                        }

                        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                dateList.forEach { it.isSelected = false }
                                dateList[position].isSelected = true
                                dateRecyclerView.adapter?.notifyDataSetChanged()
                                dateRecyclerView.scrollToPosition(position)
                            }
                        })

                        if (dateList.isNotEmpty()) {
                            dateList[0].isSelected = true
                            viewPager.setCurrentItem(0, false)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }

    private fun createDateList(matches: List<TournamentRow>): List<DateModel> {
        val calendarList = mutableListOf<DateModel>()
        try {
            val uniqueDates = matches.map { it.date }.distinct().sortedByDescending { 
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it) 
            }
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

    private fun groupMatchesByLeagueAndStage(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }
        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            result.add(TournamentRow(league = parts[0], stage = if (parts.size > 1) parts[1] else "", isHeader = true))
            result.addAll(leagueMatches)
        }
        return result
    }

    inner class MatchSwipeAdapter(private val dates: List<DateModel>) : RecyclerView.Adapter<MatchSwipeAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val rvMatches: RecyclerView = RecyclerView(view.context).apply {
                layoutManager = LinearLayoutManager(view.context)
                layoutParams = ViewGroup.LayoutParams(-1, -1)
            }
            init { (view as FrameLayout).addView(rvMatches) }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val frame = FrameLayout(parent.context).apply { layoutParams = ViewGroup.LayoutParams(-1, -1) }
            return ViewHolder(frame)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val date = dates[position].date
            val filtered = allMatches.filter { it.date == date }
            val grouped = groupMatchesByLeagueAndStage(filtered)
            holder.rvMatches.adapter = TournamentAdapter(grouped)
        }
        override fun getItemCount(): Int = dates.size
    }
}
