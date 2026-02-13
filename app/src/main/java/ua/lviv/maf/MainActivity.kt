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

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var titleHeader: TextView
    private lateinit var contentLayout: LinearLayout
    private lateinit var seasonSpinner: Spinner
    private lateinit var fragmentContainer: FrameLayout // Додаємо контейнер для фрагментів

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

        // --- CONTENT LAYOUT (для списків) ---
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
            setPadding(0, 0, 0, 0) // Прибираємо великий паддінг, бо тепер є контейнер
            clipToPadding = false
        }

        newsRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            setPadding(0, 0, 0, 0)
            clipToPadding = false
            visibility = View.VISIBLE
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(recyclerView)
        contentLayout.addView(newsRecyclerView)

        // --- ФРАГМЕНТ КОНТЕЙНЕР (для StandingFragment) ---
        fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId() // Генеруємо ID для фрагмент менеджера
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
                
                // Логіка перемикання видимості
                when (item.itemId) {
                    R.id.nav_matches -> {
                        fragmentContainer.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        dateRecyclerView.visibility = View.VISIBLE
                        newsRecyclerView.visibility = View.GONE
                        seasonSpinner.visibility = View.VISIBLE
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
                        seasonSpinner.visibility = View.GONE
                        fragmentContainer.visibility = View.VISIBLE
                        
                        // Завантажуємо фрагмент у контейнер
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

    // ... Твої методи loadNewsFromApi, loadFromApi, createDateList, filterMatches, groupMatchesByLeagueAndStage залишаються без змін ...
