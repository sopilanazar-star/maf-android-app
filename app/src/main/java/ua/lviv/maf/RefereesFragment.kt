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

    private fun fetchReferees() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        // URL для отримання арбітрів
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referees?year=$selectedYear"

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
                        Log.e("Referees", "JSON Parse Error: ${e.message}")
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
            openRefereeProfile(refereeId)
        }
    }

    private fun openRefereeProfile(refereeId: String) {
        if (refereeId.isEmpty()) return
        Log.d("Referees", "Клік по арбітру з ID: $refereeId")
        // Місце для переходу на картку арбітра (наступний крок)
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
        val tvCity: TextView = v.findViewById(R.id.tvRefereeCity)
        val tvMainMatches: TextView = v.findViewById(R.id.tvMainMatches)
        val tvAssistMatches: TextView = v.findViewById(R.id.tvAssistMatches)
        val container: View = v.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_referee, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val ref = item.optJSONObject("referee")

        val refId = ref?.optString("id") ?: ""
        holder.tvName.text = ref?.optString("name") ?: "Арбітр"
        
        // Поки немає міста в JSON, ставимо цю заглушку (можна прибрати, якщо не треба)
        holder.tvCity.text = "Федерація футболу" 

        // 🔥 ПРОСТИЙ І ШВИДКИЙ ПІДРАХУНОК МАТЧІВ 🔥
        val mainMatchesArray = item.optJSONArray("main_matches")
        val assistMatchesArray = item.optJSONArray("assistant_matches")

        // Якщо масив є - беремо його довжину, якщо ні - ставимо 0
        val mainCount = mainMatchesArray?.length() ?: 0
        val assistCount = assistMatchesArray?.length() ?: 0

        holder.tvMainMatches.text = mainCount.toString()
        holder.tvAssistMatches.text = assistCount.toString()

        // Фото арбітра (ідеально кругле)
        Glide.with(holder.itemView.context)
            .load(ref?.optString("photo"))
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_player_placeholder)
            .into(holder.ivPhoto)

        holder.container.setOnClickListener { 
            onRefereeClick(refId) 
        }
    }

    override fun getItemCount() = items.size
}
