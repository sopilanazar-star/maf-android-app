package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TimelineFragment : Fragment() {

    private val client = OkHttpClient()
    private var homeTeamId: String? = null

    companion object {
        fun newInstance(matchId: String, homeTeamId: String): TimelineFragment {
            val args = Bundle().apply { 
                putString("match_id", matchId) 
                putString("home_team_id", homeTeamId)
            }
            return TimelineFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val matchId = arguments?.getString("match_id") ?: return
        homeTeamId = arguments?.getString("home_team_id")
        
        val container = view.findViewById<LinearLayout>(R.id.timelineContainer)
        val tvLoading = view.findViewById<TextView>(R.id.tvTimelineLoading)
        val centerLine = view.findViewById<View>(R.id.centerLine)

        loadTimelineData(matchId, container, tvLoading, centerLine)
    }

    private fun loadTimelineData(matchId: String, container: LinearLayout, tvLoading: TextView?, centerLine: View?) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/match-details?id=$matchId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { tvLoading?.text = "Помилка мережі" }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { 
                    if (!response.isSuccessful) return
                    val json = response.body?.string() ?: return
                    activity?.runOnUiThread {
                        tvLoading?.visibility = View.GONE
                        centerLine?.visibility = View.VISIBLE
                        parseAndShowTimeline(json, container)
                    }
                }
            }
        })
    }

    private fun parseAndShowTimeline(json: String, container: LinearLayout) {
        try {
            val root = JSONObject(json)

            // --- ВСТАВЛЯЙ ЦЕЙ БЛОК ---
            val penScore = root.optString("pen_score", "")
            if (penScore.isNotEmpty()) {
                (activity as? MatchDetailActivity)?.runOnUiThread {
                    activity?.findViewById<TextView>(R.id.tvAdditionalScore)?.apply {
                        text = "(пен. $penScore)"
                        visibility = View.VISIBLE
                    }
                }
            }
            // --- КІНЕЦЬ БЛОКУ ---

            val timeline = root.optJSONArray("timeline") ?: return
            
            // Надійна перевірка ID господаря з самого JSON
            val jsonHomeId = root.optString("home_team_id")
            if (jsonHomeId.isNotEmpty() && jsonHomeId != "0") {
                homeTeamId = jsonHomeId
            }

            container.removeAllViews()
            var penaltyHeaderAdded = false

            if (timeline.length() == 0) {
                val emptyTv = TextView(context).apply {
                    text = "Подій поки немає"
                    setTextColor(Color.GRAY)
                    gravity = Gravity.CENTER
                    setPadding(0, 50, 0, 0)
                }
                container.addView(emptyTv)
                return
            }

            for (i in 0 until timeline.length()) {
                val event = timeline.getJSONObject(i)
                val minute = event.optString("minute")
                val playerName = event.optString("player_name")
                val playerOutName = event.optString("player_out_name")
                val type = event.optString("type")
                val eventTeamId = event.optString("team_id")
                val eventSide = event.optString("side")
                // ДОДАНО: Витягуємо ID гравця
                val playerId = event.optString("player_id")

                // Визначаємо сторону: або за збігом ID, або якщо сервер прямо каже "left"
                val isHomeTeam = (eventTeamId == homeTeamId && homeTeamId != "0") || eventSide == "left"
// --- ДОДАТИ ЦЕЙ БЛОК ---
                if ((type == "penalty_goal" || type == "penalty_miss") && container.findViewWithTag<View>("pen_header") == null) {
                    val tvHeader = TextView(context).apply {
                        tag = "pen_header"
                        text = "ПІСЛЯМАТЧЕВІ ПЕНАЛЬТІ"
                        setTextColor(Color.parseColor("#E30613"))
                        textSize = 18f
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                        setPadding(0, 40, 0, 20)
                    }
                    container.addView(tvHeader)
                }
                // -----------------------
                val rowLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(-1, -2)
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 25, 0, 25)
                }

                val infoLayout = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                    orientation = LinearLayout.VERTICAL
                    gravity = if (isHomeTeam) Gravity.END else Gravity.START
                }

                val tvPlayer = TextView(context).apply {
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = if (isHomeTeam) Gravity.END else Gravity.START
                    
                    if ((type == "substitution" || type == "sub") && !playerOutName.isNullOrBlank() && playerOutName != "null") {
                        val sb = SpannableStringBuilder()
                        
                        // Гравець, що входить (Зелений)
                        val startIn = sb.length
                        sb.append(playerName)
                        sb.setSpan(ForegroundColorSpan(Color.parseColor("#00E676")), startIn, sb.length, 0)
                        
                        sb.append("\n")
                        
                        // Гравець, що виходить (Червоний)
                        val startOut = sb.length
                        sb.append(playerOutName)
                        sb.setSpan(ForegroundColorSpan(Color.parseColor("#FF5252")), startOut, sb.length, 0)
                        
                        text = sb
                    } else {
                        text = if (type == "goal_og") "$playerName (АГ)" else playerName
                        setTextColor(Color.WHITE)
                    }
                }

                val tvMin = TextView(context).apply {
                    text = "$minute'"
                    setTextColor(Color.parseColor("#BCBCBC"))
                    textSize = 12f
                    gravity = if (isHomeTeam) Gravity.END else Gravity.START
                }

                infoLayout.addView(tvPlayer)
                infoLayout.addView(tvMin)

                val ivIcon = ImageView(context).apply {
                    val size = (24 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(25, 0, 25, 0)
                    }
                }

                when (type) {

                    "goal" -> {
                        ivIcon.setImageResource(R.drawable.ic_ball)
                        ivIcon.clearColorFilter()
                    }

                    // забитий пенальті в матчі
                    "goal_pen" -> {
                        ivIcon.setImageResource(R.drawable.ic_penalty_goal)
                        ivIcon.clearColorFilter()
                    }

                    // забитий у післяматчевій серії
                    "penalty_goal" -> {
                        ivIcon.setImageResource(R.drawable.ic_penalty_goal)
                        ivIcon.setColorFilter(Color.parseColor("#00E676"))
                    }

                    // промах у матчі
                    "missed_penalty" -> {
                        ivIcon.setImageResource(R.drawable.ic_penalty_missed)
                        ivIcon.clearColorFilter()
                    }

                    // промах у післяматчевій серії
                    "penalty_miss" -> {
                        ivIcon.setImageResource(R.drawable.ic_penalty_missed)
                        ivIcon.setColorFilter(Color.RED)
                    }

                    "goal_og" -> {
                        ivIcon.setImageResource(R.drawable.ic_ball)
                        ivIcon.setColorFilter(Color.RED)
                    }

                    "yellow_card" -> {
                        ivIcon.setImageResource(R.drawable.ic_card)
                        ivIcon.setColorFilter(Color.parseColor("#FFEB3B"))
                    }

                    "red_card" -> {
                        ivIcon.setImageResource(R.drawable.ic_card)
                        ivIcon.setColorFilter(Color.RED)
                    }

                    "yellow_red" -> ivIcon.setImageResource(R.drawable.ic_second_yellow)

                    "substitution", "sub" -> {
                        ivIcon.setImageResource(R.drawable.ic_substitution)
                        ivIcon.clearColorFilter()
                    }
                }

                val emptySpace = View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }

                if (isHomeTeam) {
                    rowLayout.addView(infoLayout)
                    rowLayout.addView(ivIcon)
                    rowLayout.addView(emptySpace)
                } else {
                    rowLayout.addView(emptySpace)
                    rowLayout.addView(ivIcon)
                    rowLayout.addView(infoLayout)
                }
// --- ДОДАНИЙ БЛОК: Клік по події таймлайну ---
                rowLayout.setOnClickListener { view ->
                    if (playerId.isNotEmpty() && playerId != "0") {
                        val intent = android.content.Intent(view.context, PlayerProfileActivity::class.java)

                        intent.putExtra("PLAYER_ID", playerId)
                        intent.putExtra("PLAYER_NAME", playerName)

                        // Передаємо пусті значення, щоб екран не крашнувся
                        intent.putExtra("PLAYER_PHOTO", "")
                        intent.putExtra("PLAYER_NUMBER", "")
                        intent.putExtra("PLAYER_POSITION", "")
                        intent.putExtra("PLAYER_BIRTHDATE", "")
                        intent.putExtra("PLAYER_AGE", 0)
                        intent.putExtra("TEAM_NAME", "")
                        intent.putExtra("TEAM_LOGO", "")

                        view.context.startActivity(intent)
                    }
                }
                // --- КІНЕЦЬ ДОДАНОГО БЛОКУ ---
                container.addView(rowLayout)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
