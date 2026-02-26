package ua.lviv.maf

import android.content.Intent
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
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ScorersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvHeaderTitle: TextView
    private val client = OkHttpClient()

    private var leagueType: String = ""
    private var selectedYear: String = "2025"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_scorers, container, false)

        leagueType = arguments?.getString("LEAGUE_TYPE") ?: ""
        selectedYear = arguments?.getString("SELECTED_YEAR") ?: "2025"

        recyclerView = view.findViewById(R.id.rvScorers)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)

        val cleanTitle = leagueType.replace("Бомбардири", "").replace("(", "").replace(")", "").trim()
        tvHeaderTitle.text = "Бомбардири: $cleanTitle ($selectedYear)"

        view.findViewById<View>(R.id.btnBackText)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadCompetitionId()
        return view
    }

    fun updateYear(year: String) {
        if (selectedYear != year) {
            selectedYear = year
            val cleanTitle = leagueType.replace("Бомбардири", "").replace("(", "").replace(")", "").trim()
            tvHeaderTitle.text = "Бомбардири: $cleanTitle ($selectedYear)"
            
            recyclerView.visibility = View.GONE 
            loadCompetitionId() 
        }
    }

    private fun loadCompetitionId() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        val url = "https://maf.lviv.ua/wp-json/maf/v2/competitions?year=$selectedYear"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return
                val body = response.body?.string() ?: ""
                try {
                    val array = JSONArray(body)
                    val compId = findCompetitionId(array)

                    if (compId.isEmpty()) {
                        activity?.runOnUiThread { showEmptyState() }
                    } else {
                        fetchScorers(compId)
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread { showEmptyState() }
                }
            }
        })
    }

    private fun findCompetitionId(array: JSONArray): String {
        val search = leagueType.lowercase().trim()
        val searchIsU19 = search.contains("u-19")
        val searchIsSecond = search.contains("іі ліга") || search.contains("2 ліга") || search.contains("ii ліга") || search.contains("друга")
        val searchIsFirst = (search.contains("і ліга") || search.contains("1 ліга") || search.contains("i ліга") || search.contains("перша")) && !searchIsSecond

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.optString("name", "").lowercase()

            if (name.contains("фіналь") || name.contains("плей-офф") || name.contains("кубок")) continue 

            val compIsU19 = name.contains("u-19")
            val compIsSecond = name.contains("іі ліга") || name.contains("2 ліга") || name.contains("ii ліга") || name.contains("друга")
            val compIsFirst = (name.contains("і ліга") || name.contains("1 ліга") || name.contains("i ліга") || name.contains("перша")) && !compIsSecond

            if (searchIsU19 == compIsU19 && searchIsSecond == compIsSecond && searchIsFirst == compIsFirst) {
                return obj.optString("id")
            }
        }
        return ""
    }

    private fun fetchScorers(competitionId: String) {
        if (!isAdded) return
        val url = "https://maf.lviv.ua/wp-json/maf/v2/top-scorers?competition_id=$competitionId&year=$selectedYear"

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
                        showEmptyState()
                    }
                }
            }
        })
    }

    private fun setupList(data: List<JSONObject>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ScorersAdapter(data) { playerId ->
            openPlayerProfile(playerId)
        }
    }

    private fun openPlayerProfile(playerId: String) {
        if (playerId.isEmpty()) return
        try {
            val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
            intent.putExtra("PLAYER_ID", playerId.toInt()) 
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("Scorers", "Error opening profile: ${e.message}")
        }
    }

    private fun showEmptyState() {
        if (!isAdded) return
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
        holder.team.text = t?.optString("name") ?: "Без команди"
        holder.matches.text = item.optInt("matches", 0).toString()
        holder.goals.text = item.optInt("goals", 0).toString()

        val color = when (position) {
            0 -> "#FFD700"
            1 -> "#C0C0C0"
            2 -> "#CD7F32"
            else -> "#00E676"
        }
        holder.rank.setTextColor(Color.parseColor(color))

        // 🔥 МАГІЯ ТУТ: Замінено .centerCrop().circleCrop() на нашу трансформацію
        val photoUrl = p?.optString("photo") ?: ""
        Glide.with(holder.itemView.context)
            .load(photoUrl.replace("http://", "https://"))
            .transform(PlayerTopCropTransformation(), CircleCrop()) // Обличчя вгорі + коло
            .placeholder(R.drawable.ic_player_placeholder)
            .into(holder.ivPlayer)

        // Також робимо безпечним логотип команди
        val logoUrl = t?.optString("logo") ?: ""
        Glide.with(holder.itemView.context)
            .load(logoUrl.replace("http://", "https://"))
            .placeholder(R.drawable.ic_player_placeholder)
            .into(holder.ivTeam)

        holder.container.setOnClickListener { 
            onPlayerClick(p?.optString("id") ?: "") 
        }
    }

    override fun getItemCount() = items.size
}
