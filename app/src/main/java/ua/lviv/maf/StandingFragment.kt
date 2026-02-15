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

        setupTabListener()
        loadCompetitions()

        return view
    }

    private fun loadCompetitions() {

        val client = OkHttpClient()
        val request = Request.Builder().url(COMPS_URL).build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка ліг", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val json = response.body?.string() ?: ""

                try {
                    val array = JSONArray(json)
                    competitions.clear()

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)

                        competitions.add(
                            Competition(
                                obj.getString("id"),
                                obj.getString("title")
                            )
                        )
                    }

                    activity?.runOnUiThread { updateTabs() }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun updateTabs() {

        tabLayout.removeAllTabs()

        for (comp in competitions) {
            tabLayout.addTab(tabLayout.newTab().setText(comp.title))
        }

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

    private fun loadStanding(compId: String) {

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$STANDING_URL?competition_id=$compId")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка таблиці", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val json = response.body?.string() ?: ""

                try {

                    val array = JSONArray(json)
                    val list = mutableListOf<StandingRow>()

                    for (i in 0 until array.length()) {

                        val obj = array.getJSONObject(i)

                        val teamId = obj.optString("team_id", "0")

                        list.add(
                            StandingRow(
                                team_id = teamId,
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
                                form = emptyList()
                            )
                        )
                    }

                    activity?.runOnUiThread {
                        recyclerView.adapter = StandingAdapter(list)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }
}
