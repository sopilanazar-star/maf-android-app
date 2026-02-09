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

    // Використовуємо lateinits обережно
    private var contentFrame: FrameLayout? = null
    private var yearSpinner: Spinner? = null
    private var selectedYear: String = "2025"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. ПОВНИЙ ЕКРАН
        hideSystemUI()

        // 2. СТВОРЮЄМО ДИЗАЙН ПРОГРАМНО
        val root = RelativeLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
            )
        }

        // ВЕРХНЯ ПАНЕЛЬ
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 220)
            setPadding(40, 90, 40, 20)
        }

        yearSpinner = Spinner(this).apply {
            layoutParams = RelativeLayout.LayoutParams(400, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        val authBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_myplaces)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(110, 110).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        header.addView(yearSpinner)
        header.addView(authBtn)

        // НИЖНЯ НАВІГАЦІЯ
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            menu.clear()
            menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_gallery)
            menu.add(0, 2, 0, "Матчі").setIcon(android.R.drawable.ic_menu_today)
            menu.add(0, 3, 0, "Таблиці").setIcon(android.R.drawable.ic_menu_sort_by_size)
            menu.add(0, 4, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        // КОНТЕНТНА ОБЛАСТЬ
        contentFrame = FrameLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(-1, -1).apply {
                addRule(RelativeLayout.BELOW, header.id)
                addRule(RelativeLayout.ABOVE, nav.id)
            }
        }

        root.addView(header)
        root.addView(contentFrame)
        root.addView(nav)

        setContentView(root)

        // 3. БЕЗПЕЧНЕ ЗАВАНТАЖЕННЯ РОКІВ
        loadSeasonsFromApi()
    }

    private fun loadSeasonsFromApi() {
        thread {
            try {
                // Використовуємо твій оновлений PHP API
                val json = URL("https://maf.lviv.ua/wp-json/maf/v1/seasons").readText()
                val array = JSONArray(json)
                val years = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    years.add(array.getJSONObject(i).getString("name"))
                }

                runOnUiThread {
                    yearSpinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // Якщо API не відповіло, ставимо дефолтні значення, щоб не впало
                    val defaultYears = arrayOf("2025 рік", "2024 рік")
                    yearSpinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultYears)
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
