package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.lviv.maf.models.Player

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

        // ІМ'Я
        holder.tvName.text = player.name ?: ""

        // НОМЕР
        holder.tvNumber.text =
            if (player.number.isNullOrEmpty()) "—" else player.number

        // ПОЗИЦІЯ (головний фікс)
        val posRaw = player.position?.trim()?.lowercase() ?: ""

        val posText = when (posRaw) {
            "g", "gk" -> "Воротар"
            "d", "df" -> "Захисник"
            "m", "mf" -> "Півзахисник"
            "f", "fw" -> "Нападник"
            else -> ""
        }

        holder.tvPosition.text = posText

        // ФОТО
        if (!player.photo.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(player.photo)
                .placeholder(R.drawable.ic_player_placeholder)
                .error(R.drawable.ic_player_placeholder)
                .circleCrop()
                .into(holder.ivPhoto)
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_player_placeholder)
        }
    }

    override fun getItemCount() = players.size

    fun updateData(newPlayers: List<Player>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}
