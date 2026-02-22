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

// Модель даних для турніру
data class Competition(val id: String, val title: String)

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

    // Публічний метод для оновлення даних при зміні року в MainActivity
    fun refreshData() {
        if (isAdded) { // Перевірка, чи фрагмент ще активний
            loadCompetitions()
        }
    }

    private fun loadCompetitions() {
        val client = OkHttpClient.Builder()
            .cache(null)
            .build()
        
        // Використовуємо вибраний у додатку рік
        val url = "$COMPS_URL?year=${AppConfig.selectedYear}"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка зв'язку (Турніри)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()?.trim() ?: "" 
                
                try {
                    val array = JSONArray(json)
                    competitions.clear()

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        
                        // Підтримуємо і 'id', і 'term_id'
                        val compId = obj.optString("id", obj.optString("term_id", ""))
                        // Підтримуємо і 'name' (з твого нового PHP), і 'title'
                        val compTitle = obj.optString("name", obj.optString("title", "Турнір"))
                        
                        if (compId.isNotEmpty()) {
                            competitions.add(Competition(compId, compTitle))
                        }
                    }

                    activity?.runOnUiThread { updateTabs() }

                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Турнірів за цей рік не знайдено", Toast.LENGTH_SHORT).show()
                        updateTabs() // Очищуємо вкладки, якщо порожньо
                    }
                }
            }
        })
    }

    private fun updateTabs() {
        tabLayout.removeAllTabs()
        tabLayout.clearOnTabSelectedListeners() 

        for (comp in competitions) {
            tabLayout.addTab(tabLayout.newTab().setText(comp.title))
        }

        setupTabListener()

        if (competitions.isNotEmpty()) {
            loadStanding(competitions[0].id)
        } else {
            adapter.updateData(emptyList()) // Очищуємо таблицю, якщо ліг немає
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
        val url = "$STANDING_URL?competition_id=$compId&year=${AppConfig.selectedYear}"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Помилка завантаження таблиці", Toast.LENGTH_SHORT).show()
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
                        list.add(StandingRow(
                            team_id = obj.optString("team_id", ""),
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
                        ))
                    }
                    activity?.runOnUiThread { adapter.updateData(list) }
                } catch (e: Exception) {
                    activity?.runOnUiThread { adapter.updateData(emptyList()) }
                }
            }
        })
    }
}
