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

class RefereeProfileActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private val client = OkHttpClient()

    private var refereeName: String = ""
    // Беремо поточний рік як базу, якщо раптом нічого не прийде
    private var selectedYear: String = Calendar.getInstance().get(Calendar.YEAR).toString()
    
    var mainMatchesList = ArrayList<TournamentRow>()
    var assistantMatchesList = ArrayList<TournamentRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referee_profile)

        // Отримуємо дані з Intent (як ти і робив у фрагменті з arguments)
        val refId = intent.getStringExtra("REF_ID") ?: ""
        refereeName = intent.getStringExtra("REF_NAME") ?: ""
        selectedYear = intent.getStringExtra("YEAR") ?: Calendar.getInstance().get(Calendar.YEAR).toString()
        
        progressBar = findViewById(R.id.progressBar)
        viewPager = findViewById(R.id.viewPagerReferee)
        tabLayout = findViewById(R.id.tabLayoutReferee)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        setupHeader()
        // Автоматично шукаємо сезон і вантажимо матчі
        fetchAutoSeasonAndLoad(refId)
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Сезон $selectedYear"
        findViewById<TextView>(R.id.tvProfileName).text = refereeName
        
        findViewById<TextView>(R.id.tvProfileMatches).text = intent.getIntExtra("REF_MATCHES", 0).toString()
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

    // Робимо як у фрагменті: тягнемо дані динамічно за вибраним роком
    private fun fetchAutoSeasonAndLoad(refId: String) {
        progressBar.visibility = View.VISIBLE
        
        // Крок 1: Отримуємо список турнірів для цього року, щоб витягнути ID сезону
        val urlComps = "https://maf.lviv.ua/wp-json/maf/v2/competitions?year=$selectedYear"

        client.newCall(Request.Builder().url(urlComps).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { finishLoadingWithError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val comps = JSONArray(body)
                    if (comps.length() > 0) {
                        // Автоматично беремо ID першого турніру сезону
                        val seasonId = comps.getJSONObject(0).optString("id")
                        loadMatchesData(refId, seasonId)
                    } else {
                        runOnUiThread { finishLoadingWithError() }
                    }
                } catch (e: Exception) {
                    runOnUiThread { finishLoadingWithError() }
                }
            }
        })
    }

    private fun loadMatchesData(refId: String, seasonId: String) {
        // Крок 2: Вантажимо всі матчі року для фільтрації
        val urlAllMatches = "https://maf.lviv.ua/wp-json/maf/v2/matches?year=$selectedYear"

        client.newCall(Request.Builder().url(urlAllMatches).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { finishLoadingWithError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val array = JSONArray(body)
                    val allMatches = mutableListOf<TournamentRow>()
                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        allMatches.add(TournamentRow(
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
                    // Крок 3: Отримуємо матчі конкретного арбітра за знайденим seasonId
                    fetchRefereeMatches(refId, seasonId, allMatches)
                } catch (e: Exception) {
                    runOnUiThread { finishLoadingWithError() }
                }
            }
        })
    }

    private fun fetchRefereeMatches(refId: String, seasonId: String, allMatches: List<TournamentRow>) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referee/$refId/matches?year=$selectedYear&season_id=$seasonId"

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { finishLoadingWithError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    val mainIds = extractIds(json.optJSONArray("main_matches") ?: json.optJSONArray("matches"))
                    val assIds = extractIds(json.optJSONArray("assistant_matches"))

                    mainMatchesList.clear()
                    assistantMatchesList.clear()
                    mainMatchesList.addAll(allMatches.filter { mainIds.contains(it.id) })
                    assistantMatchesList.addAll(allMatches.filter { assIds.contains(it.id) })

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        setupViewPager()
                        if (mainMatchesList.isEmpty() && assistantMatchesList.isEmpty()) {
                            tvEmptyState.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread { finishLoadingWithError() }
                }
            }
        })
    }

    private fun extractIds(array: JSONArray?): List<String> {
        val list = mutableListOf<String>()
        if (array == null) return list
        for (i in 0 until array.length()) list.add(array.getJSONObject(i).optString("match_id"))
        return list
    }

    private fun finishLoadingWithError() {
        progressBar.visibility = View.GONE
        setupViewPager()
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

// Фрагмент списку матчів
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
