package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
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
        
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener { 
            parentFragmentManager.popBackStack() 
        }

        fetchScorersData()
        return view
    }

    private fun fetchScorersData() {
        progressBar.visibility = View.VISIBLE
        
        val tournamentId = when (leagueType) {
            "І ліга" -> "1404"
            "ІІ ліга" -> "1405"
            "U-19 (І ліга)" -> "1406"
            "U-19 (ІІ ліга)" -> "1407"
            else -> "1406" 
        }

        val client = OkHttpClient()
        val url = "https://maf.lviv.ua/wp-json/maf/v2/top-scorers?tournament_id=$tournamentId&year=$selectedYear"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (!body.isNullOrEmpty()) {
                        val list = parseJson(body)
                        if (list.isEmpty()) showEmptyState() else setupList(list)
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
            for (i in 0 until array.length()) list.add(array.getJSONObject(i))
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    private fun setupList(data: List<JSONObject>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ScorersAdapter(data) { playerId ->
            // Тут буде виклик картки гравця
        }
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
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
        val ivPlayer: ImageView = v.findViewById(R.id.ivPlayerPhoto)
        val ivTeam: ImageView = v.findViewById(R.id.ivTeamLogoSmall)
        val container: View = v.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_top_scorer, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val p = item.optJSONObject("player")
        val t = item.optJSONObject("team")

        holder.rank.text = "${position + 1}."
        holder.name.text = p?.optString("name") ?: "Гравець"
        holder.team.text = t?.optString("name") ?: ""
        holder.matches.text = item.optString("matches", "0")
        holder.goals.text = item.optString("goals", "0")

        val color = when(position) {
            0 -> "#FFD700"
            1 -> "#C0C0C0"
            2 -> "#CD7F32"
            else -> "#00E676"
        }
        holder.rank.setTextColor(Color.parseColor(color))

        Glide.with(holder.itemView).load(p?.optString("photo")).circleCrop().placeholder(R.drawable.ic_player_placeholder).into(holder.ivPlayer)
        Glide.with(holder.itemView).load(t?.optString("logo")).placeholder(R.drawable.ic_player_placeholder).into(holder.ivTeam)

        holder.container.setOnClickListener { onPlayerClick(p?.optString("id") ?: "") }
    }

    override fun getItemCount() = items.size
}
