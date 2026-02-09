package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var contentFrame: FrameLayout
    private lateinit var yearSpinner: Spinner
    private var selectedYear: String = "2025"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        // 1. ГОЛОВНИЙ ФОН
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )
        val root = RelativeLayout(this).apply { background = gradient }

        // 2. ВЕРХНЯ ПАНЕЛЬ (HEADER)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 220)
            setPadding(40, 90, 40, 20)
        }

        // Спінер (Роки)
        yearSpinner = Spinner(this).apply {
            layoutParams = RelativeLayout.LayoutParams(450, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Кнопка Авторизації (Профіль)
        val authBtn = ImageButton(this).apply {
            // Використовуємо системну іконку акаунта
            setImageResource(android.R.drawable.ic_menu_myplaces) 
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(110, 110).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { showLoginDialog() }
        }

        header.addView(yearSpinner)
        header.addView(authBtn)

        // 3. НИЖНЯ НАВІГАЦІЯ (СПОРТИВНИЙ СТИЛЬ)
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            // Оновлюємо іконки та назви
            menu.clear()
            // 1. Новини (М'яч/Спорт)
            menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_gallery) 
            // 2. Матчі (Календар)
            menu.add(0, 2, 0, "Матчі").setIcon(android.R.drawable.ic_menu_today)
            // 3. Таблиці (Рейтинг)
            menu.add(0, 3, 0, "Таблиці").setIcon(android.R.drawable.ic_menu_sort_by_size)
            // 4. Більше (Налаштування)
            menu.add(0, 4, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    1 -> Toast.makeText(context, "Новини МАФ", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(context, "Розклад за $selectedYear рік", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(context, "Турнірні таблиці", Toast.LENGTH_SHORT).show()
                    4 -> Toast.makeText(context, "Меню керування", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }

        // 4. КОНТЕНТ
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

        // ЗАВАНТАЖУЄМО РОКИ З САЙТУ
        loadSeasons()
    }

    // ФУНКЦІЯ АВТОРИЗАЦІЇ
    private fun showLoginDialog() {
        val builder = AlertDialog.Builder(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
            val email = EditText(this@MainActivity).apply { hint = "Логін (Email)" }
            val pass = EditText(this@MainActivity).apply { 
                hint = "Пароль"
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            addView(email)
            addView(pass)
        }

        builder.setTitle("Авторизація МАФ")
            .setView(layout)
            .setPositiveButton("Увійти") { _, _ ->
                Toast.makeText(this, "Перевірка даних...", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // ЗАВАНТАЖЕННЯ РОКІВ З PHP API
    private fun loadSeasons() {
        thread {
            try {
                val json = URL("https://maf.lviv.ua/wp-json/maf/v1/seasons").readText()
                val array = JSONArray(json)
                val yearsList = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    yearsList.add(array.getJSONObject(i).getString("name"))
                }

                runOnUiThread {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearsList)
                    yearSpinner.adapter = adapter
                    yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                            selectedYear = yearsList[pos].take(4) // Беремо цифри року
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // Якщо сайт лежить, ставимо дефолт
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("2025 рік"))
                    yearSpinner.adapter = adapter
                }
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }
}
