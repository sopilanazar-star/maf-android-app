package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*

class DisqualifiedFragment : Fragment() {

    private var adapter: DisqualifiedAdapter? = null
    private var tvHeaderYear: TextView? = null
    private var rvPlayers: RecyclerView? = null
    private var allPlayers = listOf<DisqualifiedPlayer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_disqualified, container, false)
        
        try {
            val btnBack: View? = view.findViewById(R.id.btnBackText)
            rvPlayers = view.findViewById(R.id.rvDisqualifiedPlayers)
            tvHeaderYear = view.findViewById(R.id.tvHeaderYear)

            btnBack?.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            tvHeaderYear?.text = AppConfig.selectedYear.toString()

            context?.let {
                rvPlayers?.layoutManager = LinearLayoutManager(it)
            }
            
            loadPlayers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return view
    }

    private fun loadPlayers() {
        // Отримуємо рік як String для запиту до API
        val year = AppConfig.selectedYear.toString()
        
        RetrofitClient.instance.getDisqualifiedPlayers(year).enqueue(object : Callback<List<DisqualifiedPlayer>> {
            override fun onResponse(
                call: Call<List<DisqualifiedPlayer>>,
                response: Response<List<DisqualifiedPlayer>>
            ) {
                if (!isAdded || context == null) return
                
                try {
                    if (response.isSuccessful) {
                        allPlayers = response.body() ?: emptyList()
                        updateList()
                    } else {
                        Toast.makeText(context, "Помилка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<List<DisqualifiedPlayer>>, t: Throwable) {
                if (!isAdded || context == null) return
                Toast.makeText(context, "Немає зв'язку з сервером", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateYear() {
        if (tvHeaderYear != null) {
            tvHeaderYear?.text = AppConfig.selectedYear.toString()
            loadPlayers()
        }
    }

    private fun updateList() {
        adapter = DisqualifiedAdapter(allPlayers)
        rvPlayers?.adapter = adapter
    }

    inner class DisqualifiedAdapter(private val items: List<DisqualifiedPlayer>) : 
        RecyclerView.Adapter<DisqualifiedAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_disqualified_player, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val player = items[position]
                
                // 1. Безпечне визначення статусу
                val statusValue = player.status ?: ""
                val isActive = statusValue.lowercase() == "активна"

                // 2. Встановлення основних текстових полів
                holder.name?.text = player.name ?: "Невідомо"
                holder.team?.text = player.teamName ?: "Без команди"

                // 3. Форматування дати завершення (з yyyy-MM-dd у dd.MM.yyyy)
                val rawDate = player.expiryDate ?: ""
                var formattedDate = rawDate
                if (rawDate.isNotEmpty()) {
                    try {
                        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val date = parser.parse(rawDate)
                        if (date != null) formattedDate = formatter.format(date)
                    } catch (e: Exception) { /* залишити як є */ }
                }

                if (isActive) {
                    // Формуємо детальний статус: Причина + Кількість матчів + Дата
                    val reason = if (!player.reason.isNullOrEmpty()) "${player.reason}. " else ""
                    val matchesCount = player.matches ?: 0
                    val dateInfo = if (formattedDate.isNotEmpty()) " до $formattedDate" else ""
                    
                    holder.status?.text = "${reason}Дискваліфікований на $matchesCount матчі(в)$dateInfo"
                    holder.status?.setTextColor(Color.parseColor("#FF5252"))
                    holder.indicator?.setBackgroundColor(Color.RED)
                } else {
                    holder.status?.text = "Завершена дискваліфікація"
                    holder.status?.setTextColor(Color.GREEN)
                    holder.indicator?.setBackgroundColor(Color.GREEN)
                }

                // 4. Клік на всю картку для переходу в профіль гравця
                holder.itemView.setOnClickListener {
                    val playerId = player.playerId
                    if (!playerId.isNullOrEmpty()) {
                        val intent = Intent(holder.itemView.context, PlayerProfileActivity::class.java)
                        intent.putExtra("PLAYER_ID", playerId)
                        holder.itemView.context.startActivity(intent)
                    } else {
                        Toast.makeText(holder.itemView.context, "ID гравця відсутній", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView? = view.findViewById(R.id.tvPlayerName)
            val team: TextView? = view.findViewById(R.id.tvTeamName)
            val status: TextView? = view.findViewById(R.id.tvStatus)
            val indicator: View? = view.findViewById(R.id.statusIndicator)
        }
    }
}
