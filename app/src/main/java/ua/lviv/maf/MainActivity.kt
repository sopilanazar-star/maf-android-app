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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
    private lateinit var titleHeader: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // ПОВНИЙ ЕКРАН (без годинника)
        hideSystemUI()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -1)
            setBackgroundColor(Color.WHITE)
        }

        titleHeader = TextView(this).apply {
            text = "Більше"
            textSize = 22f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            setPadding(40, 80, 40, 40)
            gravity = Gravity.CENTER
        }

        val contentFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        historyView = layoutInflater.inflate(R.layout.layout_history, null).apply {
            visibility = View.GONE
        }

        val bottomNav = BottomNavigationView(this).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            selectedItemId = R.id.nav_more
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
        mainLayout.addView(titleHeader); mainLayout.addView(contentFrame); mainLayout.addView(bottomNav)
        setContentView(mainLayout)

        updateUI("Більше", "more")
    }

    private fun updateUI(title: String, type: String) {
        titleHeader.text = title
        historyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        loadData(type)
    }

    private fun loadData(type: String) {
        if (type == "history_screen") {
            recyclerView.visibility = View.GONE
            historyView.visibility = View.VISIBLE
            loadHistory()
            return
        }

        val url = if (type == "bans_list") "https://maf.lviv.ua/wp-json/maf/v1/bans" 
                  else "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                val list = mutableListOf<TournamentRow>()
                try {
                    if (type == "bans_list") {
                        val arr = JSONArray(body)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            // Використовуємо твій TournamentRow з 4 полями
                            list.add(TournamentRow(obj.getString("name"), obj.getString("reason"), "", ""))
                        }
                    } else if (type == "more") {
                        list.add(TournamentRow("Прогнози (MAF Bet)", "Зробити прогноз", "", ""))
                        list.add(TournamentRow("Дискваліфікації", "Список банів", "", ""))
                        list.add(TournamentRow("Історія", "Архів асоціації", "", ""))
                    }
                    runOnUiThread {
                        recyclerView.adapter = TournamentAdapter(list) { item ->
                            if (item.year == "Дискваліфікації") updateUI("Список банів", "bans_list")
                            if (item.year == "Історія") updateUI("Історія", "history_screen")
                        }
                    }
                } catch (e: Exception) {}
            }
        })
    }

    private fun loadHistory() {
        client.newCall(Request.Builder().url("https://maf.lviv.ua/wp-json/maf/v1/history").build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                runOnUiThread {
                    findViewById<TextView>(R.id.historyTitle).text = json.getString("title")
                    val content = json.getString("content")
                    findViewById<TextView>(R.id.historyContent).text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
                }
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
        if (titleHeader.text != "Більше") updateUI("Більше", "more") else super.onBackPressed()
    }
}
