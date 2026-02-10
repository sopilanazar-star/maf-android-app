package ua.lviv.maf

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var titleHeader: TextView
    
    // Твоє посилання на API (PHP код вище активує це посилання)
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        hideSystemUI()

        // 1. ФОН (Темно-сірий UEFA)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -1)
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        // 2. ЗАГОЛОВОК
        titleHeader = TextView(this).apply {
            text = "Матчі"
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            val headerBg = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23"))
            )
            background = headerBg
            setPadding(60, 100, 40, 60)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        // 3. СПИСОК
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            setPadding(0, 10, 0, 10)
            clipToPadding = false
        }

        // 4. МЕНЮ
        bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.parseColor("#121417"))
            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
            val colors = intArrayOf(Color.parseColor("#E30613"), Color.GRAY)
            itemIconTintList = android.content.res.ColorStateList(states, colors)
            itemTextColor = android.content.res.ColorStateList(states, colors)
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_news -> { updateUI("Новини", "news"); true }
                    R.id.nav_matches -> { updateUI("Матчі", "matches"); true }
                    R.id.nav_tables -> { updateUI("Турніри", "tables"); true }
                    R.id.nav_more -> { updateUI("Більше", "more"); true }
                    else -> false
                }
            }
        }

        mainLayout.addView(titleHeader)
        mainLayout.addView(recyclerView)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        updateUI("Матчі", "matches")
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun updateUI(title: String, type: String) {
        titleHeader.text = title
        loadDataFromApi(type)
    }

    private fun loadDataFromApi(type: String) {
        // Показуємо завантаження або старі дані, поки вантажиться
        if (recyclerView.adapter == null) runOnUiThread { showLocalData(type) }

        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Якщо інтернету немає - показуємо локальні заглушки
                runOnUiThread { showLocalData(type) }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: ""
                try {
                    val displayList = mutableListOf<TournamentRow>()
                    
                    if (jsonString.startsWith("{")) {
                        val jsonObject = JSONObject(jsonString)
                        
                        // Шукаємо масив 'matches', який повертає наш новий PHP код
                        if (type == "matches" && jsonObject.has("matches")) {
                            val array = jsonObject.getJSONArray("matches")
                            for (i in 0 until array.length()) {
                                val m = array.getJSONObject(i)
                                displayList.add(TournamentRow(
                                    m.optString("team1", "Команда 1"),
                                    m.optString("team2", "Команда 2"),
                                    m.optString("score", "-:-"),
                                    false
                                ))
                            }
                        }
                    }

                    runOnUiThread {
                        if (displayList.isNotEmpty()) {
                            // УРА! Ми отримали дані з сайту
                            recyclerView.adapter = TournamentAdapter(displayList) { handleItemClick(it) }
                        } else {
                            // Якщо сайт повернув пустоту, показуємо локальні
                            showLocalData(type)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread { showLocalData(type) }
                }
            }
        })
    }

    private fun showLocalData(type: String) {
        val displayList = mutableListOf<TournamentRow>()
        when (type) {
            "matches" -> {
                // Це запасний варіант, якщо PHP ще не додано
                displayList.add(TournamentRow("ФК Миколаїв", "ФК Зубра", "2 : 1", false))
                displayList.add(TournamentRow("Очікування...", "Даних з сайту", "...", false))
            }
            "more" -> {
                displayList.add(TournamentRow("Прогнози (MAF Bet)", "Перейти", ">>", false))
                displayList.add(TournamentRow("Дискваліфікації", "Список", ">>", false))
                displayList.add(TournamentRow("Архів", "Історія", ">>", false))
            }
            "tables" -> {
                displayList.add(TournamentRow("Прем'єр Ліга", "Сезон 25/26", "Таблиця", false))
            }
            else -> displayList.add(TournamentRow("МАФ", "Завантаження...", "...", false))
        }
        recyclerView.adapter = TournamentAdapter(displayList) { handleItemClick(it) }
    }

    private fun handleItemClick(item: TournamentRow) {
        android.widget.Toast.makeText(this, item.team1, android.widget.Toast.LENGTH_SHORT).show()
    }
}
