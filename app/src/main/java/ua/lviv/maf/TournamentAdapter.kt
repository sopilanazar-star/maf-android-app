package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Переконайся, що Glide додано в build.gradle

class TournamentAdapter(private var matches: List<TournamentRow>) :
    RecyclerView.Adapter<TournamentAdapter.MatchViewHolder>() {

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val team1: TextView = view.findViewById(R.id.tvTeam1)
        val team2: TextView = view.findViewById(R.id.tvTeam2)
        val score: TextView = view.findViewById(R.id.tvScore)
        val date: TextView = view.findViewById(R.id.tvDate)
        val logo1: ImageView = view.findViewById(R.id.ivLogo1)
        val logo2: ImageView = view.findViewById(R.id.ivLogo2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false) // Твій XML файл рядка
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        holder.team1.text = match.team1
        holder.team2.text = match.team2
        holder.score.text = match.score
        holder.date.text = match.date

        // Завантаження логотипів через Glide
        if (match.logo1.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(match.logo1)
                .into(holder.logo1)
        }

        if (match.logo2.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(match.logo2)
                .into(holder.logo2)
        }
    }

    override fun getItemCount() = matches.size

    fun updateData(newMatches: List<TournamentRow>) {
        this.matches = newMatches
        notifyDataSetChanged()
    }
    }
    
