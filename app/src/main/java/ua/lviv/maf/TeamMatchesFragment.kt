package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class TeamMatchesFragment : Fragment() {

    companion object {
        fun newInstance(teamId: String): TeamMatchesFragment {
            val args = Bundle().apply { putString("team_id", teamId) }
            return TeamMatchesFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(context).apply {
            text = "Тут будуть матчі команди"
            setTextColor(Color.GRAY)
            textSize = 16f
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
    }
}
