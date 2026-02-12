package ua.lviv.maf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
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
    private lateinit var seasonSpinner: Spinner // Спінер для вибору року
    
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"
    private var currentYear = "2025" // Рік за замовчуванням
    
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

        // --- НОВИЙ HEADER ЗІ СПІНЕРОМ ---
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23")))
            setPadding(60, 100, 40, 60)
        }

        titleHeader = TextView(this).apply {
            text = "Матчі"
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Створюємо та налаштовуємо Spinner
        seasonSpinner = Spinner(this).apply {
            val seasons = arrayOf("2026", "2025", "2024")
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, seasons)
            this.adapter = adapter
            setSelection(1) // Позиція "2025"
            
            // Налаштовуємо відступи, щоб стрілочка не тиснула на текст
            setPadding(30, 0, 0, 0)
        }

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = parent?.getItemAtPosition(position).toString()
                if (currentYear != selectedYear) {
                    currentYear = selectedYear
                    loadFromApi(currentYear) // Перезавантажуємо дані для обраного року
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        headerLayout.addView(titleHeader)
        headerLayout.addView(seasonSpinner)

        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
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

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_matches -> {
                        headerLayout.visibility = View.VISIBLE
                        contentLayout.visibility = View.VISIBLE
                    }
                    else -> {
                        // Для інших вкладок можна ховати спінер або весь хедер
                        headerLayout.visibility = View.VISIBLE 
                        titleHeader.text = item.title
                        contentLayout.visibility = View.GONE
                    }
                }
                true
            }
            selectedItemId = R.id.nav_matches
        }

        mainLayout.addView(headerLayout)
        mainLayout.addView(contentLayout)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        loadFromApi(currentYear)
    }

    private fun loadFromApi(year: String) {
        val client = OkHttpClient()
        // Додаємо параметр року до URL запиту
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
                            // Очищуємо список, якщо на обраний рік матчів немає
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
