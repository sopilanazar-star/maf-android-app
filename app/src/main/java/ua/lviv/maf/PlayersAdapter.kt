package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.lviv.maf.models.Player

class PlayersAdapter(private val players: List<Player>) :
    RecyclerView.Adapter<PlayersAdapter.PlayerVH>() {

    class PlayerVH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerVH {
        val tv = TextView(parent.context)
        tv.setPadding(20, 20, 20, 20)
        tv.textSize = 16f
        tv.setTextColor(Color.WHITE)
        return PlayerVH(tv)
    }

    override fun getItemCount() = players.size

    override fun onBindViewHolder(holder: PlayerVH, position: Int) {
        val p = players[position]

        val num = if (!p.number.isNullOrEmpty()) "${p.number}. " else ""
        val pos = if (!p.position.isNullOrEmpty()) " (${p.position})" else ""

        holder.tv.text = num + p.name + pos
    }
}
