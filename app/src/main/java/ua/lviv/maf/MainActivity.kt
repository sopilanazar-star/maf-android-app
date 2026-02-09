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
    
    // Посилання на твій API
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        hideSystemUI()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -1)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // ХЕДЕР
        titleHeader = TextView(this).apply {
            text = "МАФ: Турніри"
            textSize = 22f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            setPadding(40, 70, 40, 40)
            gravity = Gravity.CENTER
        }

        // СПИСОК
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        // НИЖНЯ НАВІГАЦІЯ (Виправлені ID під твоє меню)
        bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            
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

        updateUI("Турніри", "tables")
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
            override fun onFailure(call: Call, e: IOException) {
                // Якщо PHP видалено, завантажуємо локальні заглушки для тесту
                showLocalData(type)
            }
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: ""
                try {
                    val jsonObject = JSONObject(jsonString)
                    val displayList = mutableListOf<TournamentRow>()
                    // Тут логіка обробки твого JSON
                    // ... (залишаємо як було в #272)
                } catch (e: Exception) { 
                    runOnUiThread { showLocalData(type) }
                }
            }
        })
    }

    // Додав метод, щоб додаток не був порожнім без PHP
    private fun showLocalData(type: String) {
        val displayList = mutableListOf<TournamentRow>()
        when (type) {
            "more" -> {
                displayList.add(TournamentRow("Прогнози (MAF Bet)", "Зробити прогноз на матчі"))
                displayList.add(TournamentRow("Дискваліфікації", "Список відсторонених гравців"))
                displayList.add(TournamentRow("Історія", "Архів та досягнення асоціації"))
            }
            "tables" -> displayList.add(TournamentRow("2025", "Турнірні таблиці незабаром"))
            else -> displayList.add(TournamentRow("Інфо", "Дані завантажуються..."))
        }
        runOnUiThread {
            recyclerView.adapter = TournamentAdapter(displayList) { handleItemClick(it) }
        }
    }

    private fun handleItemClick(item: TournamentRow) {
        when (item.year) {
            "Дискваліфікації" -> updateUI("Список банів", "bans_list")
            "Історія" -> updateUI("Історія МАФ", "history")
            else -> android.widget.Toast.makeText(this, item.year, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
