package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
// Рекламні імпорти закоментовано для версії 2.3
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.AdView

class TournamentAdapter(private val items: List<TournamentRow>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemsWithAds: List<Any> = mutableListOf<Any>().apply {
        // У цій версії ми додаємо лише реальні матчі, без вставки "AD_SLOT"
        items.forEach { item ->
            add(item)
        }

        /* Стара логіка реклами закоментована для збереження структури:
        val AD_INTERVAL = 5
        var count = 0
        items.forEach { item ->
            add(item)
            count++
            if (count % AD_INTERVAL == 0) add("AD_SLOT")
        }
        if (!contains("AD_SLOT") && items.isNotEmpty()) add("AD_SLOT")
        */
    }

    private val TYPE_HEADER = 0
    private val TYPE_MATCH = 1
    private val TYPE_AD = 2

    // ВИПРАВЛЕНО: тепер використовуємо itemsWithAds для визначення типу
    override fun getItemViewType(position: Int): Int {
        val item = itemsWithAds[position]
        return when {
            item is String -> TYPE_AD
            (item as TournamentRow).isHeader -> TYPE_HEADER
            else -> TYPE_MATCH
        }
    }

    // ВИПРАВЛЕНО: додано створення AdViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> LeagueHeaderViewHolder(inflater.inflate(R.layout.item_league_header, parent, false))
            TYPE_AD -> AdViewHolder(inflater.inflate(R.layout.item_native_ad, parent, false))
            else -> MatchViewHolder(inflater.inflate(R.layout.item_match, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemOrAd = itemsWithAds[position]

        // 1. ЛОГІКА ДЛЯ РЕКЛАМИ (Вимкнено)
        if (holder is AdViewHolder) {
            // Оскільки "AD_SLOT" більше не додається, цей блок не має викликатися.
            // Ми просто повертаємо керування, щоб не було помилок.
            return
        }

        // Перетворюємо Any назад у TournamentRow для подальшої роботи
        val item = itemOrAd as TournamentRow

        if (holder is LeagueHeaderViewHolder) {
            holder.tvLeagueName.text = item.league.uppercase()

            if (item.stage.isNotEmpty()) {
                holder.tvStageName.visibility = View.VISIBLE
                holder.tvStageName.text = item.stage
                holder.tvStageName.setTextColor(Color.parseColor("#E30613"))
                holder.tvStageName.textSize = 12f
            } else {
                holder.tvStageName.visibility = View.GONE
            }

        } else if (holder is MatchViewHolder) {
            holder.tvTeam1.text = item.team1
            holder.tvTeam2.text = item.team2

            if (item.stadium.isNotEmpty()) {
                holder.tvStadium?.visibility = View.VISIBLE
                holder.tvStadium?.text = item.stadium
            } else {
                holder.tvStadium?.visibility = View.GONE
            }

            if (item.referee.isNotEmpty()) {
                holder.tvReferee?.visibility = View.VISIBLE
                holder.tvReferee?.text = "Арбітр: ${item.referee}"
            } else {
                holder.tvReferee?.visibility = View.GONE
            }

            val scoreValue = item.score?.trim() ?: ""
            holder.tvScore?.setTypeface(null, Typeface.BOLD)

            when {
                // --- ДОДАНО: Перевірка на технічний результат (має найвищий пріоритет) ---
                item.is_technical -> {
                    holder.ivTimeIcon?.visibility = View.GONE
                    holder.tvScore?.text = scoreValue
                    holder.tvScore?.setTextColor(Color.parseColor("#E30613")) // Червоний колір
                    holder.tvScore?.textSize = 18f
                }
                // --------------------------------------------------------------------------
                scoreValue.contains("'") || scoreValue == "HT" -> {
                    holder.ivTimeIcon?.visibility = View.GONE
                    holder.tvScore?.text = scoreValue
                    holder.tvScore?.setTextColor(Color.parseColor("#E30613"))
                    holder.tvScore?.textSize = 16f
                }
                scoreValue == "FT" -> {
                    holder.ivTimeIcon?.visibility = View.GONE
                    holder.tvScore?.text = "FT"
                    holder.tvScore?.setTextColor(Color.parseColor("#BCBCBC"))
                    holder.tvScore?.textSize = 16f
                }
                scoreValue.contains(" : ") -> {
                    holder.ivTimeIcon?.visibility = View.GONE
                    holder.tvScore?.text = scoreValue
                    holder.tvScore?.setTextColor(Color.WHITE)
                    holder.tvScore?.textSize = 18f
                }
                scoreValue.isNotEmpty() -> {
                    holder.ivTimeIcon?.visibility = View.VISIBLE
                    holder.tvScore?.text = scoreValue
                    holder.tvScore?.setTextColor(Color.parseColor("#BCBCBC"))
                    holder.tvScore?.textSize = 16f
                }
                else -> {
                    holder.ivTimeIcon?.visibility = View.VISIBLE
                    holder.tvScore?.text = "VS"
                    holder.tvScore?.setTextColor(Color.parseColor("#BCBCBC"))
                    holder.tvScore?.textSize = 16f
                }
            }

            Glide.with(holder.itemView.context).load(item.logo1).into(holder.ivLogo1)
            Glide.with(holder.itemView.context).load(item.logo2).into(holder.ivLogo2)

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, MatchDetailActivity::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("team1", item.team1)
                    putExtra("team2", item.team2)
                    putExtra("logo1", item.logo1)
                    putExtra("logo2", item.logo2)
                    putExtra("score", item.score)

                    // --- ДОДАНО ПЕРЕДАЧУ ТЕХНІЧНИХ ДАНИХ У КАРТКУ МАТЧУ ---
                    putExtra("is_technical", item.is_technical)
                    putExtra("technical_reason", item.technical_reason)
                    // ------------------------------------------------------

                    putExtra("league", item.league)
                    putExtra("stage", item.stage)
                    putExtra("date", item.date)
                    putExtra("stadium", item.stadium)
                    putExtra("referee", item.referee)
                    putExtra("home_team_id", item.home_team_id)
                    putExtra("away_team_id", item.away_team_id)
                }
                context.startActivity(intent)
            }
        }
    }

    // ВИПРАВЛЕНО: тепер рахуємо загальну кількість разом з рекламою
    override fun getItemCount() = itemsWithAds.size

    class LeagueHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeagueName: TextView = view.findViewById(R.id.tvLeagueName)
        val tvStageName: TextView = view.findViewById(R.id.tvStageName)
    }

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val tvScore: TextView? = view.findViewById(R.id.tvScore)
        val tvStadium: TextView? = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView? = view.findViewById(R.id.tvReferee)
        val ivTimeIcon: ImageView? = view.findViewById(R.id.ivTimeIcon)
    }

    // Клас для утримання рекламного банера (заглушка)
    class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Закоментовано через відсутність класів Google Ads
        // val adView: com.google.android.gms.ads.nativead.NativeAdView = view as com.google.android.gms.ads.nativead.NativeAdView
        val headline: TextView = view.findViewById(R.id.ad_headline)
        val body: TextView = view.findViewById(R.id.ad_body)
        val icon: ImageView = view.findViewById(R.id.ad_app_icon)
        val callToAction: android.widget.Button = view.findViewById(R.id.ad_call_to_action)
    }
}