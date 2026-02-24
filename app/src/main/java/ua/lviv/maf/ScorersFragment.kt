package ua.lviv.maf

import android.graphics.Color
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

class ScorersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    
    private var leagueType: String = ""
    private var selectedYear: String = "2025"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scorers, container, false)
        
        leagueType = arguments?.getString("LEAGUE_TYPE") ?: ""
        selectedYear = arguments?.getString("SELECTED_YEAR") ?: "2025"

        recyclerView = view.findViewById(R.id.rvScorers)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        
        val btnBack = view.findViewById<View>(R.id.btnBack)
        btnBack?.setOnClickListener { parentFragmentManager.popBackStack() }

        fetchScorersData()

        return view
    }

    private fun fetchScorersData() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        val tournamentId = when (leagueType) {
            "І ліга" -> "1404"
            "ІІ ліга" -> "1405"
            "U-19 (І ліга)" -> "1406"
            "U-19 (ІІ ліга)" -> "1407"
            else -> "1406" 
        }

        val client = OkHttpClient()
        val apiUrl = "https://maf.lviv.ua/wp-json/maf/v2/top-scorers?tournament_id=$tournamentId&year=$selectedYear"

        val request = Request.Builder().url(apiUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (jsonData != null) {
                        val scorers = parseJson(jsonData)
                        if (scorers.isEmpty()) showEmptyState() else setupList(scorers)
                    } else {
                        showEmptyState()
                    }
                }
            }
        })
    }

    private fun parseJson(json: String): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getJSONObject(i))
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    private fun setupList(data: List<JSONObject>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ScorersAdapter(data) { playerId ->
            Log.d("Scorers", "Player ID: $playerId")
        }
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}

class ScorersAdapter(
    private val items: List<JSONObject>,
    private val onPlayerClick: (String) -> Unit
) : RecyclerView.Adapter<ScorersAdapter.ViewHolder>() {
    
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val rank: TextView = v.findViewById(R.id.tvRank)
        val name: TextView = v.findViewById(R.id.tvPlayerName)
        val team: TextView = v.findViewById(R.id.tvPlayerTeam)
        val matches: TextView = v.findViewById(R.id.tvMatches)
        val goals: TextView = v.findViewById(R.id.tvGoals)
        val ivPlayerPhoto: ImageView = v.findViewById(R.id.ivPlayerPhoto)
        val ivTeamLogoSmall: ImageView = v.findViewById(R.id.ivTeamLogoSmall)
        val container: View = v.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_top_scorer, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val playerObj = item.optJSONObject("player")
        val teamObj = item.optJSONObject("team")

        val name = playerObj?.optString("name") ?: "Гравець"
        val photoUrl = playerObj?.optString("photo") ?: ""
        val teamName = teamObj?.optString("name") ?: ""
        val teamLogoUrl = teamObj?.optString("logo") ?: ""

        holder.rank.text = "${position + 1}."
        holder.name.text = name
        holder.team.text = teamName
        holder.matches.text = item.optString("matches", "0")
        holder.goals.text = item.optString("goals", "0")

        // Кольори для ТОП-3
        when (position) {
            0 -> holder.rank.setTextColor(Color.parseColor("#FFD700"))
            1 -> holder.rank.setTextColor(Color.parseColor("#C0C0C0"))
            2 -> holder.rank.setTextColor(Color.parseColor("#CD7F32"))
            else -> holder.rank.setTextColor(Color.parseColor("#00E676"))
        }

        Glide.with(holder.itemView.context).load(photoUrl).circleCrop().placeholder(R.drawable.ic_player_placeholder).into(holder.ivPlayerPhoto)
        Glide.with(holder.itemView.context).load(teamLogoUrl).placeholder(R.drawable.ic_player_placeholder).into(holder.ivTeamLogoSmall)

        holder.container.setOnClickListener { onPlayerClick(playerObj?.optString("id") ?: "") }
    }

    override fun getItemCount() = items.size
}
