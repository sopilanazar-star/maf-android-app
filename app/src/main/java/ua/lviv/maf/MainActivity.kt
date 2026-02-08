package ua.lviv.maf

import android.annotation.SuppressLint
import android.graphics.Color
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
    private val VERSION_JSON_URL = "https://raw.githubusercontent.com/sopilanazar-star/maf-android-app/main/version.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 1. ПОВНИЙ ЕКРАН
        hideSystemUI()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // 2. ХЕДЕР
        titleHeader = TextView(this).apply {
            text = "МАФ: Турніри"
            textSize = 22f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            setPadding(40, 70, 40, 40)
            gravity = Gravity.CENTER
            elevation = 10f
        }

        // 3. СПИСОК (RecyclerView)
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
            setPadding(0, 10, 0, 10)
            clipToPadding = false
        }

        // 4. НИЖНЯ НАВІГАЦІЯ
        bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { updateUI("Новини", "news"); true }
                    R.id.nav_tables -> { updateUI("Турніри", "tables"); true }
                    R.id.nav_matches -> { updateUI("Матчі", "matches"); true }
                    R.id.nav_more -> { updateUI("Більше", "more"); true }
                    else -> false
                }
            }
        }

        mainLayout.addView(titleHeader)
        mainLayout.addView(recyclerView)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        updateUI("Турніри", "tables")
    }

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

    private fun updateUI(title: String, type: String) {
        titleHeader.text = title
        loadDataFromApi(type)
    }

    private fun loadDataFromApi(type: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: return
                try {
                    val jsonObject = JSONObject(jsonString)
                    val displayList = mutableListOf<TournamentRow>()

                    when (type) {
                        "tables" -> {
                            val futsal = jsonObject.getJSONObject("futsal").getJSONObject("stats")
                            val years = listOf("2026", "2025")
                            for (year in years) {
                                if (futsal.has(year)) {
                                    val data = futsal.getJSONArray(year)
                                    displayList.add(TournamentRow(year, "Чемпіон: ${data.getString(0)}"))
                                }
                            }
                        }
                        "more" -> {
                            displayList.add(TournamentRow("Прогнози (MAF Bet)", "Зробити прогноз на матчі"))
                            displayList.add(TournamentRow("Дискваліфікації", "Список відсторонених гравців"))
                            displayList.add(TournamentRow("Історія", "Архів та досягнення асоціації"))
                        }
                        "bans_list" -> {
                            displayList.add(TournamentRow("Степан Гірняк (ФК Миколаїв)", "🟥 3 матчі (Червона картка)"))
                            displayList.add(TournamentRow("Олег Кульчицький (ФК Зубра)", "🟥 До 15.03.2026 (4 жовті)"))
                        }
                        "news" -> {
                            displayList.add(TournamentRow("Новина 1", "Відкриття сезону 2026 вже скоро!"))
                        }
                        "matches" -> {
                            displayList.add(TournamentRow("15.02.2026", "Миколаїв - Зубра (12:00)"))
                        }
                    }

                    runOnUiThread {
                        // ПЕРЕДАЄМО ОБРОБНИК НАТИСКАНЬ В АДАПТЕР
                        recyclerView.adapter = TournamentAdapter(displayList) { selectedItem ->
                            handleItemClick(selectedItem)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }

    private fun handleItemClick(item: TournamentRow) {
        when (item.year) {
            "Дискваліфікації" -> {
                updateUI("Список банів", "bans_list")
            }
            "Прогнози (MAF Bet)" -> {
                // Тут ми зробимо відкриття вікна для вводу рахунку
                android.widget.Toast.makeText(this, "Готуємо форму прогнозів...", android.widget.Toast.LENGTH_SHORT).show()
            }
            "Історія" -> {
                updateUI("Історія МАФ", "history")
            }
        }
    }

    override fun onBackPressed() {
        // Якщо ми в списку банів, повертаємось до розділу "Більше"
        if (titleHeader.text == "Список банів") {
            updateUI("Більше", "more")
        } else {
            super.onBackPressed()
        }
    }
}
