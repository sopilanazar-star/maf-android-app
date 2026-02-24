package ua.lviv.maf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class MenuItem(val id: Int, val title: String)

class MoreFragment : Fragment() {

    // Твій лінк на бота для зворотного зв'язку (залишаємо як було)
    private val TELEGRAM_BOT_URL = "https://t.me/MafFeedback_bot"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        val rvMoreMenu = view.findViewById<RecyclerView>(R.id.rvMoreMenu)
        val btnTelegram = view.findViewById<Button>(R.id.btnTelegram)

        btnTelegram.setOnClickListener {
            openTelegramBot()
        }

        val menuItems = listOf(
            MenuItem(1, "Прогноз на матчі"),
            MenuItem(2, "Дискваліфіковані гравці"),
            MenuItem(3, "Бомбардири (І ліга)"),
            MenuItem(4, "Бомбардири (ІІ ліга)"),
            MenuItem(5, "Бомбардири U-19 (І ліга)"),
            MenuItem(6, "Бомбардири U-19 (ІІ ліга)"),
            MenuItem(7, "Арбітри")
        )

        rvMoreMenu.layoutManager = LinearLayoutManager(context)
        rvMoreMenu.adapter = MoreMenuAdapter(menuItems) { clickedItem ->
            handleMenuClick(clickedItem)
        }

        return view
    }

    // 🔥 ПРАВКА: Оновлено метод для реакції на зміну року
    fun refreshData() {
        if (isAdded) {
            // Пошук поточного активного фрагмента в контейнері для оновлення даних за роком
            val containerId = (requireView().parent as View).id
            val currentFragment = parentFragmentManager.findFragmentById(containerId)
            
            if (currentFragment is DisqualifiedFragment) {
                currentFragment.updateYear() // Викликаємо оновлення у фрагменті дискваліфікацій
            }
            
            Toast.makeText(context, "Рік змінено на ${AppConfig.selectedYear}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTelegramBot() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_BOT_URL))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram не встановлено на цьому пристрої", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔥 ПРАВКА: Реалізовано перехід до DisqualifiedFragment
    private fun handleMenuClick(item: MenuItem) {
        val containerId = (requireView().parent as View).id
        
        when (item.id) {
            1 -> { 
                val predictionsFragment = PredictionsFragment()
                parentFragmentManager.beginTransaction()
                    .replace(containerId, predictionsFragment) 
                    .addToBackStack(null)
                    .commit()
            }
            2 -> { 
                // Відкриваємо фрагмент дискваліфікованих гравців
                val disqualifiedFragment = DisqualifiedFragment()
                parentFragmentManager.beginTransaction()
                    .replace(containerId, disqualifiedFragment) 
                    .addToBackStack(null)
                    .commit()
            }
            3 -> { Toast.makeText(context, "Відкриваємо: ${item.title} (В розробці)", Toast.LENGTH_SHORT).show() }
            4 -> { Toast.makeText(context, "Відкриваємо: ${item.title} (В розробці)", Toast.LENGTH_SHORT).show() }
            5 -> { Toast.makeText(context, "Відкриваємо: ${item.title} (В розробці)", Toast.LENGTH_SHORT).show() }
            6 -> { Toast.makeText(context, "Відкриваємо: ${item.title} (В розробці)", Toast.LENGTH_SHORT).show() }
            7 -> { Toast.makeText(context, "Відкриваємо: ${item.title} (В розробці)", Toast.LENGTH_SHORT).show() }
        }
    }

    inner class MoreMenuAdapter(
        private val items: List<MenuItem>,
        private val onClick: (MenuItem) -> Unit
    ) : RecyclerView.Adapter<MoreMenuAdapter.MenuViewHolder>() {

        inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMenuTitle: TextView = view.findViewById(R.id.tvMenuTitle)
            init {
                view.setOnClickListener { 
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onClick(items[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_more_menu, parent, false)
            return MenuViewHolder(view)
        }

        override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
            holder.tvMenuTitle.text = items[position].title
        }

        override fun getItemCount() = items.size
    }
}
