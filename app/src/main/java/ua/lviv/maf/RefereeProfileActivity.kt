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
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger

class RefereeProfileActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private val client = OkHttpClient()

    private var refereeName: String = ""
    private var selectedYear: String = Calendar.getInstance().get(Calendar.YEAR).toString()
    
    val mainMatchesList = mutableListOf<TournamentRow>()
    val assistantMatchesList = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referee_profile)

        val refId = intent.getStringExtra("REF_ID") ?: ""
        refereeName = intent.getStringExtra("REF_NAME") ?: ""
        selectedYear = intent.getStringExtra("YEAR") ?: Calendar.getInstance().get(Calendar.YEAR).toString()
        
        progressBar = findViewById(R.id.progressBar)
        viewPager = findViewById(R.id.viewPagerReferee)
        tabLayout = findViewById(R.id.tabLayoutReferee)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        setupHeader()
        loadAllRefereeData(refId)
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Сезон $selectedYear"
        findViewById<TextView>(R.id.tvProfileName).text = refereeName
        
        val mainCount = intent.getIntExtra("REF_MATCHES", 0)
        val assistantCount = intent.getIntExtra("REF_ASSISTANT", 0)
        
        findViewById<TextView>(R.id.tvProfileMatches).text = (mainCount + assistantCount).toString()
        findViewById<TextView>(R.id.tvProfileYellow).text = intent.getIntExtra("REF_YELLOW", 0).toString()
        findViewById<TextView>(R.id.tvProfileRed).text = intent.getIntExtra("REF_RED", 0).toString()

        val photoUrl = intent.getStringExtra("REF_PHOTO") ?: ""
        Glide.with(this)
            .load(photoUrl.replace("http://", "https://"))
            .transform(PlayerTopCropTransformation())
            .placeholder(R.drawable.ic_player_placeholder)
            .into(findViewById(R.id.ivProfilePhoto))

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadAllRefereeData(refId: String) {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        mainMatchesList.clear()
        assistantMatchesList.clear()

        // 1. Отримуємо список турнірів року
        val url = "https://maf.lviv.ua/wp-json/maf/v2/competitions?year=$selectedYear"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { runOnUiThread { finishLoading() } }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val compsArray = JSONArray(body)
                    if (compsArray.length() == 0) {
                        runOnUiThread { finishLoading() }
                        return
                    }

                    // 2. Для кожного турніру тягнемо матчі арбітра
                    val remainingRequests = AtomicInteger(compsArray.length())
                    for (i in 0 until compsArray.length()) {
                        val seasonId = compsArray.getJSONObject(i).optString("id")
                        fetchMatchesForSeason(refId, seasonId) {
                            if (remainingRequests.decrementAndGet() == 0) {
                                runOnUiThread { finishLoading() }
                            }
                        }
                    }
                } catch (e: Exception) { runOnUiThread { finishLoading() } }
            }
        })
    }

    private fun fetchMatchesForSeason(refId: String, seasonId: String, onDone: () -> Unit) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referee/$refId/matches?year=$selectedYear&season_id=$seasonId"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onDone() }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    // Головні матчі
                    parseJsonToMatches(json.optJSONArray("main_matches") ?: json.optJSONArray("matches"), mainMatchesList)
                    // Матчі асистента
                    parseJsonToMatches(json.optJSONArray("assistant_matches"), assistantMatchesList)
                } catch (e: Exception) { }
                onDone()
            }
        })
    }

    private fun parseJsonToMatches(array: JSONArray?, targetList: MutableList<TournamentRow>) {
        if (array == null) return
        for (i in 0 until array.length()) {
            val m = array.getJSONObject(i)
            val matchId = m.optString("match_id")
            if (targetList.none { it.id == matchId }) {
                targetList.add(TournamentRow(
                    id = matchId,
                    team1 = m.optString("team1"),
                    logo1 = m.optString("logo1"),
                    team2 = m.optString("team2"),
                    logo2 = m.optString("logo2"),
                    score = m.optString("score", "v"),
                    date = m.optString("date"),
                    league = m.optString("league"),
                    stage = m.optString("stage", ""),
                    isHeader = false
                ))
            }
        }
    }

    private fun finishLoading() {
        progressBar.visibility = View.GONE
        mainMatchesList.sortByDescending { it.date }
        assistantMatchesList.sortByDescending { it.date }
        setupViewPager()
        if (mainMatchesList.isEmpty() && assistantMatchesList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
        }
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            layoutManager = LinearLayoutManager(context)
            setPadding(0, 20, 0, 100)
            clipToPadding = false
        }
        
        val pos = arguments?.getInt("TAB_POS") ?: 0
        val activity = requireActivity() as? RefereeProfileActivity
        val matches = if (pos == 0) activity?.mainMatchesList else activity?.assistantMatchesList

        if (matches != null && matches.isNotEmpty()) {
            val grouped = mutableListOf<TournamentRow>()
            matches.groupBy { "${it.league}|${it.stage}" }.forEach { (key, list) ->
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
