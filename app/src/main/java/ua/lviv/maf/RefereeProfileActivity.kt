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
import java.io.IOException

class RefereeProfileActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private val client = OkHttpClient()

    private var refereeName: String = ""
    private var selectedYear: String = "2025"
    private var mainMatches = mutableListOf<TournamentRow>()
    private var assistantMatches = mutableListOf<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referee_profile)

        refereeName = intent.getStringExtra("REF_NAME") ?: ""
        selectedYear = intent.getStringExtra("YEAR") ?: "2025"
        
        progressBar = findViewById(R.id.progressBar)
        viewPager = findViewById(R.id.viewPagerReferee)
        tabLayout = findViewById(R.id.tabLayoutReferee)

        setupHeader()
        loadData()
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Сезон $selectedYear"
        findViewById<TextView>(R.id.tvProfileName).text = refereeName
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

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        val url = "https://maf.lviv.ua/wp-json/maf/v2/matches?year=$selectedYear"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { progressBar.visibility = View.GONE }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        val row = TournamentRow(
                            id = m.optString("id"), team1 = m.optString("team1"), logo1 = m.optString("logo1"),
                            team2 = m.optString("team2"), logo2 = m.optString("logo2"), score = m.optString("score"),
                            date = m.optString("date"), league = m.optString("league"), stage = m.optString("stage"),
                            referee = m.optString("referee"), stadium = m.optString("stadium"), status = m.optString("status")
                        )

                        val mainRef = m.optString("referee", "")
                        val ass1 = m.optString("assistant_1", "")
                        val ass2 = m.optString("assistant_2", "")

                        if (mainRef.contains(refereeName, true)) mainMatches.add(row)
                        else if (ass1.contains(refereeName, true) || ass2.contains(refereeName, true)) assistantMatches.add(row)
                    }
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        setupViewPager()
                    }
                } catch (e: Exception) {
                    runOnUiThread { progressBar.visibility = View.GONE }
                }
            }
        })
    }

    private fun setupViewPager() {
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return RefereeMatchesListFragment.newInstance(if (position == 0) mainMatches else assistantMatches)
            }
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "ГОЛОВНИЙ" else "АСИСТЕНТ"
        }.attach()
    }
}

// 🔥 Цей клас обов'язково має бути тут, щоб Activity могла його викликати 🔥
class RefereeMatchesListFragment : Fragment() {
    private var matches: List<TournamentRow> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setPadding(0, 0, 0, 100)
            clipToPadding = false
        }
        
        if (matches.isNotEmpty()) {
            rv.layoutManager = LinearLayoutManager(context)
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
        fun newInstance(list: List<TournamentRow>) = RefereeMatchesListFragment().apply {
            matches = list
        }
    }
}
