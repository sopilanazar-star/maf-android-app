package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.lviv.maf.api.RetrofitClient
import ua.lviv.maf.models.DisqualifiedPlayer

class DisqualifiedFragment : Fragment() {

    private lateinit var adapter: DisqualifiedAdapter
    private lateinit var tvHeaderYear: TextView
    private lateinit var rvPlayers: RecyclerView
    private var allPlayers = listOf<DisqualifiedPlayer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_disqualified, container, false)
        
        val btnBack = view.findViewById<TextView>(R.id.btnBackText)
        rvPlayers = view.findViewById(R.id.rvDisqualifiedPlayers)
        tvHeaderYear = view.findViewById(R.id.tvHeaderYear)

        // Кнопка Назад (червоний текст як на скрині)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Встановлюємо початковий рік
        tvHeaderYear.text = AppConfig.selectedYear.toString()

        rvPlayers.layoutManager = LinearLayoutManager(context)
        
        // Завантажуємо реальні дані з API
        loadPlayers()
        
        return view
    }

    // 🔥 ПРАВКА: Завантаження даних через Retrofit
    private fun loadPlayers() {
        val year = AppConfig.selectedYear
        
        RetrofitClient.instance.getDisqualifiedPlayers(year).enqueue(object : Callback<List<DisqualifiedPlayer>> {
            override fun onResponse(
                call: Call<List<DisqualifiedPlayer>>,
                response: Response<List<DisqualifiedPlayer>>
            ) {
                if (isAdded) { // Перевірка, чи фрагмент ще активний
                    if (response.isSuccessful) {
                        allPlayers = response.body() ?: emptyList()
                        updateList()
                    } else {
                        Toast.makeText(context, "Помилка завантаження: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<DisqualifiedPlayer>>, t: Throwable) {
                if (isAdded) {
                    Toast.makeText(context, "Помилка мережі: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // Метод для оновлення списку (викликається з MoreFragment при зміні року)
    fun updateYear() {
        if (::tvHeaderYear.isInitialized) {
            tvHeaderYear.text = AppConfig.selectedYear.toString()
            loadPlayers() // Перезавантажуємо дані для нового року
        }
    }

    private fun updateList() {
        adapter = DisqualifiedAdapter(allPlayers)
        rvPlayers.adapter = adapter
    }

    inner class DisqualifiedAdapter(private val items: List<DisqualifiedPlayer>) : 
        RecyclerView.Adapter<DisqualifiedAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_disqualified_player, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val player = items[position]
            
            // 🔥 ВИПРАВЛЕНО ТУТ: Додано Елвіс-оператор (?: "") для захисту від null
            val isActive = (player.status ?: "").lowercase() == "активна"

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
