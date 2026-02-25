package ua.lviv.maf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
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
    private lateinit var spinnerYear: Spinner
    private val client = OkHttpClient()

    private var selectedYear: String = "2025"

    // Доступні роки для спінера
    private val yearsList = listOf("2026", "2025", "2024")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Переконайся, що ти створив fragment_referees.xml з мого попереднього повідомлення
        val view = inflater.inflate(R.layout.fragment_referees, container, false) 

        selectedYear = arguments?.getString("SELECTED_YEAR") ?: "2025"

        recyclerView = view.findViewById(R.id.rvReferees)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        spinnerYear = view.findViewById(R.id.spinnerYear)

        view.findViewById<View>(R.id.btnBackText)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupSpinner()
        
        return view
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, yearsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = adapter

        val startIndex = yearsList.indexOf(selectedYear)
        if (startIndex >= 0) {
            spinnerYear.setSelection(startIndex)
        }

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newYear = yearsList[position]
                if (newYear != selectedYear || recyclerView.adapter == null) {
                    selectedYear = newYear
                    fetchReferees() 
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // 🔥 АВТОМАТИЧНИЙ ЗАПИТ: Просто передаємо year, а WordPress сам знайде season_id 🔥
    private fun fetchReferees() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        // Тепер запит виглядає так, як у всіх твоїх інших картках
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
        recyclerView.adapter = RefereesAdapter(data) { refereeId ->
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
