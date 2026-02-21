package ua.lviv.maf.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import org.json.JSONArray
import ua.lviv.maf.R
import ua.lviv.maf.models.StandingItem
import java.io.IOException

class StandingFragment : Fragment(R.layout.fragment_standing) {

    private lateinit var adapter: StandingAdapter
    private val tournaments = mutableListOf<org.json.JSONObject>()

    private val URL = "https://maf.lviv.ua/wp-json/maf/v2/tables"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvStanding)
        val tabs = view.findViewById<TabLayout>(R.id.tabLayoutCompetitions)

        adapter = StandingAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadData(tabs)
    }

    private fun loadData(tabs: TabLayout) {

        val client = OkHttpClient()
        val request = Request.Builder().url(URL).build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val json = response.body?.string()?.trim() ?: "[]"
                val array = JSONArray(json)

                tournaments.clear()

                for (i in 0 until array.length()) {
                    tournaments.add(array.getJSONObject(i))
                }

                activity?.runOnUiThread {
                    tabs.removeAllTabs()

                    tournaments.forEach {
                        tabs.addTab(tabs.newTab().setText(it.getString("title")))
                    }

                    tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            showTournament(tab?.position ?: 0)
                        }
                        override fun onTabUnselected(tab: TabLayout.Tab?) {}
                        override fun onTabReselected(tab: TabLayout.Tab?) {}
                    })

                    if (tournaments.isNotEmpty()) {
                        showTournament(0)
                    }
                }
            }
        })
    }

    private fun showTournament(index: Int) {

        val obj = tournaments[index]
        val list = mutableListOf<StandingItem>()

        val table = obj.optJSONArray("table")

        if (table != null) {

            for (i in 0 until table.length()) {

                val row = table.getJSONObject(i)

                if (row.optBoolean("is_group_header")) {
                    list.add(StandingItem.GroupHeader(row.optString("group_name")))
                    list.add(StandingItem.TableHeader)
                } else {
                    list.add(
                        StandingItem.TeamRow(
                            position = row.optInt("position"),
                            name = row.optString("team_name"),
                            logo = row.optString("logo"),
                            games = row.optInt("games"),
                            win = row.optInt("win"),
                            draw = row.optInt("draw"),
                            loss = row.optInt("loss"),
                            goalsFor = row.optInt("goals_for"),
                            goalsAgainst = row.optInt("goals_against"),
                            points = row.optInt("points")
                        )
                    )
                }
            }
        }

        val playoff = obj.optJSONArray("playoff")

        if (playoff != null && playoff.length() > 0) {

            list.add(StandingItem.PlayoffHeader("ПЛЕЙ-ОФ"))

            for (i in 0 until playoff.length()) {
                val stage = playoff.getJSONObject(i)
                list.add(
                    StandingItem.PlayoffStage(
                        stage.getString("group_name")
                    )
                )
            }
        }

        adapter.submit(list)
    }
}
