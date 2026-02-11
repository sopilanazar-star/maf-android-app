package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DateAdapter(
    private val dates: List<DateModel>,
    private val onDateSelected: (String) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.dateContainer)
        val tvDayName: TextView = view.findViewById(R.id.tvDayName)
        val tvDayNum: TextView = view.findViewById(R.id.tvDayNum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val item = dates[position]
        holder.tvDayName.text = item.dayName
        holder.tvDayNum.text = item.dayNumber

        // UEFA Style: Якщо дата обрана — виділяємо червоним
        if (item.isSelected) {
            holder.tvDayName.setTextColor(Color.parseColor("#E30613"))
            holder.tvDayNum.setTextColor(Color.WHITE)
            holder.container.setBackgroundResource(R.drawable.bg_date_selected)
        } else {
            holder.tvDayName.setTextColor(Color.GRAY)
            holder.tvDayNum.setTextColor(Color.WHITE)
            holder.container.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            // Знімаємо виділення з усіх і ставимо на поточну
            dates.forEach { it.isSelected = false }
            item.isSelected = true
            notifyDataSetChanged()
            
            // Викликаємо фільтрацію в MainActivity
            onDateSelected(item.date)
        }
    }

    override fun getItemCount() = dates.size
}
