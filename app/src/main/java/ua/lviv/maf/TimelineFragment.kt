package ua.lviv.maf

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class TimelineFragment : Fragment(R.layout.fragment_timeline) {

    companion object {
        fun newInstance(matchId: String): TimelineFragment {
            val args = Bundle()
            args.putString("match_id", matchId)
            val fragment = TimelineFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val matchId = arguments?.getString("match_id") ?: ""
        // Тут буде логіка завантаження подій матчу
    }
}
