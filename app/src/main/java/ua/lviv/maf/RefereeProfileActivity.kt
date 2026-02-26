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
    
    // Робимо масиви доступними для фрагментів
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
        loadRefereeData(refId)
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

    private fun loadRefereeData(refId: String) {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        // 🔥 ПОВЕРНУЛИ КОНВЕРТЕР: API арбітрів на сайті досі чекає season_id!
        val seasonId = when(selectedYear) {
            "2026" -> "23"
            "2025" -> "22"
            "2024" -> "21"
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
                    
                    mainMatchesList = parseMatches(json.optJSONArray("matches"))
                    assistantMatchesList = parseMatches(json.optJSONArray("assistant_matches"))

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        
                        if (stats != null) {
                            findViewById<TextView>(R.id.tvProfileMatches).text = stats.optString("total", "0")
                            findViewById<TextView>(R.id.tvProfileYellow).text = stats.optString("yellow", "0")
                            findViewById<TextView>(R.id.tvProfileRed).text = stats.optString("red", "0")
                        }

                        // 🔥 ГАРАНТОВАНО МАЛЮЄМО ВКЛАДКИ 🔥
                        setupViewPager()

                        if (mainMatchesList.isEmpty() && assistantMatchesList.isEmpty()) {
                            tvEmptyState.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RefereeProfile", "JSON Parse Error: ${e.message}")
                    runOnUiThread { finishLoadingWithError() }
                }
            }
        })
    }

    // 🔥 БРОНЕБІЙНИЙ ПАРСЕР: Розуміє і об'єкти (Сокіл), і просто ID (19) 🔥
    private fun parseMatches(array: JSONArray?): ArrayList<TournamentRow> {
        val list = ArrayList<TournamentRow>()
        if (array == null) return list
        
        for (i in 0 until array.length()) {
            val m = array.getJSONObject(i)
            
            val homeObj = m.optJSONObject("home")
            val awayObj = m.optJSONObject("away")
            
            val team1Name = homeObj?.optString("name") ?: m.optString("home", "Команда 1")
            val team2Name = awayObj?.optString("name") ?: m.optString("away", "Команда 2")
            
            list.add(TournamentRow(
                id = m.optString("match_id"),
                team1 = team1Name,
                logo1 = homeObj?.optString("logo") ?: "",
                team2 = team2Name,
                logo2 = awayObj?.optString("logo") ?: "",
                score = m.optString("score", "v"),
                date = m.optString("kickoff"),
                league = m.optString("competition", "Матч"),
                stage = m.optString("stage", ""),
                referee = refereeName,
                isHeader = false
            ))
        }
        return list
    }

    private fun finishLoadingWithError() {
        progressBar.visibility = View.GONE
        setupViewPager() // Вкладки малюємо все одно!
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

// 🔥 ФРАГМЕНТ СПИСКУ: Тепер безпечно бере дані з Activity 🔥
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
        
        // Беремо списки напряму, щоб уникнути крашів при передачі
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
