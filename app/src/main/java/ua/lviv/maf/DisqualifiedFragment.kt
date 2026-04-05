package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import android.widget.ImageView
import com.bumptech.glide.Glide
// import ua.lviv.maf.AdInterceptor

class DisqualifiedFragment : Fragment() {

    private var adapter: DisqualifiedAdapter? = null
    private var tvHeaderYear: TextView? = null
    private var rvPlayers: RecyclerView? = null
    private var progressBar: View? = null // Додав, якщо у тебе є ProgressBar у розмітці
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
            progressBar = null // Перевір ID у своєму XML

            btnBack?.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            // Встановлюємо заголовок без року
            tvHeaderYear?.text = "Дискваліфіковані гравці"

            context?.let {
                rvPlayers?.layoutManager = LinearLayoutManager(it)
            }
            
            loadPlayers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return view
    }

    // 🔥 ПРАВКА: Ця функція тепер публічна і викликається зовні при зміні року 🔥
    fun updateYear() {
        if (!isAdded) return // Перевірка, чи фрагмент ще "живий"

        activity?.runOnUiThread {
            tvHeaderYear?.text = "Дискваліфіковані гравці"
            // Очищаємо список перед новим завантаженням, щоб юзер бачив, що дані міняються
            adapter = DisqualifiedAdapter(emptyList())
            rvPlayers?.adapter = adapter
            
            loadPlayers() // Запускаємо завантаження для нового року
        }
    }

    private fun loadPlayers() {
        progressBar?.visibility = View.VISIBLE
        
        // Беремо актуальний рік з AppConfig
        val year = AppConfig.selectedYear.toString()
        
        RetrofitClient.instance.getDisqualifiedPlayers(year).enqueue(object : Callback<List<DisqualifiedPlayer>> {
            override fun onResponse(
                call: Call<List<DisqualifiedPlayer>>,
                response: Response<List<DisqualifiedPlayer>>
            ) {
                if (!isAdded || context == null) return
                
                progressBar?.visibility = View.GONE
                
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
                progressBar?.visibility = View.GONE
                Toast.makeText(context, "Немає зв'язку з сервером", Toast.LENGTH_SHORT).show()
            }
        })
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
                val statusValue = player.status ?: ""
                val isActive = statusValue.lowercase() == "активна"

                holder.name?.text = player.name ?: "Невідомо"
                holder.team?.text = player.teamName ?: "Без команди"

                holder.position?.visibility = View.GONE
                holder.birthDate?.visibility = View.GONE
                holder.age?.visibility = View.GONE

                val logoUrl = player.teamLogo ?: ""

                if (!logoUrl.isNullOrEmpty() && holder.teamLogo != null) {
                    Glide.with(holder.itemView.context)
                        .load(logoUrl.replace("http://", "https://"))
                        .into(holder.teamLogo)
                }
                val rawDate = player.expiryDate ?: ""
                var formattedDate = rawDate
                if (rawDate.isNotEmpty()) {
                    try {
                        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val date = parser.parse(rawDate)
                        if (date != null) formattedDate = formatter.format(date)
                    } catch (e: Exception) { }
                }

                if (isActive) {
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

                // Клік: рекламу відключено, переходимо одразу до профілю
                holder.itemView.setOnClickListener {
                    val context = holder.itemView.context

                    // AdInterceptor.execute(context) {
                    val playerId = player.playerId
                    if (!playerId.isNullOrEmpty()) {
                        val intent = Intent(context, PlayerProfileActivity::class.java)
                        intent.putExtra("PLAYER_ID", playerId)
                        intent.putExtra("PLAYER_NAME", player.name ?: "Гравець")
                        intent.putExtra("TEAM_NAME", player.teamName ?: "Команда")
                        intent.putExtra("PLAYER_POSITION", player.position ?: "")
                        intent.putExtra("PLAYER_BIRTH_DATE", player.birthDate ?: "")
                        intent.putExtra("PLAYER_AGE", player.age ?: 0)

                        val photoUrl = player.photo ?: ""
                        val logoUrl = player.teamLogo ?: ""
                        intent.putExtra("PLAYER_PHOTO", photoUrl.replace("http://", "https://"))
                        intent.putExtra("TEAM_LOGO", logoUrl.replace("http://", "https://"))

                        context.startActivity(intent)
                    }
                    // }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView? = view.findViewById(R.id.tvPlayerName)
            val team: TextView? = view.findViewById(R.id.tvTeamName)

            val teamLogo: ImageView? = view.findViewById(R.id.ivTeamLogo)

            val position: TextView? = view.findViewById(R.id.tvPosition)
            val birthDate: TextView? = view.findViewById(R.id.tvBirthDate)
            val age: TextView? = view.findViewById(R.id.tvAge)

            val status: TextView? = view.findViewById(R.id.tvStatus)
            val indicator: View? = view.findViewById(R.id.statusIndicator)
        }
    }
}
