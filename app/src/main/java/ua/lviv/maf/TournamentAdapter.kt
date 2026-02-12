package ua.lviv.maf

import android.content.Intent
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
            holder.tvLeagueName.text = item.league
            holder.tvStageName.text = item.stage
            holder.tvStageName.visibility = if (item.stage.isEmpty()) View.GONE else View.VISIBLE
        } else if (holder is MatchViewHolder) {
            holder.tvTeam1.text = item.team1
            holder.tvTeam2.text = item.team2
            
            // Заповнюємо нові поля у центрі картки
            holder.tvLeagueName?.text = item.league
            holder.tvStage?.text = item.stage
            
            holder.tvStadium?.text = item.stadium
            holder.tvReferee?.text = if (item.referee.isNotEmpty()) "Арбітр: ${item.referee}" else ""
            
            // Видимість полів
            holder.tvReferee?.visibility = if (item.referee.isNotEmpty()) View.VISIBLE else View.GONE
            holder.tvStage?.visibility = if (item.stage.isNotEmpty()) View.VISIBLE else View.GONE

            // --- ЛОГІКА РАХУНКУ ---
            if (item.score.contains(":")) {
                val parts = item.score.split(":")
                if (parts.size >= 2) {
                    holder.tvScore1?.text = parts[0].trim()
                    holder.tvScore2?.text = parts[1].trim()
                }
            } else {
                holder.tvScore1?.text = ""
                holder.tvScore2?.text = item.score
            }

            Glide.with(holder.itemView.context).load(item.logo1).into(holder.ivLogo1)
            Glide.with(holder.itemView.context).load(item.logo2).into(holder.ivLogo2)

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, MatchDetailActivity::class.java).apply {
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
                holder.itemView.context.startActivity(intent)
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
        
        val tvScore1: TextView? = view.findViewById(R.id.tvScore1)
        val tvScore2: TextView? = view.findViewById(R.id.tvScore2)
        
        // Додано tvLeagueName для відображення назви всередині картки
        val tvLeagueName: TextView? = view.findViewById(R.id.tvLeagueName)
        val tvStage: TextView? = view.findViewById(R.id.tvStage)
        val tvStadium: TextView? = view.findViewById(R.id.tvStadium)
        val tvReferee: TextView? = view.findViewById(R.id.tvReferee)
    }
}
