package ua.lviv.maf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*
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
    private lateinit var contentLayout: LinearLayout
    private lateinit var seasonSpinner: Spinner
    
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private var currentYear = "2025"
    
    private var allMatches = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }

        // --- ГОЛОВНИЙ КОНТЕЙНЕР (FRAME), ЩОБ МЕНЮ БУЛО ЗНИЗУ ---
        val rootFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        // Весь основний контент пакуємо в вертикальний LinearLayout
        val mainContentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(-1, -1).apply {
                // Робимо відступ знизу, щоб контент не перекривався меню (приблизно 60dp)
                setMargins(0, 0, 0, 160) 
            }
        }

        // --- HEADER ---
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23")))
            setPadding(60, 100, 40, 60)
        }

        titleHeader = TextView(this).apply {
            text = "Новини" // Починаємо з Новин
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        seasonSpinner = Spinner(this).apply {
            val seasons = arrayOf("2026", "2025", "2024")
            // Використовуємо вбудований ресурс для кращого вигляду тексту в спінері
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, seasons)
            this.adapter = adapter
            setSelection(1)
            setPadding(30, 0, 0, 0)
        }

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = parent?.getItemAtPosition(position).toString()
                if (currentYear != selectedYear) {
                    currentYear = selectedYear
                    loadFromApi(currentYear)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        headerLayout.addView(titleHeader)
        headerLayout.addView(seasonSpinner)

        // --- МАТЧІ ТА ДАТИ (ХОВАЄМО ДЛЯ НОВИН) ---
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
        }

        contentLayout.addView(dateRecyclerView)
        contentLayout.addView(recyclerView)

        // Додаємо все в контентний контейнер
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

            // Прив'язуємо меню строго до низу
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
            }

            setOnItemSelectedListener { item ->
                titleHeader.text = item.title
                when (item.itemId) {
                    R.id.nav_matches -> {
                        contentLayout.visibility = View.VISIBLE
                    }
                    else -> {
                        contentLayout.visibility = View.GONE
                    }
                }
                true
            }
            selectedItemId = R.id.nav_news // Перша вкладка при старті - Новини
        }

        // Збираємо все докупи в FrameLayout
        rootFrame.addView(mainContentContainer)
        rootFrame.addView(bottomNav)
        setContentView(rootFrame)

        loadFromApi(currentYear)
    }

    private fun loadFromApi(year: String) {
        val client = OkHttpClient()
        val urlWithYear = "$MAF_API_URL?year=$year"
        val request = Request.Builder().url(urlWithYear).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }
            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    allMatches.clear()
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        allMatches.add(TournamentRow(
                            id = m.optString("id", "0"),
                            team1 = m.optString("team1", ""),
                            logo1 = m.optString("logo1", ""),
                            team2 = m.optString("team2", ""),
                            logo2 = m.optString("logo2", ""),
                            score = m.optString("score", ""),
                            date  = m.optString("date", ""),
                            league = m.optString("league", "MAF"),
                            stage = m.optString("stage", ""),
                            stadium = m.optString("stadium", ""),
                            referee = m.optString("referee", ""),
                            isHeader = false
                        ))
                    }
                    runOnUiThread {
                        val dateList = createDateList(allMatches)
                        dateRecyclerView.adapter = DateAdapter(dateList) { selectedDate ->
                            filterMatches(selectedDate)
                        }
                        if (dateList.isNotEmpty()) {
                            dateList[0].isSelected = true
                            filterMatches(dateList[0].date)
                        } else {
                            recyclerView.adapter = TournamentAdapter(emptyList())
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
        val filteredByDate = allMatches.filter { it.date == date }
        val groupedList = groupMatchesByLeagueAndStage(filteredByDate)
        runOnUiThread {
            recyclerView.adapter = TournamentAdapter(groupedList)
        }
    }

    private fun groupMatchesByLeagueAndStage(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }
        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            result.add(TournamentRow(
                id = "0", team1 = "", team2 = "", score = "", logo1 = "", logo2 = "",
                league = parts[0], stage = parts.getOrNull(1) ?: "", date = "", stadium = "", referee = "", isHeader = true
            ))
            result.addAll(leagueMatches)
        }
        return result
    }
}
