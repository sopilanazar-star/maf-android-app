package ua.lviv.maf 

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.lviv.maf.R // Твій ресурсний пакет
import ua.lviv.maf.models.Player // Твоя модель даних

class PlayersAdapter(private var players: List<Player>) : 
    RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvPlayerNumber)
        val tvName: TextView = view.findViewById(R.id.tvPlayerName)
        val tvPosition: TextView = view.findViewById(R.id.tvPlayerPosition)
        val ivPhoto: ImageView = view.findViewById(R.id.ivPlayerPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]

        // Ім'я та номер
        holder.tvName.text = player.name
        holder.tvNumber.text = if (player.number.isNullOrEmpty()) "—" else player.number

        // Перекладаємо позиції з бази даних
        holder.tvPosition.text = when (player.position.lowercase()) {
            "g", "gk" -> "Воротар"
            "d", "df" -> "Захисник"
            "m", "mf" -> "Півзахисник"
            "f", "fw" -> "Нападник"
            else -> "Гравець"
        }

        // Завантаження фото через Glide
        if (player.photo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(player.photo)
                .placeholder(R.drawable.ic_player_placeholder) // Заглушка, поки вантажиться
                .error(R.drawable.ic_player_placeholder)       // Якщо фото не знайдено
                .circleCrop() // Робимо фото круглим
                .into(holder.ivPhoto)
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_player_placeholder)
        }
    }

    override fun getItemCount() = players.size

    // Метод для оновлення списку (якщо дані прийдуть пізніше)
    fun updateData(newPlayers: List<Player>) {
        this.players = newPlayers
        notifyDataSetChanged()
    }
}
