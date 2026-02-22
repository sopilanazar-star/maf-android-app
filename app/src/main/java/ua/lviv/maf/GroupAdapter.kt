package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(private var groups: List<GroupTable>) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupTitle: TextView = view.findViewById(R.id.tvGroupTitle)
        val rvInnerTeams: RecyclerView = view.findViewById(R.id.rvInnerTeams)

        init {
            rvInnerTeams.layoutManager = LinearLayoutManager(view.context)
            rvInnerTeams.setRecycledViewPool(viewPool)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.tvGroupTitle.text = group.groupName
        
        // Внутрішній адаптер
        val teamAdapter = TeamAdapter(group.teams)
        holder.rvInnerTeams.adapter = teamAdapter
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<GroupTable>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
