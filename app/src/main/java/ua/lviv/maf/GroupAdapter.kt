package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 1. ПРИБРАЛИ onTeamClick З КОНСТРУКТОРА
class GroupAdapter(
    private var groups: List<GroupTable>
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupTitle: TextView = view.findViewById(R.id.tvGroupTitle)
        val rvStandingLeft: RecyclerView = view.findViewById(R.id.rvStandingLeft)
        val rvStandingRight: RecyclerView = view.findViewById(R.id.rvStandingRight)

        init {
            rvStandingLeft.layoutManager = LinearLayoutManager(view.context)
            rvStandingRight.layoutManager = LinearLayoutManager(view.context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.tvGroupTitle.text = group.groupName
        
        // 2. ПРОСТО ПЕРЕДАЄМО ДАНІ, БЕЗ ЛЯМБД ТА КЛІКІВ
        holder.rvStandingLeft.adapter = StandingLeftAdapter(group.teams)
        holder.rvStandingRight.adapter = StandingRightAdapter(group.teams) 
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<GroupTable>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
