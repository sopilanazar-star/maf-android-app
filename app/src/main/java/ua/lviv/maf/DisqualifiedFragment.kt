package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ua.lviv.maf.models.DisqualifiedPlayer
import java.text.SimpleDateFormat
import java.util.*

class DisqualifiedFragment : Fragment() {

    private lateinit var adapter: DisqualifiedAdapter
    private lateinit var tvHeaderYear: TextView
    private var allPlayers = listOf<DisqualifiedPlayer>() 

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_disqualified, container, false)
        
        val btnBack = view.findViewById<TextView>(R.id.btnBackText)
        val rvPlayers = view.findViewById<RecyclerView>(R.id.rvDisqualifiedPlayers)
        tvHeaderYear = view.findViewById(R.id.tvHeaderYear)

        // Кнопка Назад
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Встановлюємо поточний рік у заголовок
        tvHeaderYear.text = AppConfig.selectedYear.toString()

        // Твоя база даних гравців оновлена під нову модель
        allPlayers = listOf(
            DisqualifiedPlayer("1", "Олексій Смирнов", "СК Шериф", "Червона картка", "2026-06-10", 2026),
            DisqualifiedPlayer("2", "Іван Петров", "ФК Зірка", "Перебір жовтих", "2025-10-05", 2025)
        )

        rvPlayers.layoutManager = LinearLayoutManager(context)
        updateList() // Первинне завантаження
        
        return view
    }

    // Метод для оновлення списку (викликається з MoreFragment при зміні року)
    fun updateYear() {
        if (::tvHeaderYear.isInitialized) {
            tvHeaderYear.text = AppConfig.selectedYear.toString()
            updateList()
        }
    }

    private fun updateList() {
        val filteredList = allPlayers.filter { it.year == AppConfig.selectedYear }
        adapter = DisqualifiedAdapter(filteredList)
        val rvPlayers = view?.findViewById<RecyclerView>(R.id.rvDisqualifiedPlayers)
        rvPlayers?.adapter = adapter
    }

    inner class DisqualifiedAdapter(private val items: List<DisqualifiedPlayer>) : 
        RecyclerView.Adapter<DisqualifiedAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_disqualified_player, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val player = items[position]
            
            // Парсимо дату з формату "yyyy-MM-dd"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            
            val expiryDate = try {
                inputFormat.parse(player.expiryDate)
            } catch (e: Exception) {
                null
            }

            val today = Calendar.getInstance().time
            val isExpired = expiryDate?.before(today) ?: false

            holder.name.text = player.name
            holder.team.text = player.teamName // Використовуємо teamName з моделі

            if (isExpired) {
                holder.status.text = "Завершена дискваліфікація"
                holder.status.setTextColor(android.graphics.Color.GREEN)
                holder.indicator.setBackgroundColor(android.graphics.Color.GREEN)
            } else {
                val dateStr = expiryDate?.let { outputFormat.format(it) } ?: player.expiryDate
                holder.status.text = "Дискваліфікований до $dateStr"
                holder.status.setTextColor(android.graphics.Color.parseColor("#FF5252"))
                holder.indicator.setBackgroundColor(android.graphics.Color.RED)
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.tvPlayerName)
            val team: TextView = view.findViewById(R.id.tvTeamName)
            val status: TextView = view.findViewById(R.id.tvStatus)
            val indicator: View = view.findViewById(R.id.statusIndicator)
        }
    }
}
