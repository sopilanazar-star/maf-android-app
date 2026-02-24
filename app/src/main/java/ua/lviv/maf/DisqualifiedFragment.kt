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

        // Кнопка Назад (червоний текст як на скрині)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvHeaderYear.text = AppConfig.selectedYear.toString()

        // ТЕСТОВІ ДАНІ (згідно з твоїм JSON)
        allPlayers = listOf(
            DisqualifiedPlayer("Пелех Володимир", "\"Космос\" Колодруби", 2, "активна"),
            DisqualifiedPlayer("Тестовий Гравець", "ФК Тест", 0, "завершена")
        )

        rvPlayers.layoutManager = LinearLayoutManager(context)
        updateList()
        
        return view
    }

    fun updateYear() {
        if (::tvHeaderYear.isInitialized) {
            tvHeaderYear.text = AppConfig.selectedYear.toString()
            // Тут буде виклик завантаження даних з API для нового року
            updateList()
        }
    }

    private fun updateList() {
        adapter = DisqualifiedAdapter(allPlayers)
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
            val isActive = player.status.lowercase() == "активна"

            holder.name.text = player.name
            holder.team.text = player.teamName

            if (isActive) {
                holder.status.text = "Дискваліфікований на ${player.matches} матчі(в)"
                holder.status.setTextColor(android.graphics.Color.parseColor("#FF5252")) // Червоний
                holder.indicator.setBackgroundColor(android.graphics.Color.RED)
            } else {
                holder.status.text = "Завершена дискваліфікація"
                holder.status.setTextColor(android.graphics.Color.GREEN)
                holder.indicator.setBackgroundColor(android.graphics.Color.GREEN)
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
