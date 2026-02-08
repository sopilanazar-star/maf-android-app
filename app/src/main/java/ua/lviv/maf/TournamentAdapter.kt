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
        val year: TextView = view.findViewById(R.id.textYear)
        val winner: TextView = view.findViewById(R.id.textWinner)
        val icon: ImageView = view.findViewById(R.id.itemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.year.text = item.year
        holder.winner.text = item.winner
        
        // Встановлюємо іконки для кожного пункту розділу "Більше"
        when (item.year) {
            "Прогнози (MAF Bet)" -> {
                // Використовуємо твою іконку ic_bet.png
                holder.icon.setImageResource(R.drawable.ic_bet)
            }
            "Дискваліфікації" -> {
                // Стандартна системна іконка (кошик або попередження)
                holder.icon.setImageResource(android.R.drawable.ic_delete)
            }
            "Історія" -> {
                // Використовуємо ic_menu_search або ic_menu_info_details (вони стабільні)
                holder.icon.setImageResource(android.R.drawable.ic_menu_info_details)
            }
            else -> {
                // Для звичайних турнірів залишаємо кубок або лого
                holder.icon.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
