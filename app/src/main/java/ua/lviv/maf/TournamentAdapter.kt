package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TournamentAdapter(private val items: List<TournamentRow>) :
    RecyclerView.Adapter<TournamentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val year: TextView = view.findViewById(R.id.textYear)
        val winner: TextView = view.findViewById(R.id.textWinner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.year.text = "Сезон ${item.year}"
        holder.winner.text = "🏆 Чемпіон: ${item.winner}"
    }

    override fun getItemCount() = items.size
}
