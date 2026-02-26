package ua.lviv.maf

import android.os.Bundle
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
import java.io.Serializable

class RefereeProfileActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private val client = OkHttpClient()

    private var refereeName: String = ""
    private var selectedYear: String = "2025"
    
    private var mainMatchesList = ArrayList<TournamentRow>()
    private var assistantMatchesList = ArrayList<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referee_profile)

        val refId = intent.getStringExtra("REF_ID") ?: ""
        refereeName = intent.getStringExtra("REF_NAME") ?: ""
        selectedYear = intent.getStringExtra("YEAR") ?: "2025"
        
        progressBar = findViewById(R.id.progressBar)
        viewPager = findViewById(R.id.viewPagerReferee)
        tabLayout = findViewById(R.id.tabLayoutReferee)

        setupHeader()
        loadRefereeData(refId)
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Сезон $selectedYear"
        findViewById<TextView>(R.id.tvProfileName).text = refereeName
        
        // Початкові дані
        findViewById<TextView>(R.id.tvProfileMatches).text = intent.getIntExtra("REF_MATCHES", 0).toString()
        findViewById<TextView>(R.id.tvProfileYellow).text = intent.getIntExtra("REF_YELLOW", 0).toString()
        findViewById<TextView>(R.id.tvProfileRed).text = intent.getIntExtra("REF_RED", 0).toString()

        Glide.with(this)
            .load(intent.getStringExtra("REF_PHOTO"))
            .circleCrop()
            .placeholder(R.drawable.ic_player_placeholder)
            .into(findViewById<ImageView>(R.id.ivProfilePhoto))

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadRefereeData(refId: String) {
        progressBar.visibility = View.VISIBLE
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referee/$refId/matches?year=$selectedYear"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { progressBar.visibility = View.GONE }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    
                    // Статистика (232 жовті!)
                    val stats = json.optJSONObject("stats")
                    
                    // Парсимо матчі
                    mainMatchesList = parseMatches(json.optJSONArray("matches"))
                    assistantMatchesList = parseMatches(json.optJSONArray("assistant_matches"))

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        if (stats != null) {
                            findViewById<TextView>(R.id.tvProfileMatches).text = stats.optString("total")
                            findViewById<TextView>(R.id.tvProfileYellow).text = stats.optString("yellow")
                            findViewById<TextView>(R.id.tvProfileRed).text = stats.optString("red")
                        }
                        setupViewPager()
                    }
                } catch (e: Exception) {
                    runOnUiThread { progressBar.visibility = View.GONE }
                }
            }
        })
    }

    private fun parseMatches(array: JSONArray?): ArrayList<TournamentRow> {
        val list = ArrayList<TournamentRow>()
        if (array == null) return list
        
        for (i in 0 until array.length()) {
            val m = array.getJSONObject(i)
            val home = m.optJSONObject("home")
            val away = m.optJSONObject("away")
            
            list.add(TournamentRow(
                id = m.optString("match_id"),
                team1 = home?.optString("name") ?: "ТВА",
                logo1 = "", // В цьому JSON поки немає посилань на лого
                team2 = away?.optString("name") ?: "ТВА",
                logo2 = "",
                score = m.optString("score", "v"),
                date = m.optString("kickoff"),
                league = m.optString("competition"),
                stage = m.optString("stage", ""),
                referee = refereeName,
                isHeader = false
            ))
        }
        return list
    }

    private fun setupViewPager() {
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int) = 
                RefereeMatchesListFragment.newInstance(if (position == 0) mainMatchesList else assistantMatchesList)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "ГОЛОВНИЙ" else "АСИСТЕНТ"
        }.attach()
    }
}

// 🔥 ФРАГМЕНТ СПИСКУ (Тепер з правильним передаванням даних) 🔥
class RefereeMatchesListFragment : Fragment() {
    
    private var matches: ArrayList<TournamentRow>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        matches = arguments?.getSerializable("MATCHES_KEY") as? ArrayList<TournamentRow>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setPadding(0, 0, 0, 100)
            clipToPadding = false
            layoutManager = LinearLayoutManager(context)
        }
        
        val currentMatches = matches
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
        fun newInstance(list: ArrayList<TournamentRow>) = RefereeMatchesListFragment().apply {
            arguments = Bundle().apply {
                putSerializable("MATCHES_KEY", list)
            }
        }
    }
}
