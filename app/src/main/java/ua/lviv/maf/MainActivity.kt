package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var yearSpinner: Spinner? = null
    private var selectedYear: String = "2025"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        // 1. ФОН (Програмний градієнт)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )
        val root = RelativeLayout(this).apply { background = gradient }

        // 2. ВЕРХНЯ ПАНЕЛЬ (Header)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 220)
            setPadding(40, 90, 40, 20)
        }

        yearSpinner = Spinner(this).apply {
            layoutParams = RelativeLayout.LayoutParams(450, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }
        header.addView(yearSpinner)

        // 3. НИЖНЯ НАВІГАЦІЯ (Зв'язка з твоїм XML)
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            // Завантажуємо твоє меню з GitHub (res/menu/bottom_nav_menu.xml)
            inflateMenu(R.menu.bottom_nav_menu)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_news -> Toast.makeText(context, "Новини МАФ", Toast.LENGTH_SHORT).show()
                    R.id.nav_matches -> Toast.makeText(context, "Матчі за $selectedYear рік", Toast.LENGTH_SHORT).show()
                    R.id.nav_tables -> Toast.makeText(context, "Турнірні таблиці", Toast.LENGTH_SHORT).show()
                    R.id.nav_more -> Toast.makeText(context, "Додаткове меню", Toast.LENGTH_SHORT).show()
                }
                true
            }

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        // 4. КОНТЕНТНА ОБЛАСТЬ
        val content = FrameLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(-1, -1).apply {
                addRule(RelativeLayout.BELOW, header.id)
                addRule(RelativeLayout.ABOVE, nav.id)
            }
        }

        root.addView(header)
        root.addView(content)
        root.addView(nav)
        setContentView(root)

        // ЗАПУСКАЄМО ОНОВЛЕНИЙ API
        loadSeasonsFromMafApi()
    }

    private fun loadSeasonsFromMafApi() {
        thread {
            try {
                // Звертаємося до нового маршруту
                val json = URL("https://maf.lviv.ua/wp-json/maf/v1/seasons").readText()
                val array = JSONArray(json)
                val yearsList = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    yearsList.add(array.getJSONObject(i).getString("name"))
                }

                runOnUiThread {
                    yearSpinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearsList)
                    yearSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                            selectedYear = yearsList[pos].take(4)
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // Резервний варіант, щоб додаток не вилетів
                    yearSpinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("2025 рік"))
                }
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
    }
}
