package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TournamentAdapter(private val matches: List<TournamentRow>) :
    RecyclerView.Adapter<TournamentAdapter.MatchViewHolder>() {

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeam1: TextView = view.findViewById(R.id.tvTeam1)
        val tvTeam2: TextView = view.findViewById(R.id.tvTeam2)
        val ivLogo1: ImageView = view.findViewById(R.id.ivLogo1)
        val ivLogo2: ImageView = view.findViewById(R.id.ivLogo2)
        val tvScore1: TextView = view.findViewById(R.id.tvScore1) // Нове поле для голів команди 1
        val tvScore2: TextView = view.findViewById(R.id.tvScore2) // Нове поле для голів команди 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.tvTeam1.text = match.team1
        holder.tvTeam2.text = match.team2

        // Логіка розділення рахунку "3 : 4" на дві окремі цифри
        if (match.score.contains(":")) {
            val scores = match.score.split(":")
            if (scores.size == 2) {
                holder.tvScore1.text = scores[0].trim()
                holder.tvScore2.text = scores[1].trim()
            }
        } else {
            // Якщо рахунку ще немає (наприклад, там час "17:00" або "vs")
            holder.tvScore1.text = ""
            holder.tvScore2.text = match.score
        }

        // Завантаження логотипів через Glide
        Glide.with(holder.itemView.context)
            .load(match.logo1)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.ivLogo1)

        Glide.with(holder.itemView.context)
            .load(match.logo2)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.ivLogo2)
    }

    override fun getItemCount() = matches.size
}
