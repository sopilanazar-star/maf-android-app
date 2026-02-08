package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TournamentAdapter(
    private val items: List<TournamentRow>,
    private val onItemClick: (TournamentRow) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textYear)
        val subtitle: TextView = view.findViewById(R.id.textWinner)
        val icon: ImageView = view.findViewById(R.id.itemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.year
        holder.subtitle.text = item.winner
        
        // Логіка іконок для нового дизайну
        when {
            item.year.contains("Прогнози") -> holder.icon.setImageResource(R.drawable.ic_bet) // Перевір, чи є ця іконка!
            item.year.contains("Дискваліфікації") -> holder.icon.setImageResource(android.R.drawable.ic_delete)
            item.year.contains("Історія") -> holder.icon.setImageResource(android.R.drawable.ic_menu_recent_history)
            item.winner.contains("Команда:") -> {
                // Це гравець у бані
                holder.icon.setImageResource(android.R.drawable.ic_dialog_alert)
            }
            else -> holder.icon.setImageResource(android.R.drawable.ic_menu_sort_by_size)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
