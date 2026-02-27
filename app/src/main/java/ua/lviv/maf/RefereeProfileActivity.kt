package ua.lviv.maf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RefereeProfileActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private val client = OkHttpClient()

    private var refereeName: String = ""
    private var selectedYear: String = "2025"
    
    // Списки для фрагментів
    var mainMatchesList = ArrayList<TournamentRow>()
    var assistantMatchesList = ArrayList<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referee_profile)

        val refId = intent.getStringExtra("REF_ID") ?: ""
        refereeName = intent.getStringExtra("REF_NAME") ?: ""
        selectedYear = intent.getStringExtra("YEAR") ?: "2025"
        
        progressBar = findViewById(R.id.progressBar)
        viewPager = findViewById(R.id.viewPagerReferee)
        tabLayout = findViewById(R.id.tabLayoutReferee)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        setupHeader()
        loadData(refId)
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Сезон $selectedYear"
        findViewById<TextView>(R.id.tvProfileName).text = refereeName
        
        findViewById<TextView>(R.id.tvProfileMatches).text = intent.getIntExtra("REF_MATCHES", 0).toString()
        findViewById<TextView>(R.id.tvProfileYellow).text = intent.getIntExtra("REF_YELLOW", 0).toString()
        findViewById<TextView>(R.id.tvProfileRed).text = intent.getIntExtra("REF_RED", 0).toString()

        // 🔥 МАГІЯ ТУТ: Замінено .circleCrop() на PlayerTopCropTransformation()
        val photoUrl = intent.getStringExtra("REF_PHOTO") ?: ""
        Glide.with(this)
            .load(photoUrl.replace("http://", "https://"))
            .transform(PlayerTopCropTransformation()) // Фокус на голові
            .placeholder(R.drawable.ic_player_placeholder)
            .into(findViewById<ImageView>(R.id.ivProfilePhoto))

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadData(refId: String) {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        val globalUrl = "https://maf.lviv.ua/wp-json/maf/v2/matches?year=$selectedYear"

        client.newCall(Request.Builder().url(globalUrl).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { finishLoadingWithError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val globalArray = JSONArray(body)
                    val allGlobalMatches = mutableListOf<TournamentRow>()

                    for (i in 0 until globalArray.length()) {
                        val m = globalArray.getJSONObject(i)
                        allGlobalMatches.add(TournamentRow(
                            id = m.optString("id"),
                            team1 = m.optString("team1"),
                            logo1 = m.optString("logo1"),
                            team2 = m.optString("team2"),
                            logo2 = m.optString("logo2"),
                            score = m.optString("score", "v"),
                            date = m.optString("date"),
                            league = m.optString("league"),
                            stage = m.optString("stage", ""),
                            referee = refereeName,
                            isHeader = false
                        ))
                    }

                    fetchRefereeIdsAndFilter(refId, allGlobalMatches)

                } catch (e: Exception) {
                    Log.e("RefereeProfile", "Global JSON Error: ${e.message}")
                    runOnUiThread { finishLoadingWithError() }
                }
            }
        })
    }

    private fun fetchRefereeIdsAndFilter(refId: String, allGlobalMatches: List<TournamentRow>) {
        val seasonId = when(selectedYear) {
            "2026" -> "29"
            "2025" -> "22"
            "2024" -> "3"
            else -> "22"
        }
        
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referee/$refId/matches?year=$selectedYear&season_id=$seasonId"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { finishLoadingWithError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    val stats = json.optJSONObject("stats")

                    val mainArray = json.optJSONArray("main_matches") ?: json.optJSONArray("matches")
                    val assArray = json.optJSONArray("assistant_matches")

                    val mainIds = extractMatchIds(mainArray)
                    val assIds = extractMatchIds(assArray)

                    mainMatchesList.clear()
                    assistantMatchesList.clear()

                    mainMatchesList.addAll(allGlobalMatches.filter { mainIds.contains(it.id) })
                    assistantMatchesList.addAll(allGlobalMatches.filter { assIds.contains(it.id) })

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        if (stats != null) {
                            findViewById<TextView>(R.id.tvProfileMatches).text = stats.optString("total", "0")
                            findViewById<TextView>(R.id.tvProfileYellow).text = stats.optString("yellow", "0")
                            findViewById<TextView>(R.id.tvProfileRed).text = stats.optString("red", "0")
                        }
                        
                        setupViewPager()
                        
                        if (mainMatchesList.isEmpty() && assistantMatchesList.isEmpty()) {
                            tvEmptyState.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RefereeProfile", "Ref JSON Error: ${e.message}")
                    runOnUiThread { finishLoadingWithError() }
                }
            }
        })
    }

    private fun extractMatchIds(array: JSONArray?): List<String> {
        val ids = mutableListOf<String>()
        if (array == null) return ids
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            ids.add(obj.optString("match_id"))
        }
        return ids
    }

    private fun finishLoadingWithError() {
        progressBar.visibility = View.GONE
        setupViewPager()
        tvEmptyState.text = "Матчів не знайдено"
        tvEmptyState.visibility = View.VISIBLE
    }

    private fun setupViewPager() {
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int) = RefereeMatchesListFragment.newInstance(position)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "ГОЛОВНИЙ" else "АСИСТЕНТ"
        }.attach()
    }
}

class RefereeMatchesListFragment : Fragment() {
    private var tabPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabPosition = arguments?.getInt("TAB_POS") ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setPadding(0, 20, 0, 100)
            clipToPadding = false
            layoutManager = LinearLayoutManager(context)
        }
        
        val activity = requireActivity() as? RefereeProfileActivity
        val currentMatches = if (tabPosition == 0) activity?.mainMatchesList else activity?.assistantMatchesList

        if (currentMatches != null && currentMatches.isNotEmpty()) {
            val grouped = mutableListOf<TournamentRow>()
            currentMatches.groupBy { "${it.league}|${it.stage}" }.forEach { (key, list) ->
                val parts = key.split("|")
                grouped.add(TournamentRow(league = parts[0], stage = parts.getOrElse(1){""}, isHeader = true))
                grouped.addAll(list)
            }
            rv.adapter = TournamentAdapter(grouped)
        }
        return rv
    }

    companion object {
        fun newInstance(position: Int) = RefereeMatchesListFragment().apply {
            arguments = Bundle().apply { putInt("TAB_POS", position) }
        }
    }
}
