package ua.lviv.maf.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.lviv.maf.R
import ua.lviv.maf.models.PredictionTablePlayer

class PredictionTableAdapter(
    private val players: List<PredictionTablePlayer>,
    private val onClick: (PredictionTablePlayer) -> Unit
) : RecyclerView.Adapter<PredictionTableAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val tvStats: TextView = view.findViewById(R.id.tvStats)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prediction_table, parent, false)

        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {

        val player = players[position]

        holder.tvPosition.text = player.position.toString()
        holder.tvName.text = player.name
        holder.tvPoints.text = player.points.toString()

        holder.tvStats.text =
            "🎯${player.exact}   ✔${player.correct}   ✖${player.wrong}"

        holder.itemView.setOnClickListener {
            onClick(player)
        }
    }

    override fun getItemCount(): Int = players.size
}
