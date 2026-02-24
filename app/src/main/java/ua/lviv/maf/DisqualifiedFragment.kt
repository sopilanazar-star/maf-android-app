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

    // Робимо змінні безпечними (nullable), щоб уникнути UninitializedPropertyAccessException
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
            // Використовуємо View? та безпечний пошук
            val btnBack: View? = view.findViewById(R.id.btnBackText)
            rvPlayers = view.findViewById(R.id.rvDisqualifiedPlayers)
            tvHeaderYear = view.findViewById(R.id.tvHeaderYear)

            btnBack?.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            tvHeaderYear?.text = AppConfig.selectedYear.toString()

            // requireContext() обгорнуто в безпечний виклик, якщо контекст ще не готовий
            context?.let {
                rvPlayers?.layoutManager = LinearLayoutManager(it)
            }
            
            loadPlayers()
        } catch (e: Exception) {
            e.printStackTrace() // Якщо щось не знайдено, просто пропустимо, але не впадемо
        }
        
        return view
    }

    private fun loadPlayers() {
        val year = AppConfig.selectedYear
        
        RetrofitClient.instance.getDisqualifiedPlayers(year).enqueue(object : Callback<List<DisqualifiedPlayer>> {
            override fun onResponse(
                call: Call<List<DisqualifiedPlayer>>,
                response: Response<List<DisqualifiedPlayer>>
            ) {
                // Захист: якщо користувач вже пішов з вкладки, нічого не робимо
                if (!isAdded || context == null) return
                
                try {
                    if (response.isSuccessful) {
                        allPlayers = response.body() ?: emptyList()
                        updateList()
                    } else {
                        Toast.makeText(context, "Помилка: ${response.code()}", Toast.LENGTH_SHORT).show()
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
                
                // 🔥 МАКСИМАЛЬНИЙ ЗАХИСТ ВІД NULL
                val statusText = player.status ?: ""
                val isActive = statusText.lowercase() == "активна"

                // Захист на випадок, якщо Gson передав null замість імені чи команди
                holder.name?.text = player.name ?: "Невідомо"
                holder.team?.text = player.teamName ?: "Невідома команда"

                if (isActive) {
                    val matchesCount = player.matches ?: 0
                    holder.status?.text = "Дискваліфікований на $matchesCount матчі(в)"
                    holder.status?.setTextColor(android.graphics.Color.parseColor("#FF5252")) // Червоний
                    holder.indicator?.setBackgroundColor(android.graphics.Color.RED)
                } else {
                    holder.status?.text = "Завершена дискваліфікація"
                    holder.status?.setTextColor(android.graphics.Color.GREEN)
                    holder.indicator?.setBackgroundColor(android.graphics.Color.GREEN)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount() = items.size

        // Усі TextView тепер з ?, щоб не падати, якщо ID в XML не збігається
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView? = view.findViewById(R.id.tvPlayerName)
            val team: TextView? = view.findViewById(R.id.tvTeamName)
            val status: TextView? = view.findViewById(R.id.tvStatus)
            val indicator: View? = view.findViewById(R.id.statusIndicator)
        }
    }
}
