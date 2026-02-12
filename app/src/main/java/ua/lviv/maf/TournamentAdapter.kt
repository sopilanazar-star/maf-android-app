package ua.lviv.maf

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TournamentAdapter(private val items: List<TournamentRow>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = if (items[position].isHeader) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            LeagueHeaderViewHolder(inflater.inflate(R.layout.item_league_header, parent, false))
        } else {
            MatchViewHolder(inflater.inflate(R.layout.item_match, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is LeagueHeaderViewHolder) {
            holder.tvLeagueName.apply {
                text = item.league.uppercase() 
                textSize = 12f
                setTextColor(Color.parseColor("#BCBCBC"))
            }
            holder.tvStageName.apply {
                text = item.stage
                textSize = 11f
                setTextColor(Color.parseColor("#E30613"))
                visibility = if (item.stage.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

        } else if (holder is MatchViewHolder) {
            holder.tvTeam1.text = item.team1
            holder.tvTeam2.text = item.team2
            holder.tvStadium?.text = item.stadium
            
            if (item.referee.isNullOrEmpty()) {
                holder.tvReferee?.visibility = View.GONE
            } else {
                holder.tvReferee?.visibility = View.VISIBLE
                holder.tvReferee?.text = "Арбітр: ${item.referee}"
            }

            // --- ЛОГІКА РАХУНКУ ТА ГОДИННИКА ---
            holder.tvScore?.apply {
                val scoreValue = item.score ?: ""
                text = scoreValue

                // Якщо це час (наприклад 11:00)
                if (scoreValue.contains(":") && scoreValue.length <= 5) {
                    setTextColor(Color.parseColor("#BCBCBC"))
                    setTypeface(null, Typeface.NORMAL)
                    textSize = 14f
                    
                    // Додаємо іконку годинника
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time, 0, 0, 0)
                    compoundDrawablePadding = 12
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#BCBCBC"))
                    }
                } else {
                    // Якщо це результат (наприклад 2 : 1)
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    textSize = 18f
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) // Прибираємо іконку
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
                    putExtra("league", item.league)
                    putExtra("stage", item.stage)
                    putExtra("date", item.date)
                    putExtra("stadium", item.stadium)
                    putExtra("referee", item.referee)
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

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val tvScore: TextView? = view.findViewById(R.id.tvScore)
        val tvStadium: TextView? = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView? = view.findViewById(R.id.tvReferee)
    }
}
