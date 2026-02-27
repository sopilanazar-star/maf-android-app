package ua.lviv.maf

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StandingLeftAdapter(
    private val items: List<StandingRow>
) : RecyclerView.Adapter<StandingLeftAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val ivTeamLogo: ImageView = view.findViewById(R.id.ivTeamLogo)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val positionMarker: View = view.findViewById(R.id.positionMarker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_left, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPosition.text = item.position.toString()
        holder.tvTeamName.text = item.team_name

        Glide.with(holder.itemView.context)
            .load(item.logo)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivTeamLogo)

        // ТВІЙ ОРИГІНАЛЬНИЙ РОБОЧИЙ ПЕРЕХІД
        holder.itemView.setOnClickListener {
            if (item.team_id.isNotEmpty() && item.team_id != "0") {
                val intent = Intent(it.context, TeamPlayersActivity::class.java)
                intent.putExtra("team_id", item.team_id)
                intent.putExtra("team_name", item.team_name)
                intent.putExtra("team_logo", item.logo)
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = items.size
}
