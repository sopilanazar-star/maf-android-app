package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TournamentAdapter(private val items: List<TournamentRow>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Константи для типів рядків
    private val VIEW_TYPE_LEAGUE = 0
    private val VIEW_TYPE_MATCH = 1

    // Визначаємо, який тип рядка малювати
    override fun getItemViewType(position: Int): Int {
        return if (items[position].isHeader) VIEW_TYPE_LEAGUE else VIEW_TYPE_MATCH
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_LEAGUE) {
            val view = inflater.inflate(R.layout.item_league_header, parent, false)
            LeagueHeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_match, parent, false)
            MatchViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is LeagueHeaderViewHolder) {
            // Малюємо заголовок (Назва ліги + Етап)
            holder.tvLeagueName.text = item.league
            holder.tvStageName.text = item.stage
        } else if (holder is MatchViewHolder) {
            // Малюємо звичайний матч
            holder.tvTeam1.text = item.team1
            holder.tvTeam2.text = item.team2

            // Розділяємо рахунок
            if (item.score.contains(":")) {
                val scores = item.score.split(":")
                if (scores.size == 2) {
                    holder.tvScore1.text = scores[0].trim()
                    holder.tvScore2.text = scores[1].trim()
                }
            } else {
                holder.tvScore1.text = ""
                holder.tvScore2.text = item.score
            }

            // Завантажуємо логотипи
            Glide.with(holder.itemView.context).load(item.logo1).into(holder.ivLogo1)
            Glide.with(holder.itemView.context).load(item.logo2).into(holder.ivLogo2)
        }
    }

    override fun getItemCount() = items.size

    // ViewHolder для заголовка ліги
    class LeagueHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeagueName: TextView = view.findViewById(R.id.tvLeagueName)
        val tvStageName: TextView = view.findViewById(R.id.tvStageName)
    }

    // ViewHolder для самого матчу
    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvScore1: TextView = view.findViewById(R.id.tvScore1)
        val tvScore2: TextView = view.findViewById(R.id.tvScore2)
    }
}
