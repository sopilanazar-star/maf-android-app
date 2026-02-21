package ua.lviv.maf.ui

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
import ua.lviv.maf.AppConfig
import ua.lviv.maf.R
import ua.lviv.maf.models.StandingItem

class StandingFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StandingAdapter

    private val competitions = mutableListOf<Competition>()

    private val COMPS_URL = "https://maf.lviv.ua/wp-json/maf/v2/competitions"
    private val STANDING_URL = "https://maf.lviv.ua/wp-json/maf/v2/standing"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_standing, container, false)

        tabLayout = view.findViewById(R.id.tabLayoutCompetitions)
        recyclerView = view.findViewById(R.id.rvStanding)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = StandingAdapter()
        recyclerView.adapter = adapter

        loadCompetitions()

        return view
    }

    // =========================
    // ТУРНІРИ
    // =========================
    private fun loadCompetitions() {
        val client = OkHttpClient()
        val url = "$COMPS_URL?year=${AppConfig.selectedYear}"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка турнірів", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()?.trim() ?: ""
                val array = JSONArray(json)

                competitions.clear()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    competitions.add(
                        Competition(
                            obj.getInt("id"),
                            obj.getString("title")
                        )
                    )
                }

                activity?.runOnUiThread { updateTabs() }
            }
        })
    }

    private fun updateTabs() {
        tabLayout.removeAllTabs()

        for (comp in competitions) {
            tabLayout.addTab(tabLayout.newTab().setText(comp.title))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val pos = tab?.position ?: 0
                loadStanding(competitions[pos].id)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        if (competitions.isNotEmpty()) {
            loadStanding(competitions[0].id)
        }
    }

    // =========================
    // ТАБЛИЦЯ
    // =========================
    private fun loadStanding(compId: Int) {
        val client = OkHttpClient()
        val url = "$STANDING_URL?competition_id=$compId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()?.trim() ?: ""

                val array = JSONArray(json)
                val list = mutableListOf<StandingItem>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)

                    if (obj.optBoolean("is_group_header")) {

                        list.add(
                            StandingItem.GroupHeader(
                                obj.getString("group_name")
                            )
                        )

                        list.add(StandingItem.TableHeader())

                    } else {

                        list.add(
                            StandingItem.TeamRow(
                                position = obj.getInt("position"),
                                name = obj.getString("team_name"),
                                logo = obj.getString("logo"),
                                games = obj.getInt("games"),
                                win = obj.getInt("win"),
                                draw = obj.getInt("draw"),
                                loss = obj.getInt("loss"),
                                goalsFor = obj.getInt("goals_for"),
                                goalsAgainst = obj.getInt("goals_against"),
                                points = obj.getInt("points")
                            )
                        )
                    }
                }

                activity?.runOnUiThread {
                    adapter.submit(list)
                }
            }
        })
    }
}

data class Competition(
    val id: Int,
    val title: String
)
