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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. ПРИБИРАЄМО СИСТЕМНІ ПАНЕЛІ (Fullscreen)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }

        // 2. ФОН-ГРАДІЄНТ (Програмно)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )
        val root = RelativeLayout(this).apply { background = gradient }

        // 3. ВЕРХНЯ ПАНЕЛЬ (Header)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 220)
            setPadding(40, 90, 40, 20)
        }

        val spinner = Spinner(this).apply {
            val items = arrayOf("2025 рік", "2024 рік", "2023 рік")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
            layoutParams = RelativeLayout.LayoutParams(400, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        val authBtn = ImageButton(this).apply {
            // Системна іконка "Профіль"
            setImageResource(android.R.drawable.ic_menu_myplaces)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(110, 110).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        header.addView(spinner)
        header.addView(authBtn)

        // 4. НИЖНЯ НАВІГАЦІЯ (Тільки системні іконки!)
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            menu.clear()
            // Використовуємо нативні іконки Android (android.R.drawable)
            menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_gallery)
            menu.add(0, 2, 0, "Матчі").setIcon(android.R.drawable.ic_menu_today)
            menu.add(0, 3, 0, "Таблиці").setIcon(android.R.drawable.ic_menu_sort_by_size)
            menu.add(0, 4, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

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
    }
}
