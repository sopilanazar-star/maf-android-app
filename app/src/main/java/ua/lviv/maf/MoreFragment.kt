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

    // Твій лінк на бота для зворотного зв'язку
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

        // --- ДОДАЛИ НОВИЙ ПУНКТ (8) ---
        val menuItems = listOf(
            MenuItem(1, "Прогноз на матчі"),
            MenuItem(2, "Дискваліфіковані гравці"),
            MenuItem(3, "Бомбардири (І ліга)"),
            MenuItem(4, "Бомбардири (ІІ ліга)"),
            MenuItem(5, "Бомбардири U-19 (І ліга)"),
            MenuItem(6, "Бомбардири U-19 (ІІ ліга)"),
            MenuItem(7, "Арбітри"),
            MenuItem(8, "📺 Відеоогляди матчів") // <--- ОСЬ ВІН!
        )

        rvMoreMenu.layoutManager = LinearLayoutManager(context)
        rvMoreMenu.adapter = MoreMenuAdapter(menuItems) { clickedItem ->
            handleMenuClick(clickedItem)
        }

        return view
    }

    fun refreshData() {
        if (isAdded) {
            val containerId = (requireView().parent as View).id
            val currentFragment = parentFragmentManager.findFragmentById(containerId)
            
            if (currentFragment is DisqualifiedFragment) {
                currentFragment.updateYear() 
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
                val disqualifiedFragment = DisqualifiedFragment()
                parentFragmentManager.beginTransaction()
                    .replace(containerId, disqualifiedFragment) 
                    .addToBackStack(null)
                    .commit()
            }
            3 -> openScorersFragment("І ліга", containerId)
            4 -> openScorersFragment("ІІ ліга", containerId)
            5 -> openScorersFragment("U-19 (І ліга)", containerId)
            6 -> openScorersFragment("U-19 (ІІ ліга)", containerId)
            7 -> { 
                val refereesFragment = RefereesFragment()
                val bundle = Bundle()
                bundle.putString("SELECTED_YEAR", AppConfig.selectedYear.toString())
                refereesFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(containerId, refereesFragment)
                    .addToBackStack(null)
                    .commit()
            }
            // --- НОВА ДІЯ ДЛЯ ВІДЕООГЛЯДІВ ---
            8 -> {
                // Тимчасова заглушка. Пізніше замінимо на відкриття MediaFragment
                val mediaFragment = MediaFragment() // Ми його зараз створимо
                parentFragmentManager.beginTransaction()
                    .replace(containerId, mediaFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun openScorersFragment(leagueType: String, containerId: Int) {
        val fragment = ScorersFragment()
        val bundle = Bundle()
        
        bundle.putString("LEAGUE_TYPE", leagueType)
        bundle.putString("SELECTED_YEAR", AppConfig.selectedYear.toString()) 
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
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
