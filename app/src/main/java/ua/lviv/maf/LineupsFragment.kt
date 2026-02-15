package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LineupsFragment : Fragment(R.layout.fragment_lineups) {

    private var containerHome: LinearLayout? = null
    private var containerAway: LinearLayout? = null
    private val client = OkHttpClient()

    companion object {
        fun newInstance(matchId: String): LineupsFragment {
            val args = Bundle()
            args.putString("match_id", matchId)
            val fragment = LineupsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        containerHome = view.findViewById(R.id.containerHome)
        containerAway = view.findViewById(R.id.containerAway)

        val matchId = arguments?.getString("match_id") ?: ""
        if (matchId.isNotEmpty()) {
            fetchLineups(matchId)
        }
    }

    private fun fetchLineups(matchId: String) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/match-details?id=$matchId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: return
                activity?.runOnUiThread {
                    parseAndShowLineups(json)
                }
            }
        })
    }

    private fun parseAndShowLineups(json: String) {
        try {
            val root = JSONObject(json)
            if (!root.has("lineups")) return

            val lineups = root.getJSONObject("lineups")

            containerHome?.removeAllViews()
            containerAway?.removeAllViews()

            // HOME
            addSectionTitle(containerHome, "СТАРТ")
            displayPlayers(lineups.getJSONArray("home_start"), containerHome)

            addSectionTitle(containerHome, "ЗАПАСНІ")
            displayPlayers(lineups.getJSONArray("home_subs"), containerHome)

            // AWAY
            addSectionTitle(containerAway, "СТАРТ")
            displayPlayers(lineups.getJSONArray("away_start"), containerAway)

            addSectionTitle(containerAway, "ЗАПАСНІ")
            displayPlayers(lineups.getJSONArray("away_subs"), containerAway)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addSectionTitle(container: LinearLayout?, title: String) {
        val tv = TextView(context).apply {
            text = title
            setTextColor(Color.parseColor("#4CAF50"))
            textSize = 12f
            setPadding(10, 20, 10, 10)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_HORIZONTAL
        }
        container?.addView(tv)
    }

    private fun displayPlayers(players: org.json.JSONArray, container: LinearLayout?) {
        for (i in 0 until players.length()) {

            val p = players.getJSONObject(i)

            val name = p.optString("name")
            val number = p.optString("number")
            val posRaw = p.optString("position")

            val pos = when (posRaw) {
                "g" -> "Г"
                "d" -> "З"
                "m" -> "П"
                "f" -> "Н"
                else -> ""
            }

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(10, 12, 10, 12)
                gravity = Gravity.CENTER_VERTICAL
            }

            val posBox = TextView(context).apply {
                text = pos
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.parseColor("#8BC34A"))
                setPadding(14, 6, 14, 6)
                textSize = 12f
                gravity = Gravity.CENTER
            }

            val nameTv = TextView(context).apply {
                text = if (number.isNotEmpty())
                    "$number  $name"
                else name

                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(20, 0, 0, 0)
            }

            row.addView(posBox)
            row.addView(nameTv)

            container?.addView(row)

            val line = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                setBackgroundColor(Color.parseColor("#333A45"))
            }
            container?.addView(line)
        }
    }
}
