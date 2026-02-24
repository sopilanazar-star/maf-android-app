package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MatchPageFragment : Fragment() {

    private var matches: List<TournamentRow> = emptyList()

    companion object {
        fun newInstance(matchesForDate: List<TournamentRow>): MatchPageFragment {
            val fragment = MatchPageFragment()
            fragment.matches = matchesForDate
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Створюємо список прямо в коді, щоб не плодити зайві XML
        val context = requireContext()
        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            // Робимо відступ знизу, щоб BottomNav не перекривав матчі (90dp)
            setPadding(0, 0, 0, (90 * resources.displayMetrics.density).toInt())
            clipToPadding = false
        }

        // Групуємо матчі за лігами (твоя логіка) і ставимо адаптер
        val groupedMatches = groupMatchesByLeagueAndStage(matches)
        recyclerView.adapter = TournamentAdapter(groupedMatches)

        return recyclerView
    }

    private fun groupMatchesByLeagueAndStage(matches: List<TournamentRow>): List<TournamentRow> {
        val result = mutableListOf<TournamentRow>()
        val grouped = matches.groupBy { "${it.league}|${it.stage}" }
        for ((key, leagueMatches) in grouped) {
            val parts = key.split("|")
            result.add(TournamentRow(league = parts[0], stage = parts.getOrElse(1) { "" }, isHeader = true))
            result.addAll(leagueMatches)
        }
        return result
    }
}
