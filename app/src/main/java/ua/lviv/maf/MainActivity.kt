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
    
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        hideSystemUI()

        // 1. ГОЛОВНИЙ КОНТЕЙНЕР
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -1)
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        // 2. ХЕДЕР (З градієнтом UEFA)
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
            setPadding(20, 0, 20, 0)
        }

        // 4. НИЖНЯ НАВІГАЦІЯ
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
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { runOnUiThread { showLocalData(type) } }
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: ""
                try {
                    val jsonObject = JSONObject(jsonString)
                    // Тут буде логіка твого PHP
                } catch (e: Exception) { runOnUiThread { showLocalData(type) } }
            }
        })
    }

    private fun showLocalData(type: String) {
        val displayList = mutableListOf<TournamentRow>()
        when (type) {
            "more" -> {
                displayList.add(TournamentRow("Прогнози (MAF Bet)", "Зробити прогноз", "", false))
                displayList.add(TournamentRow("Дискваліфікації", "Список відсторонених", "", false))
                displayList.add(TournamentRow("Історія", "Архів асоціації", "", false))
            }
            "matches" -> {
                displayList.add(TournamentRow("Україна", "Ісландія", "2 : 0", false))
                displayList.add(TournamentRow("Португалія", "Вірменія", "9 : 1", false))
            }
            else -> displayList.add(TournamentRow("МАФ", "Дані оновлюються", "", false))
        }
        runOnUiThread {
            recyclerView.adapter = TournamentAdapter(displayList) { handleItemClick(it) }
        }
    }

    private fun handleItemClick(item: TournamentRow) {
        // Виправлено: тепер перевіряємо поле team1 замість year
        when (item.team1) {
            "Дискваліфікації" -> updateUI("Бан-лист", "bans")
            "Історія" -> updateUI("Історія МАФ", "history")
            else -> android.widget.Toast.makeText(this, item.team1, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
