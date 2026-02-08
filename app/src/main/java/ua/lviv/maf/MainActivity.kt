package ua.lviv.maf

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyView: View
    private lateinit var headerTitle: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        hideSystemUI()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -1)
            setBackgroundColor(Color.parseColor("#F5F5F5")) // Світло-сірий фон
        }

        // --- ВЕРХНЯ ПАНЕЛЬ (Як на скриншоті) ---
        val topBar = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 160)
            setBackgroundColor(Color.parseColor("#007c3d")) // Зелений МАФ
            setPadding(30, 40, 30, 20)
        }

        // Ліва частина: "Турніри"
        headerTitle = TextView(this).apply {
            text = "Більше"
            textSize = 24f
            setTextColor(Color.WHITE)
            // typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = RelativeLayout.LayoutParams(-2, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Права частина: Іконка профілю/Вхід
        val profileIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_my_calendar) // Тимчасова іконка профілю
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(80, 80).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { Toast.makeText(context, "Вхід у кабінет скоро...", Toast.LENGTH_SHORT).show() }
        }

        topBar.addView(headerTitle)
        topBar.addView(profileIcon)

        // --- ОСНОВНИЙ КОНТЕНТ ---
        val contentFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        recyclerView = RecyclerView(this)

        historyView = layoutInflater.inflate(R.layout.layout_history, null).apply {
            visibility = View.GONE
        }

        // --- НИЖНЯ НАВІГАЦІЯ ---
        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            selectedItemId = R.id.nav_more
            setBackgroundColor(Color.WHITE)
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_more -> { updateUI("Більше", "more"); true }
                    R.id.nav_home -> { updateUI("Новини", "news"); true }
                    R.id.nav_tables -> { updateUI("Турніри", "tables"); true }
                    else -> false
                }
            }
        }

        contentFrame.addView(recyclerView)
        contentFrame.addView(historyView)

        mainLayout.addView(topBar)
        mainLayout.addView(contentFrame)
        mainLayout.addView(bottomNav)

        setContentView(mainLayout)

        updateUI("Більше", "more")
    }

    private fun updateUI(title: String, type: String) {
        headerTitle.text = title
        
        // Скидаємо видимість
        historyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        if (type == "history_screen") {
            recyclerView.visibility = View.GONE
            historyView.visibility = View.VISIBLE
            loadHistory()
        } else {
            loadData(type)
        }
    }

    private fun loadData(type: String) {
        // Налаштування сітки або списку
        if (type == "more") {
            // СІТКА ДЛЯ МЕНЮ (2 колонки)
            recyclerView.layoutManager = GridLayoutManager(this, 2)
        } else {
            // СПИСОК ДЛЯ ІНШОГО
            recyclerView.layoutManager = LinearLayoutManager(this)
        }

        val url = if (type == "bans_list") "https://maf.lviv.ua/wp-json/maf/v1/bans" 
                  else "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                 runOnUiThread { Toast.makeText(this@MainActivity, "Помилка з'єднання", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                val list = mutableListOf<TournamentRow>()
                try {
                    if (type == "bans_list") {
                        val arr = JSONArray(body)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            // КЛЮЧІ МАЮТЬ БУТИ ЛАТИНИЦЕЮ (name, reason) ЯК У PHP
                            list.add(TournamentRow(obj.getString("name"), obj.getString("reason"), "", ""))
                        }
                    } else if (type == "more") {
                        list.add(TournamentRow("Прогнози (MAF Bet)", "Зробити прогноз", "", ""))
                        list.add(TournamentRow("Дискваліфікації", "Список банів", "", ""))
                        list.add(TournamentRow("Історія", "Архів асоціації", "", ""))
                    }
                    runOnUiThread {
                        recyclerView.adapter = TournamentAdapter(list) { item ->
                            if (item.year.contains("Дискваліфікації")) updateUI("Список банів", "bans_list")
                            if (item.year.contains("Історія")) updateUI("Історія МАФ", "history_screen")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun loadHistory() {
        client.newCall(Request.Builder().url("https://maf.lviv.ua/wp-json/maf/v1/history").build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    val t = json.optString("title", "Історія")
                    val c = json.optString("content", "Немає даних")
                    runOnUiThread {
                        val hTitle = historyView.findViewById<TextView>(R.id.historyTitle)
                        val hContent = historyView.findViewById<TextView>(R.id.historyContent)
                        if (hTitle != null && hContent != null) {
                            hTitle.text = t
                            hContent.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 
                                Html.fromHtml(c, Html.FROM_HTML_MODE_COMPACT) 
                                else @Suppress("DEPRECATION") Html.fromHtml(c)
                        }
                    }
                } catch (e: Exception) {}
            }
        })
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    override fun onBackPressed() {
        if (headerTitle.text != "Більше") updateUI("Більше", "more") else super.onBackPressed()
    }
}
