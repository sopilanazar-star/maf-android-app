package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
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
    private lateinit var tvHeaderTitle: TextView

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

        tvHeaderTitle.text = "$leagueType ($selectedYear)"

        view.findViewById<TextView>(R.id.btnBackText)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadCompetitionId()

        return view
    }

    // 1️⃣ Спочатку отримуємо список турнірів
    private fun loadCompetitionId() {

        val url = "https://maf.lviv.ua/wp-json/maf/v2/competitions?year=$selectedYear"

        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return

                val body = response.body?.string() ?: return

                try {
                    val array = JSONArray(body)
                    val compId = findCompetitionId(array)

                    if (compId.isEmpty()) {
                        activity?.runOnUiThread { showEmptyState() }
                        return
                    }

                    fetchScorers(compId)

                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread { showEmptyState() }
                }
            }
        })
    }

    // 2️⃣ Знаходимо ID турніру по назві
    private fun findCompetitionId(array: JSONArray): String {

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.getString("name")

            if (leagueType.contains("І ліга") && name.contains("І ліга")) return obj.getInt("id").toString()
            if (leagueType.contains("ІІ ліга") && name.contains("ІІ ліга")) return obj.getInt("id").toString()
            if (leagueType.contains("U-19") && name.contains("U-19")) return obj.getInt("id").toString()
        }

        return ""
    }

    // 3️⃣ Завантажуємо бомбардирів
    private fun fetchScorers(competitionId: String) {

        if (!isAdded) return

        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("maf.lviv.ua")
            .addPathSegments("wp-json/maf/v2/top-scorers")
            .addQueryParameter("competition_id", competitionId)
            .addQueryParameter("year", selectedYear)
            .build()

        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return

                val body = response.body?.string()

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (body.isNullOrEmpty()) {
                        showEmptyState()
                        return@runOnUiThread
                    }

                    try {
                        val array = JSONArray(body)
                        if (array.length() == 0) {
                            showEmptyState()
                            return@runOnUiThread
                        }

                        val list = mutableListOf<JSONObject>()
                        for (i in 0 until array.length()) list.add(array.getJSONObject(i))
                        setupList(list)

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
        recyclerView.adapter = ScorersAdapter(data) {}
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}
