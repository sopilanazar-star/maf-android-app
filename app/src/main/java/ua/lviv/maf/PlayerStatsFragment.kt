package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout

class PlayerStatsFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var spinner: Spinner
    private var playerPosition: String = ""

    companion object {
        fun newInstance(playerId: String, position: String): PlayerStatsFragment {
            val fragment = PlayerStatsFragment()
            val args = Bundle()
            args.putString("player_id", playerId)
            args.putString("position", position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_stats, container, false)
        statsGrid = view.findViewById(R.id.statsGrid)
        spinner = view.findViewById(R.id.spinnerTournaments)
        playerPosition = arguments?.getString("position") ?: ""

        setupTournamentSpinner()
        return view
    }

    private fun setupTournamentSpinner() {
        // Список турнірів (потім можна брати з API)
        val tournaments = arrayOf("Загальна статистика", "Кубок Пролісок", "Кубок Весни", "Перша ліга", "Кубок Золота Осінь")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tournaments)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateStatsTable()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateStatsTable() {
        statsGrid.removeAllViews()

        val isGK = playerPosition.lowercase() == "g" || playerPosition.lowercase() == "gk"

        if (isGK) {
            // ВАРІАНТ А: ВОРОТАР (5 рядків)
            addStatRow("Зіграні матчі", "12", "У старті", "12")
            addStatRow("Вийшов на заміну", "0", "Хвилини на полі", "1080'")
            addStatRow("Жовті картки", "1", "Другі жовті", "0")
            addStatRow("Вилучення", "0", "Голи", "0")
            addStatRow("Пропущені голи", "8", "Сухі матчі", "5")
        } else {
            // ВАРІАНТ Б: ПОЛЬОВИЙ (4 рядки)
            addStatRow("Зіграні матчі", "10", "У старті", "8")
            addStatRow("Вийшов на заміну", "2", "Хвилини на полі", "740'")
            addStatRow("Жовті картки", "2", "Другі жовті", "1")
            addStatRow("Вилучення", "0", "ГОЛИ", "4")
        }
    }

    private fun addStatRow(label1: String, val1: String, label2: String, val2: String) {
        statsGrid.addView(createStatItem(label1, val1))
        statsGrid.addView(createStatItem(label2, val2))
    }

    private fun createStatItem(label: String, value: String): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 15, 0, 15)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            layoutParams = params
        }

        val tvLabel = TextView(context).apply {
            text = label
            setTextColor(Color.parseColor("#BCBCBC"))
            textSize = 12f
        }

        val tvValue = TextView(context).apply {
            text = value
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }

        layout.addView(tvLabel)
        layout.addView(tvValue)
        return layout
    }
}
