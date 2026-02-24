package ua.lviv.maf

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MatchPagerAdapter(
    activity: AppCompatActivity,
    private val dates: List<DateModel>,
    private val allMatches: List<TournamentRow>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = dates.size

    override fun createFragment(position: Int): Fragment {
        val dateString = dates[position].date
        // Фільтруємо всі матчі, залишаючи тільки ті, що на цю дату
        val matchesForDate = allMatches.filter { it.date == dateString }
        return MatchPageFragment.newInstance(matchesForDate)
    }
}
