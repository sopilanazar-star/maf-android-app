package ua.lviv.maf

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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

        // Прибираємо відступи зверху та знизу для повного екрану
        hideSystemUI()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        titleHeader = TextView(this).apply {
            text = "МАФ: Турніри"
            textSize = 20f // Виправлено: sp замінено на Float
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            setPadding(40, 60, 40, 40) // Додано відступ зверху для естетики
            gravity = android.view.Gravity.CENTER
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
        }

        bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { updateUI("Новини", "news"); true }
                    R.id.nav_tables -> { updateUI("Турніри", "tables"); true }
                    R.id.nav_matches -> { updateUI("Матчі", "matches"); true }
                    // Виправлено: закоментовано nav_more, поки ти не додаси його в XML-меню
                    // R.id.nav_more -> { updateUI("Більше", "more"); true }
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
                    val tournamentList = mutableListOf<TournamentRow>()

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
}
