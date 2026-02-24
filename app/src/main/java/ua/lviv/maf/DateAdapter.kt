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
        val tvMonth: TextView = view.findViewById(R.id.tvMonth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val item = dates[position]
        holder.tvDayName.text = item.dayName
        holder.tvDayNum.text = item.dayNumber
        holder.tvMonth.text = item.month

        // Оновлюємо зовнішній вигляд залежно від того, чи вибрана дата
        if (item.isSelected) {
            // АКТИВНИЙ СТАН: Білий текст на червоному фоні
            holder.container.setBackgroundResource(R.drawable.bg_date_selected)
            holder.tvDayName.setTextColor(Color.parseColor("#EEEEEE")) 
            holder.tvDayNum.setTextColor(Color.WHITE)
            holder.tvMonth.setTextColor(Color.WHITE)
        } else {
            // НЕАКТИВНИЙ СТАН: Сірі відтінки
            holder.container.setBackgroundColor(Color.TRANSPARENT)
            holder.tvDayName.setTextColor(Color.GRAY)
            holder.tvDayNum.setTextColor(Color.parseColor("#CCCCCC"))
            holder.tvMonth.setTextColor(Color.GRAY)
        }

        holder.itemView.setOnClickListener {
            // При ручному кліку оновлюємо виділення
            updateSelection(position)
            // Викликаємо колбек (перегортаємо ViewPager)
            onDateSelected(item.date)
        }
    }

    override fun getItemCount() = dates.size

    /**
     * 🔥 НОВИЙ МЕТОД: дозволяє MainActivity оновлювати вибрану дату при свайпі.
     * Нічого не видаляємо, просто додаємо функціонал синхронізації.
     */
    fun updateSelection(newPosition: Int) {
        if (newPosition !in dates.indices) return

        // Знімаємо виділення з усіх і ставимо тільки на нову позицію
        dates.forEachIndexed { index, dateModel ->
            dateModel.isSelected = (index == newPosition)
        }
        
        // Повідомляємо адаптер, що дані змінилися, щоб він перемалював кольори
        notifyDataSetChanged()
    }
}
