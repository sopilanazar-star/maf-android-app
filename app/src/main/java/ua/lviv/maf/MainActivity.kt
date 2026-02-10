package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowInsets
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
    private lateinit var titleHeader: TextView
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Приховуємо статус-бар для краси
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }

        // Хедер
        titleHeader = TextView(this).apply {
            text = "Матчі"
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#450000"), Color.parseColor("#1A1D23")))
            setPadding(60, 100, 40, 60)
        }

        // Список
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        // Навігація
        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.parseColor("#121417"))
            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
            val colors = intArrayOf(Color.parseColor("#E30613"), Color.GRAY)
            itemIconTintList = android.content.res.ColorStateList(states, colors)
            itemTextColor = android.content.res.ColorStateList(states, colors)
            
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_matches -> { updateUI("Матчі", "matches"); true }
                    R.id.nav_tables -> { updateUI("Турніри", "tables"); true }
                    R.id.nav_more -> { updateUI("Більше", "more"); true }
                    else -> true
                }
            }
        }

        mainLayout.addView(titleHeader)
        mainLayout.addView(recyclerView)
        mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        updateUI("Матчі", "matches")
    }

    private fun updateUI(title: String, type: String) {
        titleHeader.text = title
        if (type == "matches") loadFromApi() else showStaticData(type)
    }

    private fun loadFromApi() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showStaticData("error") }
            }
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: ""
                try {
                    val list = mutableListOf<TournamentRow>()
                    val obj = JSONObject(json)
                    val array = obj.getJSONArray("matches")
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        list.add(TournamentRow(m.getString("team1"), m.getString("team2"), m.getString("score"), false))
                    }
                    runOnUiThread { recyclerView.adapter = TournamentAdapter(list) {} }
                } catch (e: Exception) {
                    runOnUiThread { showStaticData("error") }
                }
            }
        })
    }

    private fun showStaticData(type: String) {
        val list = mutableListOf<TournamentRow>()
        when (type) {
            "tables" -> list.add(TournamentRow("Вища ліга", "2025", "Таблиця", false))
            "more" -> list.add(TournamentRow("Дискваліфікації", "Список", ">>", false))
            else -> list.add(TournamentRow("Помилка", "Дані відсутні", "!", false))
        }
        runOnUiThread { recyclerView.adapter = TournamentAdapter(list) {} }
    }
}
