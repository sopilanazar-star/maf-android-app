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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewPagerMatches: ViewPager2 // Замість recyclerView для підтримки свайпів
    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var titleHeader: TextView
    private lateinit var contentLayout: LinearLayout
    private lateinit var seasonSpinner: Spinner
    private lateinit var fragmentContainer: FrameLayout

    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private val MAF_NEWS_URL = "https://maf.lviv.ua/wp-json/maf/v2/news"
    private val MAF_STANDINGS_URL = "https://maf.lviv.ua/wp-json/maf/v2/standing"

    private var allMatches = mutableListOf<TournamentRow>()
    private var dateList = mutableListOf<DateModel>()

    // 🔴 Твій оригінальний handler для автооновлення - ЗБЕРЕЖЕНО
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
            val spinnerAdapter = object : ArrayAdapter<String>(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                seasons
            ) {
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

            val selectedIndex = seasons.indexOf(AppConfig.selectedYear)
            if (selectedIndex != -1) setSelection(selectedIndex) else setSelection(0)
        }

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYearStr = seasons[position]
                if (AppConfig.selectedYear != selectedYearStr) {
                    AppConfig.selectedYear = selectedYearStr
                    loadFromApi(AppConfig.selectedYear)

                    // 🔴 Твій оригінальний виклик оновлення фрагментів - ПОВЕРНУТО
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

        // 🔴 ViewPager2 тепер замість старого RecyclerView - це єдина зміна в UI
        viewPagerMatches = ViewPager2(this).apply {
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
        contentLayout.addView(viewPagerMatches)
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
                        viewPagerMatches.visibility = View.VISIBLE
                        dateRecyclerView.visibility = View.VISIBLE
                        newsRecyclerView.visibility = View.GONE
                        seasonSpinner.visibility = View.VISIBLE
                        checkAutoRefresh()
                    }
                    R.id.nav_news -> {
                        fragmentContainer.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        viewPagerMatches.visibility = View.GONE
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

    // 🔴 Твоя оригінальна логіка автооновлення - ЗБЕРЕЖЕНО
    private fun checkAutoRefresh() {
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
                val array = JSONArray(jsonData)
                val newsList = mutableListOf<NewsModel>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    newsList.add(NewsModel(obj.optString("id"), obj.optString("title"), obj.optString("preview"), obj.optString("content"), obj.optString("date")))
                }
                runOnUiThread { newsRecyclerView.adapter = NewsAdapter(newsList) }
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
                    allMatches.add(TournamentRow(
                        id = m.optString("id"), home_team_id = m.optString("home_team_id"),
                        away_team_id = m.optString("away_team_id"), team1 = m.optString("team1"),
                        logo1 = m.optString("logo1"), team2 = m.optString("team2"),
                        logo2 = m.optString("logo2"), score = m.optString("score"),
                        date = m.optString("date"), league = m.optString("league"),
                        stage = m.optString("stage"), stadium = m.optString("stadium"),
                        referee = m.optString("referee"), status = m.optString("status"),
                        isHeader = false
                    ))
                }

                runOnUiThread {
                    setupViewPager()
                    checkAutoRefresh()
                }
            }
        })
    }

    private fun setupViewPager() {
        dateList = createDateList(allMatches).toMutableList()
        if (dateList.isEmpty()) return

        val dateAdapter = DateAdapter(dateList) { date ->
            val pos = dateList.indexOfFirst { it.date == date }
            if (pos != -1) viewPagerMatches.setCurrentItem(pos, true)
        }
        dateRecyclerView.adapter = dateAdapter

        val pagerAdapter = MatchPagerAdapter(this, dateList, allMatches)
        viewPagerMatches.adapter = pagerAdapter

        viewPagerMatches.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                dateAdapter.updateSelection(position)
                dateRecyclerView.scrollToPosition(position)
            }
        })
        
        viewPagerMatches.setCurrentItem(0, false)
    }

    private fun createDateList(matches: List<TournamentRow>): List<DateModel> {
        val uniqueDates = matches.map { it.date }.distinct().sortedByDescending {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it)
        }
        val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale("uk"))
        val dayNumFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale("uk"))

        return uniqueDates.mapNotNull { dateStr ->
            val date = inputFormat.parse(dateStr) ?: return@mapNotNull null
            DateModel(dateStr, dayNameFormat.format(date).uppercase(), dayNumFormat.format(date), monthFormat.format(date))
        }
    }
}

// 🔥 Адаптер для сторінок ViewPager
class MatchPagerAdapter(
    activity: AppCompatActivity,
    private val dates: List<DateModel>,
    private val allMatches: List<TournamentRow>
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = dates.size
    override fun createFragment(position: Int): Fragment {
        val date = dates[position].date
        return MatchPageFragment.newInstance(allMatches.filter { it.date == date })
    }
}

// 🔥 Фрагмент однієї сторінки (один день матчів)
class MatchPageFragment : Fragment() {
    companion object {
        fun newInstance(matches: List<TournamentRow>) = MatchPageFragment().apply { this.matches = matches }
    }
    var matches: List<TournamentRow> = emptyList()

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setPadding(0, 0, 0, (90 * resources.displayMetrics.density).toInt())
            clipToPadding = false
        }
        rv.adapter = TournamentAdapter(groupMatches(matches))
        return rv
    }

    private fun groupMatches(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }
        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            result.add(TournamentRow(league = parts[0], stage = parts.getOrElse(1) { "" }, isHeader = true))
            result.addAll(leagueMatches)
        }
        return result
    }
}
