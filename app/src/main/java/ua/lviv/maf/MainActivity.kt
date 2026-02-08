package ua.lviv.maf

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var historyView: View
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var titleHeader: TextView
    
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // ПОВЕРНУТО: Приховування годинника та навігації смартфона
        hideSystemUI()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -1)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        titleHeader = TextView(this).apply {
            text = "Більше"
            textSize = 22f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            setPadding(40, 70, 40, 40)
            gravity = Gravity.CENTER
            elevation = 10f
        }

        val contentFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        // Завантажуємо нативний макет історії
        historyView = layoutInflater.inflate(R.layout.layout_history, null).apply {
            visibility = View.GONE
        }

        bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            selectedItemId = R.id.nav_more // Стартуємо з "Більше" за твоїм бажанням
            
            setOnItemSelectedListener { item ->
                historyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                when (item.itemId) {
                    R.id.nav_home -> { updateUI("Новини", "news"); true }
                    R.id.nav_tables -> { updateUI("Турніри", "tables"); true }
                    R.id.nav_matches -> { updateUI("Матчі", "matches"); true }
                    R.id.nav_more -> { updateUI("Більше", "more"); true }
                    else -> false
                }
            }
        }

        contentFrame.addView(recyclerView)
        contentFrame.addView(historyView)
        mainLayout.addView(titleHeader)
        mainLayout.addView(contentFrame)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        updateUI("Більше", "more")
    }

    private fun updateUI(title: String, type: String) {
        titleHeader.text = title
        if (type == "history_screen") {
            recyclerView.visibility = View.GONE
            historyView.visibility = View.VISIBLE
            loadHistoryData()
        } else {
            historyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            loadDataFromApi(type)
        }
    }

    private fun loadDataFromApi(type: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: return
                val displayList = mutableListOf<TournamentRow>()
                try {
                    val jsonObject = JSONObject(jsonString)
                    when (type) {
                        "more" -> {
                            displayList.add(TournamentRow("Прогнози (MAF Bet)", "Зробити прогноз на матчі"))
                            displayList.add(TournamentRow("Дискваліфікації", "Список відсторонених гравців"))
                            displayList.add(TournamentRow("Історія", "Архів та досягнення асоціації"))
                        }
                        "bans_list" -> {
                            displayList.add(TournamentRow("Степан Гірняк (ФК Миколаїв)", "🟥 3 матчі (Червона картка)"))
                        }
                    }
                    runOnUiThread {
                        recyclerView.adapter = TournamentAdapter(displayList) { selectedItem ->
                            handleItemClick(selectedItem)
                        }
                    }
                } catch (e: Exception) {}
            }
        })
    }

    private fun loadHistoryData() {
        val client = OkHttpClient()
        // Твій новий перевірений API маршрут
        val request = Request.Builder().url("https://maf.lviv.ua/wp-json/maf/v1/history").build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: return
                try {
                    val json = JSONObject(jsonString)
                    val title = json.getString("title")
                    val content = json.getString("content")
                    runOnUiThread {
                        findViewById<TextView>(R.id.historyTitle).text = title
                        findViewById<TextView>(R.id.historyContent).text = content
                    }
                } catch (e: Exception) {}
            }
        })
    }

    private fun handleItemClick(item: TournamentRow) {
        when (item.year) {
            "Дискваліфікації" -> updateUI("Список банів", "bans_list")
            "Історія" -> updateUI("Історія МАФ", "history_screen")
            "Прогнози (MAF Bet)" -> Toast.makeText(this, "Відкриваємо MAF Bet...", Toast.LENGTH_SHORT).show()
        }
    }

    // ПОВЕРНУТО: Повний екран (без статус-бару)
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN 
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    override fun onBackPressed() {
        if (titleHeader.text == "Список банів" || titleHeader.text == "Історія МАФ") {
            updateUI("Більше", "more")
        } else {
            super.onBackPressed()
        }
    }
}
