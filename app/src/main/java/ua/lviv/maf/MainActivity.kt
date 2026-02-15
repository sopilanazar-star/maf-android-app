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

    private val seasons = arrayOf("2026", "2025", "2024")
    private var currentYear = seasons[0]
    private var allMatches = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val rootFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        val mainContentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }

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

        seasonSpinner = Spinner(this)
        seasonSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, seasons)

        headerLayout.addView(titleHeader)
        headerLayout.addView(seasonSpinner)

        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        dateRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        viewPager = ViewPager2(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        newsRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(viewPager)
        contentLayout.addView(newsRecyclerView)

        fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            visibility = View.GONE
        }

        mainContentContainer.addView(headerLayout)
        mainContentContainer.addView(contentLayout)
        mainContentContainer.addView(fragmentContainer)

        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.parseColor("#121417"))
            layoutParams = FrameLayout.LayoutParams(-1, -2).apply { gravity = Gravity.BOTTOM }
        }

        rootFrame.addView(mainContentContainer)
        rootFrame.addView(bottomNav)
        setContentView(rootFrame)

        loadFromApi(currentYear)
        loadNewsFromApi()
    }

    private fun loadNewsFromApi() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_NEWS_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                val array = JSONArray(jsonData)
                val newsList = mutableListOf<NewsModel>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    newsList.add(
                        NewsModel(
                            obj.optString("id"),
                            obj.optString("title"),
                            obj.optString("preview"),
                            obj.optString("content"),
                            obj.optString("date")
                        )
                    )
                }
                runOnUiThread {
                    newsRecyclerView.adapter = NewsAdapter(newsList)
                }
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
                val array = JSONArray(jsonData)

                allMatches.clear()

                for (i in 0 until array.length()) {
                    val m = array.getJSONObject(i)
                    allMatches.add(
                        TournamentRow(
                            id = m.optString("id"),
                            home_team_id = m.optString("home_team_id"),
                            away_team_id = m.optString("away_team_id"),
                            team1 = m.optString("team1"),
                            logo1 = m.optString("logo1"),
                            team2 = m.optString("team2"),
                            logo2 = m.optString("logo2"),
                            score = m.optString("score"),
                            date = m.optString("date"),
                            league = m.optString("league"),
                            stage = m.optString("stage"),
                            stadium = m.optString("stadium"),
                            referee = m.optString("referee"),
                            isHeader = false
                        )
                    )
                }

                runOnUiThread {
                    val dateList = createDateList(allMatches)
                    viewPager.adapter = MatchSwipeAdapter(dateList)
                }
            }
        })
    }

    private fun createDateList(matches: List<TournamentRow>): List<DateModel> {
        val list = mutableListOf<DateModel>()
        val unique = matches.map { it.date }.distinct()

        for (d in unique) {
            list.add(DateModel(d, "", "", ""))
        }
        return list
    }

    private fun groupMatchesByLeagueAndStage(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }

        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            result.add(TournamentRow(league = parts[0], stage = parts.getOrElse(1) { "" }, isHeader = true))
            result.addAll(leagueMatches)
        }
        return result
    }

    // 🔥 ВАЖЛИВО: АДАПТЕР ВСЕРЕДИНІ MainActivity
    inner class MatchSwipeAdapter(private val dates: List<DateModel>) :
        RecyclerView.Adapter<MatchSwipeAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val rvMatches: RecyclerView = RecyclerView(view.context).apply {
                layoutManager = LinearLayoutManager(view.context)
                layoutParams = ViewGroup.LayoutParams(-1, -1)
            }

            init {
                (view as FrameLayout).addView(rvMatches)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val frame = FrameLayout(parent.context)
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
