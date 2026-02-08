package ua.lviv.maf

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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

        // Головний контейнер
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // Кастомний хедер замість логотипу сайту
        titleHeader = TextView(this).apply {
            text = "МАФ: Турніри"
            textSize = 20sp
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            setPadding(40, 40, 40, 40)
            gravity = android.view.Gravity.CENTER
        }

        // Основний список контенту
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
        }

        // Нижня навігація
        bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            itemIconTintList = null // Щоб іконки були оригінальними
            
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        updateUI("Новини", "news")
                        true
                    }
                    R.id.nav_tables -> {
                        updateUI("Турніри", "tables")
                        true
                    }
                    R.id.nav_matches -> {
                        updateUI("Матчі", "matches")
                        true
                    }
                    R.id.nav_more -> {
                        updateUI("Більше", "more")
                        true
                    }
                    else -> false
                }
            }
        }

        mainLayout.addView(titleHeader)
        mainLayout.addView(recyclerView)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        // Початкове завантаження
        updateUI("Турніри", "tables")
        checkUpdate()
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
                    val tournamentList = mutableListOf<TournamentRow>()

                    // Обробка даних залежно від вкладки
                    if (type == "tables") {
                        val futsal = jsonObject.getJSONObject("futsal").getJSONObject("stats")
                        val years = listOf("2026", "2025")
                        for (year in years) {
                            if (futsal.has(year)) {
                                val data = futsal.getJSONArray(year)
                                tournamentList.add(TournamentRow(year, data.getString(0)))
                            }
                        }
                    }

                    runOnUiThread {
                        recyclerView.adapter = TournamentAdapter(tournamentList)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }

    // Твої методи сповіщень та перевірки версії
    private fun checkUpdate() {
        val client = OkHttpClient()
        val request = Request.Builder().url(VERSION_JSON_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    try {
                        val json = JSONObject(jsonString)
                        if (json.getInt("new_version_code") > BuildConfig.VERSION_CODE) {
                            // Логіка показу діалогу оновлення
                        }
                    } catch (e: Exception) {}
                }
            }
        })
    }
}
