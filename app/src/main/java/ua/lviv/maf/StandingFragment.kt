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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StandingFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StandingAdapter
    private var competitions: List<Competition> = listOf() // Потрібно буде створити просту модель Competition

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_standing, container, false)
        
        tabLayout = view.findViewById(R.id.tabLayoutCompetitions)
        recyclerView = view.findViewById(R.id.rvStanding)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = StandingAdapter(listOf())
        recyclerView.adapter = adapter

        setupTabListener()
        loadCompetitions() // Крок 1: завантажуємо список турнірів
        
        return view
    }

    private fun loadCompetitions() {
        // Тут має бути твій виклик API до /maf/v2/competitions
        ApiClient.apiService.getCompetitions().enqueue(object : Callback<List<Competition>> {
            override fun onResponse(call: Call<List<Competition>>, response: Response<List<Competition>>) {
                if (response.isSuccessful) {
                    competitions = response.body() ?: listOf()
                    updateTabs()
                }
            }
            override fun onFailure(call: Call<List<Competition>>, t: Throwable) {
                Toast.makeText(context, "Помилка завантаження турнірів", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTabs() {
        tabLayout.removeAllTabs()
        for (comp in competitions) {
            tabLayout.addTab(tabLayout.newTab().setText(comp.title))
        }
        // Завантажуємо таблицю для першого турніру за замовчуванням
        if (competitions.isNotEmpty()) {
            loadStanding(competitions[0].id)
        }
    }

    private fun setupTabListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                if (position < competitions.size) {
                    loadStanding(competitions[position].id)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadStanding(compId: String) {
        // Виклик API до /maf/v2/standing?competition_id=...
        ApiClient.apiService.getStanding(compId).enqueue(object : Callback<List<StandingRow>> {
            override fun onResponse(call: Call<List<StandingRow>>, response: Response<List<StandingRow>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: listOf())
                }
            }
            override fun onFailure(call: Call<List<StandingRow>>, t: Throwable) {
                Toast.makeText(context, "Помилка завантаження таблиці", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
