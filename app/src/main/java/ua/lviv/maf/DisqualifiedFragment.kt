package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

data class DisqualifiedPlayer(
    val name: String,
    val team: String,
    val expiryDate: Calendar,
    val year: Int // Додаємо поле року для фільтрації
)

class DisqualifiedFragment : Fragment() {

    private lateinit var adapter: DisqualifiedAdapter
    private var allPlayers = listOf<DisqualifiedPlayer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_disqualified, container, false)
        
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val rvPlayers = view.findViewById<RecyclerView>(R.id.rvDisqualifiedPlayers)

        // Кнопка назад - просто повертаємось по стеку фрагментів
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Дані для прикладу (в реальності буде завантаження з API/БД)
        allPlayers = listOf(
            DisqualifiedPlayer("Олексій Смирнов", "СК Шериф", Calendar.getInstance().apply { set(2026, 2, 15) }, 2026),
            DisqualifiedPlayer("Іван Петров", "ФК Зірка", Calendar.getInstance().apply { set(2025, 10, 5) }, 2025),
            DisqualifiedPlayer("Дмитро Коваленко", "ФК Арсенал", Calendar.getInstance().apply { set(2026, 5, 20) }, 2026)
        )

        rvPlayers.layoutManager = LinearLayoutManager(context)
        
        // Фільтруємо список згідно з вибраним роком у глобальному конфігу
        val filteredList = allPlayers.filter { it.year == AppConfig.selectedYear }
        
        adapter = DisqualifiedAdapter(filteredList)
        rvPlayers.adapter = adapter
        
        return view
    }

    // Метод для оновлення списку, якщо рік зміниться, поки фрагмент відкритий
    fun updateYear() {
        val newList = allPlayers.filter { it.year == AppConfig.selectedYear }
        adapter.updateData(newList)
    }

    inner class DisqualifiedAdapter(private var items: List<DisqualifiedPlayer>) : 
        RecyclerView.Adapter<DisqualifiedAdapter.ViewHolder>() {

        fun updateData(newItems: List<DisqualifiedPlayer>) {
            items = newItems
            notifyDataSetChanged()
        }

        // ... (onCreateViewHolder та ViewHolder залишаються такими ж, як у попередній відповіді)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_disqualified_player, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val player = items[position]
            val today = Calendar.getInstance()
            val isExpired = today.after(player.expiryDate)

            holder.name.text = player.name
            holder.team.text = player.team

            if (isExpired) {
                holder.status.text = "Завершена дискваліфікація"
                holder.status.setTextColor(android.graphics.Color.GREEN)
                holder.indicator.setBackgroundColor(android.graphics.Color.GREEN)
            } else {
                val dateStr = "${player.expiryDate.get(Calendar.DAY_OF_MONTH)}.${player.expiryDate.get(Calendar.MONTH) + 1}.${player.expiryDate.get(Calendar.YEAR)}"
                holder.status.text = "Дискваліфікований до $dateStr"
                holder.status.setTextColor(android.graphics.Color.parseColor("#FF5252"))
                holder.indicator.setBackgroundColor(android.graphics.Color.RED)
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: android.widget.TextView = view.findViewById(R.id.tvPlayerName)
            val team: android.widget.TextView = view.findViewById(R.id.tvTeamName)
            val status: android.widget.TextView = view.findViewById(R.id.tvStatus)
            val indicator: View = view.findViewById(R.id.statusIndicator)
        }
    }
}
