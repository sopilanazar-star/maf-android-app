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

            // 1. Спільні блоки для всіх
            addStatItem("Зіграні матчі", data.optString("matches"), R.drawable.ic_matches)
            addStatItem("У старті", data.optString("starts"), R.drawable.ic_lineup)
            addStatItem("Вийшов на заміну", data.optString("subs_in"), R.drawable.ic_substitution)
            addStatItem("Хвилини на полі", "${data.optString("minutes")}'", R.drawable.ic_time)
            
            // 2. Картки (використовуємо ic_card з кольоровими фільтрами)
            addStatItem("Жовті картки", data.optString("yellow"), R.drawable.ic_card, Color.YELLOW)
            addStatItem("Другі жовті", data.optString("yellow_red"), R.drawable.ic_second_yellow)
            addStatItem("Вилучення", data.optString("red"), R.drawable.ic_card, Color.RED)

            // 3. Специфічні блоки
            if (isGK) {
                addStatItem("Голи (забиті)", data.optString("goals"), R.drawable.ic_ball)
                addStatItem("Пропущені голи", data.optString("conceded"), R.drawable.ic_goal_conceded)
                addStatItem("Сухі матчі", data.optString("clean_sheets"), R.drawable.ic_clean_sheet)
            } else {
                // Для польового виділяємо ГОЛИ зеленим
                addStatItem("ГОЛИ", data.optString("goals"), R.drawable.ic_ball, highlight = true)
            }

        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun addStatItem(label: String, value: String, iconRes: Int, iconColor: Int? = null, highlight: Boolean = false) {
        // Інфлейтимо наш новий макет плитки
        val view = layoutInflater.inflate(R.layout.item_stat_block, statsGrid, false)
        
        val tvValue: TextView = view.findViewById(R.id.tvStatValue)
        val tvLabel: TextView = view.findViewById(R.id.tvStatLabel)
        val ivIcon: ImageView = view.findViewById(R.id.ivStatIcon)

        tvValue.text = if (value == "null" || value.isEmpty() || value == "0'") "0" else value
        tvLabel.text = label

        // Налаштування іконки
        ivIcon.setImageResource(iconRes)
        if (iconColor != null) {
            ivIcon.setColorFilter(iconColor)
        }
        
        // Якщо це ГОЛИ для польового - робимо колір цифри яскравим
        if (highlight) {
            tvValue.setTextColor(Color.parseColor("#00E676"))
        }

        // Параметри для GridLayout (2 колонки)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params

        statsGrid.addView(view)
    }
}
