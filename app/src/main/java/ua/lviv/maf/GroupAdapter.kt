package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(private var groups: List<GroupTable>) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    // Створюємо два пули для перевикористання в'юшок (для лівої та правої частини)
    private val leftPool = RecyclerView.RecycledViewPool()
    private val rightPool = RecyclerView.RecycledViewPool()

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupTitle: TextView = view.findViewById(R.id.tvGroupTitle)
        
        // Два RecyclerView для фіксованої та скролимої частин
        val rvStandingLeft: RecyclerView = view.findViewById(R.id.rvStandingLeft)
        val rvStandingRight: RecyclerView = view.findViewById(R.id.rvStandingRight)

        init {
            // Налаштування лівого списку (назви команд)
            rvStandingLeft.layoutManager = LinearLayoutManager(view.context)
            rvStandingLeft.setRecycledViewPool(leftPool)
            rvStandingLeft.isNestedScrollingEnabled = false // Важливо для NestedScrollView

            // Налаштування правого списку (статистика: І, В, Н, П...)
            rvStandingRight.layoutManager = LinearLayoutManager(view.context)
            rvStandingRight.setRecycledViewPool(rightPool)
            rvStandingRight.isNestedScrollingEnabled = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        // Використовуємо твій файл розмітки картки групи
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.tvGroupTitle.text = group.groupName
        
        // 1. Встановлюємо адаптер для лівої (фіксованої) частини
        // Передаємо список команд поточної групи
        val leftAdapter = StandingLeftAdapter(group.teams)
        holder.rvStandingLeft.adapter = leftAdapter

        // 2. Встановлюємо адаптер для правої (скролимої) частини
        // Саме тут будуть колонки І, В, Н, П, +/-, Оч, Форма
        val rightAdapter = StandingRightAdapter(group.teams)
        holder.rvStandingRight.adapter = rightAdapter
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<GroupTable>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
