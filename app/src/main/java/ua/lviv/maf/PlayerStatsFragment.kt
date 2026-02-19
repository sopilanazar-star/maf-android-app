package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.GridLayout // 🔥 ВАЖЛИВО: використовуємо стандартний віджет
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PlayerStatsFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var spinner: Spinner
    private var playerId: String = ""
    private var position: String = ""

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

        setupSpinner()
        loadStats()
        return view
    }

    private fun setupSpinner() {
        val tournaments = arrayOf("Загальна статистика за сезон", "Перша ліга", "Кубок Пролісок", "Кубок Весни")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tournaments)
        spinner.adapter = adapter
    }

    private fun loadStats() {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/player-stats?id=$playerId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: return
                activity?.runOnUiThread { parseAndDisplay(json) }
            }
        })
    }

    private fun parseAndDisplay(json: String) {
        try {
            val data = JSONObject(json)
            statsGrid.removeAllViews()

            val isGK = position.lowercase() == "g" || position.lowercase() == "gk"

            // Спільні поля (Варіант Б + частина А)
            addStatItem("Зіграні матчі", data.optString("matches"))
            addStatItem("У старті", data.optString("starts"))
            addStatItem("Вийшов на заміну", data.optString("subs_in"))
            addStatItem("Хвилини на полі", "${data.optString("minutes")}'")
            addStatItem("Жовті картки", data.optString("yellow"))
            addStatItem("Другі жовті", data.optString("yellow_red"))
            addStatItem("Вилучення", data.optString("red"))

            if (isGK) {
                // Додаткові поля для воротаря (Варіант А)
                addStatItem("Голи", data.optString("goals"))
                addStatItem("Пропущені голи", data.optString("conceded"))
                addStatItem("Сухі матчі", data.optString("clean_sheets"))
            } else {
                // Основне поле для польового
                addStatItem("ГОЛИ", data.optString("goals"), highlight = true)
            }

        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun addStatItem(label: String, value: String, highlight: Boolean = false) {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 24)
            
            // Налаштування для стандартного GridLayout
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            layoutParams = params
        }

        val tvValue = TextView(context).apply {
            text = if (value == "null" || value.isEmpty()) "0" else value
            textSize = 20f
            setTextColor(if (highlight) Color.parseColor("#00E676") else Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val tvLabel = TextView(context).apply {
            text = label
            textSize = 12f
            setTextColor(Color.parseColor("#BCBCBC"))
        }

        container.addView(tvValue)
        container.addView(tvLabel)
        statsGrid.addView(container)
    }
}
