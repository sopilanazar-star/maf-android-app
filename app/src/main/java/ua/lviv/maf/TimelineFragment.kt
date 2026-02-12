package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
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
    private var homeTeamId: String? = null // Зберігаємо ID домашньої команди

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

        if (container != null) {
            loadTimelineData(matchId, container, tvLoading, centerLine)
        }
    }

    private fun loadTimelineData(matchId: String, container: LinearLayout, tvLoading: TextView?, centerLine: View?) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/match-details?id=$matchId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    tvLoading?.text = "Помилка мережі"
                }
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
            // Використовуємо "timeline" як у вашому коді
            val timeline = root.optJSONArray("timeline") ?: return

            container.removeAllViews()

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
                val type = event.optString("type")
                val eventTeamId = event.optString("team_id")

                // Логіка визначення сторони (якщо ID збігається з домашнім - зліва)
                val isHomeTeam = eventTeamId == homeTeamId

                val rowLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 20, 0, 20)
                }

                // --- 1. Текстовий блок (Гравець + Хвилина) ---
                val infoLayout = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    orientation = LinearLayout.VERTICAL
                    gravity = if (isHomeTeam) Gravity.END else Gravity.START
                }

                val tvPlayer = TextView(context).apply {
                    text = if (type == "goal_og") "$playerName (АГ)" else playerName
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                }

                val tvMin = TextView(context).apply {
                    text = "$minute'"
                    setTextColor(Color.parseColor("#BCBCBC"))
                    textSize = 12f
                }

                infoLayout.addView(tvPlayer)
                infoLayout.addView(tvMin)

                // --- 2. Іконка події (по центру) ---
                val ivIcon = ImageView(context).apply {
                    val size = (24 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(25, 0, 25, 0)
                    }
                }

                // Вибір іконки
                when (type) {
                    "goal" -> {
                        ivIcon.setImageResource(android.R.drawable.ic_btn_speak_now)
                        ivIcon.setColorFilter(Color.WHITE)
                    }
                    "goal_og" -> {
                        ivIcon.setImageResource(android.R.drawable.ic_btn_speak_now)
                        ivIcon.setColorFilter(Color.RED)
                    }
                    "yellow_card" -> {
                        ivIcon.setImageResource(android.R.drawable.checkbox_on_background)
                        ivIcon.setColorFilter(Color.YELLOW)
                    }
                    "red_card" -> {
                        ivIcon.setImageResource(android.R.drawable.checkbox_on_background)
                        ivIcon.setColorFilter(Color.RED)
                    }
                    "substitution" -> {
                        ivIcon.setImageResource(android.R.drawable.stat_notify_sync)
                        ivIcon.setColorFilter(Color.GREEN)
                    }
                    else -> {
                        ivIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                        ivIcon.setColorFilter(Color.GRAY)
                    }
                }

                // --- 3. Порожній блок для симетрії ---
                val emptySpace = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                }

                // --- Формування черговості залежно від сторони ---
                if (isHomeTeam) {
                    rowLayout.addView(infoLayout)  // Текст зліва
                    rowLayout.addView(ivIcon)      // Іконка в центрі
                    rowLayout.addView(emptySpace)  // Порожнеча справа
                } else {
                    rowLayout.addView(emptySpace)  // Порожнеча зліва
                    rowLayout.addView(ivIcon)      // Іконка в центрі
                    rowLayout.addView(infoLayout)  // Текст справа
                }

                container.addView(rowLayout)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
