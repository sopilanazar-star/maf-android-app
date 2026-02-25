package ua.lviv.maf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RefereesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvHeaderTitle: TextView
    private val client = OkHttpClient()

    private var selectedYear: String = "2025"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // УВАГА: Якщо файл розмітки екрана арбітрів називається інакше (напр. fragment_referees), зміни тут назву!
        // Я використовую R.layout.fragment_scorers, бо його структура (шапка + список) ідеально підходить.
        val view = inflater.inflate(R.layout.fragment_scorers, container, false) 

        selectedYear = arguments?.getString("SELECTED_YEAR") ?: "2025"

        recyclerView = view.findViewById(R.id.rvScorers)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)

        tvHeaderTitle.text = "Арбітри ($selectedYear)"

        view.findViewById<View>(R.id.btnBackText)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        fetchReferees()
        return view
    }

    // Перетворюємо рік у season_id для API
    private fun getSeasonId(year: String): String {
        return when (year) {
            "2025" -> "22"
            "2024" -> "21" // Впиши реальний ID сезону 2024, якщо він інший
            else -> "22"
        }
    }

    private fun fetchReferees() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        val seasonId = getSeasonId(selectedYear)
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referees/full?season_id=$seasonId"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return
                val body = response.body?.string() ?: ""
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    try {
                        val array = JSONArray(body)
                        val list = mutableListOf<JSONObject>()
                        for (i in 0 until array.length()) list.add(array.getJSONObject(i))
                        
                        if (list.isEmpty()) showEmptyState() else setupList(list)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showEmptyState()
                    }
                }
            }
        })
    }

    private fun setupList(data: List<JSONObject>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RefereesAdapter(data) { refereeId ->
            // Клік по арбітру. Поки що просто логуємо, перехід зробимо наступним кроком!
            Log.d("Referees", "Клік по арбітру: $refereeId")
        }
    }

    private fun showEmptyState() {
        if (!isAdded) return
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}

class RefereesAdapter(
    private val items: List<JSONObject>,
    private val onRefereeClick: (String) -> Unit
) : RecyclerView.Adapter<RefereesAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivPhoto: ImageView = v.findViewById(R.id.ivRefereePhoto)
        val tvName: TextView = v.findViewById(R.id.tvRefereeName)
        val tvRole: TextView = v.findViewById(R.id.tvRefereeRole)
        val tvMatches: TextView = v.findViewById(R.id.tvMatches)
        val tvYellowCards: TextView = v.findViewById(R.id.tvYellowCards)
        val tvRedCards: TextView = v.findViewById(R.id.tvRedCards)
        val container: View = v.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        // Тут ми підключаємо той самий макет item_referee.xml, який ти створив
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_referee, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val stats = item.optJSONObject("stats")

        holder.tvName.text = item.optString("name", "Арбітр")
        
        // Якщо місто вказано - пишемо його, якщо пусто - просто "Арбітр МАФ"
        val city = item.optString("city", "")
        holder.tvRole.text = if (city.isNotEmpty()) "м. $city" else "Арбітр МАФ"

        // Витягуємо статистику з блоку "stats"
        holder.tvMatches.text = stats?.optInt("total", 0).toString()
        holder.tvYellowCards.text = stats?.optInt("yellow", 0).toString()
        holder.tvRedCards.text = stats?.optInt("red", 0).toString()

        // Ідеально круглі фото, як у бомбардирів
        Glide.with(holder.itemView.context)
            .load(item.optString("photo"))
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_player_placeholder)
            .into(holder.ivPhoto)

        holder.container.setOnClickListener { 
            onRefereeClick(item.optString("id")) 
        }
    }

    override fun getItemCount() = items.size
}
