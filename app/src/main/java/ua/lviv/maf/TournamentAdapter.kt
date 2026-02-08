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

    // ViewHolder тепер знає про існування іконки
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
        
        // ЛОГІКА ІКОНОК: підставляємо іконки залежно від тексту
        when (item.year) {
            "Прогнози (MAF Bet)" -> {
                // Твоя нова іконка
                holder.icon.setImageResource(R.drawable.ic_bet)
            }
            "Дискваліфікації" -> {
                // Системна іконка видалення/заборони
                holder.icon.setImageResource(android.R.drawable.ic_delete)
            }
            "Історія" -> {
                // Системна іконка книги
                holder.icon.setImageResource(android.R.drawable.ic_menu_book)
            }
            else -> {
                // Іконка кубка для сезонів
                holder.icon.setImageResource(android.R.drawable.btn_star_big_on)
            }
        }
        
        // Обробка натискання на всю картку
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
