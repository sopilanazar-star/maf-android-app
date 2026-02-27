package ua.lviv.maf

import android.content.Intent
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
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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
        val view = inflater.inflate(R.layout.fragment_referees, container, false) 

        // Отримуємо рік
        selectedYear = arguments?.getString("SELECTED_YEAR") ?: "2025"

        recyclerView = view.findViewById(R.id.rvReferees)
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

    // 🔥 Функція для оновлення року з глобального спінера
    fun updateYear(year: String) {
        if (selectedYear != year) {
            selectedYear = year
            tvHeaderTitle.text = "Арбітри ($selectedYear)"
            fetchReferees()
        }
    }

    private fun fetchReferees() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        // Запит іде напряму з year
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referees/full?year=$selectedYear"

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
        
        recyclerView.adapter = RefereesAdapter(data) { refereeId, name, photo, city, matches, yellow, red ->
            val intent = Intent(requireContext(), RefereeProfileActivity::class.java).apply {
                putExtra("REF_ID", refereeId)
                putExtra("REF_NAME", name)
                putExtra("REF_PHOTO", photo)
                putExtra("REF_CITY", city)
                putExtra("REF_MATCHES", matches)
                putExtra("REF_YELLOW", yellow)
                putExtra("REF_RED", red)
                putExtra("YEAR", selectedYear)
            }
            startActivity(intent)
        }
    }

    private fun showEmptyState() {
        if (!isAdded) return
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}

// 🔥 Оновлений клас Адаптера 🔥
class RefereesAdapter(
    private val items: List<JSONObject>,
    private val onRefereeClick: (String, String, String, String, Int, Int, Int) -> Unit
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
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_referee, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val stats = item.optJSONObject("stats")

        holder.tvName.text = item.optString("name", "Арбітр")
        val city = item.optString("city", "")
        holder.tvRole.text = if (city.isNotEmpty()) "м. $city" else "Арбітр МАФ"

        holder.tvMatches.text = stats?.optInt("total", 0).toString()
        holder.tvYellowCards.text = stats?.optInt("yellow", 0).toString()
        holder.tvRedCards.text = stats?.optInt("red", 0).toString()

        // 🔥 МАГІЯ ТУТ: Замінено .centerCrop() на .transform(PlayerTopCropTransformation(), CircleCrop())
        val photoUrl = item.optString("photo", "")
        Glide.with(holder.itemView.context)
            .load(photoUrl.replace("http://", "https://"))
            .transform(PlayerTopCropTransformation(), CircleCrop()) // Фокус на голові + кругла форма
            .placeholder(R.drawable.ic_player_placeholder)
            .into(holder.ivPhoto)

        holder.container.setOnClickListener { 
            onRefereeClick(
                item.optString("id"),
                item.optString("name", "Арбітр"),
                photoUrl,
                item.optString("city", ""),
                stats?.optInt("total", 0) ?: 0,
                stats?.optInt("yellow", 0) ?: 0,
                stats?.optInt("red", 0) ?: 0
            )
        }
    }

    override fun getItemCount() = items.size
}
