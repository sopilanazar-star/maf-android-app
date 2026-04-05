package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Додаємо імпорти для адаптера та моделі
import ua.lviv.maf.adapters.PlayerPredictionsAdapter
import ua.lviv.maf.models.PlayerMatchPrediction

class PredictionPlayerFragment : Fragment() {

    private lateinit var rvPlayerMatches: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_prediction_player, container, false)

        val tvPlayerName = view.findViewById<TextView>(R.id.tvPlayerName)
        val tvPoints = view.findViewById<TextView>(R.id.tvPoints)
        val tvExact = view.findViewById<TextView>(R.id.tvExact)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val tvMiss = view.findViewById<TextView>(R.id.tvMiss)
        val tvAccuracy = view.findViewById<TextView>(R.id.tvAccuracy)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        // 1. Отримуємо дані гравця
        val username = arguments?.getString("username") ?: "Гравець"
        tvPlayerName.text = username
        tvTitle.text = username

        // Тимчасові дані (захардкоджені), поки не підключимо API
        val exact = 10
        val result = 3
        val miss = 2
        val points = 14
        val total = exact + result + miss
        val accuracy = if (total > 0) (exact * 100 / total) else 0

        tvPoints.text = points.toString()
        tvExact.text = "🎯 $exact"
        tvResult.text = "✔ $result"
        tvMiss.text = "✖ $miss"
        tvAccuracy.text = "📊 $accuracy%"

        // 2. Налаштовуємо список матчів
        rvPlayerMatches = view.findViewById(R.id.rvPlayerMatches)
        rvPlayerMatches.layoutManager = LinearLayoutManager(requireContext())

        // Тимчасові дані для списку матчів (для перевірки дизайну)
        val dummyMatches = listOf(
            PlayerMatchPrediction("ФК Ураган - ІММ Устя", "24.03.2026", "2 : 1", "1 : 1", "+1"),
            PlayerMatchPrediction("Скала - ФК Миколаїв", "25.03.2026", "0 : 2", "0 : 2", "+3"),
            PlayerMatchPrediction("Дністер - Темп", "26.03.2026", "1 : 0", "0 : 0", "0")
        )

        // Підключаємо адаптер
        rvPlayerMatches.adapter = PlayerPredictionsAdapter(dummyMatches)

        // 3. ПРАВИЛЬНА КНОПКА НАЗАД
        btnBack?.setOnClickListener {
            // Повертаємо користувача на попередній екран (в таблицю)
            parentFragmentManager.popBackStack()
        }

        return view
    }
}