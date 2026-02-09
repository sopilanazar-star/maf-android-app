package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Прибираємо системні панелі
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }

        // 2. Створюємо чистий фон (градієнт)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )
        val root = RelativeLayout(this).apply {
            background = gradient
        }

        // 3. Додаємо ТІЛЬКИ нижнє меню
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            // Використовуємо файл меню з репозиторію
            inflateMenu(R.menu.bottom_nav_menu)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_news -> Toast.makeText(context, "Новини", Toast.LENGTH_SHORT).show()
                    R.id.nav_matches -> Toast.makeText(context, "Матчі", Toast.LENGTH_SHORT).show()
                    R.id.nav_tables -> Toast.makeText(context, "Таблиці", Toast.LENGTH_SHORT).show()
                    R.id.nav_more -> Toast.makeText(context, "Більше", Toast.LENGTH_SHORT).show()
                }
                true
            }

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        root.addView(nav)
        setContentView(root)
    }
}
