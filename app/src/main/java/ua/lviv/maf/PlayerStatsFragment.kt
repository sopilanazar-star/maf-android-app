package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PlayerStatsFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var spinner: Spinner
    private var playerId: String = ""
    private var position: String = ""

    // Зберігаємо завантажені дані, щоб не робити зайвих запитів
    private var allStatsData: JSONObject? = null
    private val tournamentIds = mutableListOf<String>()
    private val tournamentNames = mutableListOf<String>()

    companion object {
        fun newInstance(playerId: String, position: String): PlayerStatsFragment {
            val fragment = PlayerStatsFragment()
            val args = Bundle().apply {
                putString("player_id", playerId)
                putString("position", position)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_stats, container, false)
        statsGrid = view.findViewById(R.id.statsGrid)
        spinner = view.findViewById(R.id.spinnerTournaments)
        
        playerId = arguments?.getString("player_id") ?: ""
        position = arguments?.getString("position") ?: ""

        setupSpinnerListener()
        loadStats()
        return view
    }

    private fun loadStats() {
        // 🔥 ПРАВКА: Додаємо параметр року, який вибрав користувач, щоб сервер віддав правильні цифри
        val year = AppConfig.selectedYear
        val url = "https://maf.lviv.ua/wp-json/maf/v2/player-stats?id=$playerId&year=$year"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: return
                activity?.runOnUiThread { parseAndSetup(json) }
            }
        })
    }

    private fun parseAndSetup(json: String) {
        try {
            val root = JSONObject(json)
            
            // 1. Отримуємо масив турнірів
            val tournamentsArray = root.optJSONArray("tournaments") ?: JSONArray()
            tournamentIds.clear()
            tournamentNames.clear()

            for (i in 0 until tournamentsArray.length()) {
                val t = tournamentsArray.getJSONObject(i)
                tournamentIds.add(t.optString("id"))
                tournamentNames.add(t.optString("name"))
            }

            // 2. Зберігаємо всі цифри
            allStatsData = root.optJSONObject("stats")

            // 3. Заповнюємо випадаючий список реальними турнірами
            if (context != null) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tournamentNames)
                spinner.adapter = adapter
            }

        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun setupSpinnerListener() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Коли користувач обирає турнір, ми беремо його ID і малюємо таблицю
                val selectedId = tournamentIds[position]
                val statsForTournament = allStatsData?.optJSONObject(selectedId)
                if (statsForTournament != null) {
                    displayStats(statsForTournament)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun displayStats(data: JSONObject) {
        statsGrid.removeAllViews()
        val isGK = position.lowercase() in listOf("g", "gk")

        addStatItem("Зіграні матчі", data.optString("matches"), R.drawable.ic_matches, null, false)
        addStatItem("У старті", data.optString("starts"), R.drawable.ic_lineup, null, false)
        addStatItem("Вийшов на заміну", data.optString("subs_in"), R.drawable.ic_substitution, null, false)
        addStatItem("Хвилини на полі", "${data.optString("minutes")}'", R.drawable.ic_time, null, false)
        
        addStatItem("Жовті картки", data.optString("yellow"), R.drawable.ic_card, Color.YELLOW, false)
        addStatItem("Другі жовті", data.optString("yellow_red"), R.drawable.ic_second_yellow, null, false)
        addStatItem("Вилучення", data.optString("red"), R.drawable.ic_card, Color.RED, false)

        val ownGoals = data.optString("own_goals")
        if (ownGoals != "0" && ownGoals.isNotEmpty() && ownGoals != "null") {
            addStatItem("Автоголи", ownGoals, R.drawable.ic_ball, Color.RED, false)
        }

        if (isGK) {
            addStatItem("Голи (забиті)", data.optString("goals"), R.drawable.ic_ball, null, false)
            addStatItem("Пропущені голи", data.optString("conceded"), R.drawable.ic_goal_conceded, null, false)
            addStatItem("Сухі матчі", data.optString("clean_sheets"), R.drawable.ic_clean_sheet, null, false)
        } else {
            addStatItem("ГОЛИ", data.optString("goals"), R.drawable.ic_ball, null, true)
        }
    }

    private fun addStatItem(label: String, value: String, iconRes: Int, iconColor: Int?, highlight: Boolean) {
        val statView: View = layoutInflater.inflate(R.layout.item_stat_block, statsGrid, false)
        
        val tvValue: TextView = statView.findViewById(R.id.tvStatValue)
        val tvLabel: TextView = statView.findViewById(R.id.tvStatLabel)
        val ivIcon: ImageView = statView.findViewById(R.id.ivStatIcon)

        tvValue.text = if (value == "null" || value.isEmpty() || value == "0'") "0" else value
        tvLabel.text = label

        ivIcon.setImageResource(iconRes)
        if (iconColor != null) ivIcon.setColorFilter(iconColor)
        if (highlight) tvValue.setTextColor(Color.parseColor("#00E676"))

        val params = GridLayout.LayoutParams()
        params.width = 0
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        statView.layoutParams = params

        statsGrid.addView(statView)
    }
}
