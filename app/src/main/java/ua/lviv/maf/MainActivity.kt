package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val gradient = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26")))

        val root = RelativeLayout(this).apply { background = gradient }

        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 250)
            setPadding(40, 100, 40, 20)
        }

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("2025 рік"))
            layoutParams = RelativeLayout.LayoutParams(400, -2).apply { addRule(RelativeLayout.ALIGN_PARENT_LEFT) }
        }
        header.addView(spinner)

        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            inflateMenu(R.menu.bottom_nav_menu) // Використовуємо ТВОЄ меню з GitHub
            
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_news -> Toast.makeText(context, "Новини", Toast.LENGTH_SHORT).show()
                    R.id.nav_matches -> Toast.makeText(context, "Матчі", Toast.LENGTH_SHORT).show()
                    R.id.nav_tables -> Toast.makeText(context, "Таблиці", Toast.LENGTH_SHORT).show()
                    R.id.nav_more -> Toast.makeText(context, "Більше", Toast.LENGTH_SHORT).show()
                }
                true
            }

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply { addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) }
        }

        root.addView(header)
        root.addView(nav)
        setContentView(root)
    }
}
