package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class StandingFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StandingAdapter

    private var competitions = mutableListOf<Competition>()

    private val COMPS_URL = "https://maf.lviv.ua/wp-json/maf/v2/competitions"
    private val STANDING_URL = "https://maf.lviv.ua/wp-json/maf/v2/standing"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_standing, container, false)

        tabLayout = view.findViewById(R.id.tabLayoutCompetitions)
        recyclerView = view.findViewById(R.id.rvStanding)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = StandingAdapter(emptyList())
        recyclerView.adapter = adapter

        loadCompetitions()

        return view
    }

    // ===============================
    // 1. ЗАВАНТАЖЕННЯ СПИСКУ ЛІГ
    // ===============================
    private fun loadCompetitions() {
        val client = OkHttpClient.Builder()
    .cache(null)
    .build()
        // ПРАВКА: Додаємо передачу глобального року до запиту турнірів
        val url = "$COMPS_URL?year=${AppConfig.selectedYear}"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка зв'язку (Турніри)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Відрізаємо можливі невидимі пробіли від WordPress
                val json = response.body?.string()?.trim() ?: "" 
                
                try {
                    val array = JSONArray(json)
                    competitions.clear()

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        
                        // 🔥 БЕЗПЕЧНИЙ ПАРСИНГ: шукаємо id, якщо немає - term_id
                        val compId = obj.optString("id", obj.optString("term_id", ""))
                        // Шукаємо title, якщо немає - name
                        val compTitle = obj.optString("title", obj.optString("name", "Турнір"))
                        
                        if (compId.isNotEmpty()) {
                            competitions.add(Competition(compId, compTitle))
                        }
                    }

                    activity?.runOnUiThread { updateTabs() }

                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Помилка обробки турнірів", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updateTabs() {
        tabLayout.removeAllTabs()
        tabLayout.clearOnTabSelectedListeners() // Щоб не дублювати запити

        for (comp in competitions) {
            tabLayout.addTab(tabLayout.newTab().setText(comp.title))
        }

        setupTabListener()

        if (competitions.isNotEmpty()) {
            loadStanding(competitions[0].id)
        }
    }

    private fun setupTabListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val pos = tab?.position ?: 0
                if (pos < competitions.size) {
                    loadStanding(competitions[pos].id)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // ===============================
    // 2. ЗАВАНТАЖЕННЯ ТАБЛИЦІ (JSON)
    // ===============================
    private fun loadStanding(compId: String) {
        val client = OkHttpClient()
        // ПРАВКА: Додаємо передачу глобального року до запиту таблиці турніру
        val url = "$STANDING_URL?competition_id=$compId&year=${AppConfig.selectedYear}"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка зв'язку (Таблиця)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()?.trim() ?: ""

                try {
                    val array = JSONArray(json)
                    val list = mutableListOf<StandingRow>()

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)

                        val formArray = obj.optJSONArray("form")
                        val formList = mutableListOf<String>()
                        if (formArray != null) {
                            for (j in 0 until formArray.length()) {
                                formList.add(formArray.getString(j))
                            }
                        }

                        val teamIdStr = obj.optString("team_id", "")

                        list.add(
                            StandingRow(
                                team_id = teamIdStr,
                                position = obj.optInt("position", 0),
                                team_name = obj.optString("team_name", ""),
                                logo = obj.optString("logo", ""),
                                games = obj.optInt("games", 0),
                                win = obj.optInt("win", 0),
                                draw = obj.optInt("draw", 0),
                                loss = obj.optInt("loss", 0),
                                goals_for = obj.optInt("goals_for", 0),
                                goals_against = obj.optInt("goals_against", 0),
                                points = obj.optInt("points", 0),
                                is_group_header = obj.optBoolean("is_group_header", false),
                                group_name = obj.optString("group_name", ""),
                                form = formList
                            )
                        )
                    }

                    activity?.runOnUiThread {
                        adapter.updateData(list)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Помилка обробки таблиці: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
