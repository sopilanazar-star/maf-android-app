package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PlayerStatsFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var spinner: Spinner
    private var playerId: String = ""
    private var playerPos: String = ""

    companion object {
        fun newInstance(playerId: String, position: String): PlayerStatsFragment {
            val fragment = PlayerStatsFragment()
            fragment.arguments = Bundle().apply {
                putString("player_id", playerId)
                putString("position", position)
            }
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_stats, container, false)
        statsGrid = view.findViewById(R.id.statsGrid)
        spinner = view.findViewById(R.id.spinnerTournaments)
        
        playerId = arguments?.getString("player_id") ?: ""
        playerPos = arguments?.getString("position") ?: ""

        setupSpinner()
        loadData()
        return view
    }

    private fun setupSpinner() {
        val list = arrayOf("Загальна статистика за сезон", "Перша ліга", "Кубок Пролісок", "Кубок Весни")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, list)
        spinner.adapter = adapter
    }

    private fun loadData() {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/player-stats?id=$playerId"
        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: return
                activity?.runOnUiThread { parseAndShow(json) }
            }
        })
    }

    private fun parseAndShow(json: String) {
        try {
            val d = JSONObject(json)
            statsGrid.removeAllViews()

            val isGK = playerPos.lowercase() in listOf("g", "gk")

            // Рядок 1
            addCell("Зіграні матчі", d.optString("matches"))
            addCell("У старті", d.optString("starts"))
            
            // Рядок 2
            addCell("Вийшов на заміну", d.optString("subs_in"))
            addCell("Хвилини на полі", "${d.optString("minutes")}'")
            
            // Рядок 3
            addCell("Жовті картки", d.optString("yellow"))
            addCell("Другі жовті", d.optString("yellow_red"))

            if (isGK) {
                // Варіант А: Воротар
                addCell("Вилучення", d.optString("red"))
                addCell("Голи (забиті)", d.optString("goals"))
                addCell("Пропущені голи", d.optString("conceded"))
                addCell("Сухі матчі", d.optString("clean_sheets"))
            } else {
                // Варіант Б: Польовий
                addCell("Вилучення", d.optString("red"))
                addCell("ГОЛИ", d.optString("goals"), true)
            }
        } catch (e: Exception) {}
    }

    private fun addCell(label: String, value: String, isGoal: Boolean = false) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 20)
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply { width = 0 }
        }

        val tvVal = TextView(context).apply {
            text = if (value == "null" || value.isEmpty()) "0" else value
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (isGoal) Color.parseColor("#00E676") else Color.WHITE)
        }

        val tvLab = TextView(context).apply {
            text = label
            textSize = 11f
            setTextColor(Color.parseColor("#BCBCBC"))
        }

        layout.addView(tvVal)
        layout.addView(tvLab)
        statsGrid.addView(layout)
    }
}
