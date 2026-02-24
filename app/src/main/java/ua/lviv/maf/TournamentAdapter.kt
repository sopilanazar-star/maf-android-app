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

class TournamentAdapter(private val items: List<TournamentRow>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int =
        if (items[position].isHeader) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            LeagueHeaderViewHolder(
                inflater.inflate(R.layout.item_league_header, parent, false)
            )
        } else {
            TournamentMatchViewHolder(
                inflater.inflate(R.layout.item_match, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

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

        } else if (holder is TournamentMatchViewHolder) {

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

            // 🔥 НОВА ПРАВИЛЬНА ЛОГІКА ВИВОДУ
            val scoreValue = item.score ?: ""

            holder.tvScore?.text = scoreValue

            when {

                // 🟢 Рахунок
                scoreValue.contains(" : ") -> {
                    holder.ivTimeIcon.visibility = View.GONE
                    holder.tvScore?.apply {
                        setTextColor(Color.WHITE)
                        textSize = 18f
                        setTypeface(null, Typeface.BOLD)
                    }
                }

                // 🔴 LIVE хвилини
                scoreValue.contains("'") -> {
                    holder.ivTimeIcon.visibility = View.VISIBLE
                    holder.tvScore?.apply {
                        setTextColor(Color.RED)
                        textSize = 16f
                        setTypeface(null, Typeface.BOLD)
                    }
                }

                // 🟡 Перерва
                scoreValue == "HT" -> {
                    holder.ivTimeIcon.visibility = View.VISIBLE
                    holder.tvScore?.apply {
                        setTextColor(Color.YELLOW)
                        textSize = 16f
                        setTypeface(null, Typeface.BOLD)
                    }
                }

                // ⚫ Час початку
                else -> {
                    holder.ivTimeIcon.visibility = View.VISIBLE
                    holder.tvScore?.apply {
                        setTextColor(Color.parseColor("#BCBCBC"))
                        textSize = 14f
                        setTypeface(null, Typeface.NORMAL)
                    }
                }
            }

            Glide.with(holder.itemView.context)
                .load(item.logo1)
                .into(holder.ivLogo1)

            Glide.with(holder.itemView.context)
                .load(item.logo2)
                .into(holder.ivLogo2)

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, MatchDetailActivity::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("team1", item.team1)
                    putExtra("team2", item.team2)
                    putExtra("logo1", item.logo1)
                    putExtra("logo2", item.logo2)
                    putExtra("score", item.score)
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

    override fun getItemCount() = items.size

    class LeagueHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeagueName: TextView = view.findViewById(R.id.tvLeagueName)
        val tvStageName: TextView = view.findViewById(R.id.tvStageName)
    }

    class TournamentMatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val tvScore: TextView? = view.findViewById(R.id.tvScore)
        val tvStadium: TextView? = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView? = view.findViewById(R.id.tvReferee)
        val ivTimeIcon: ImageView = view.findViewById(R.id.ivTimeIcon)
    }
}
