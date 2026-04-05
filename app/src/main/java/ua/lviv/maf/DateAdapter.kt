package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// import ua.lviv.maf.AdInterceptor

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


        holder.tvMonth.visibility = View.GONE

        if (item.isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_date_selected)

            holder.tvDayName.setTextColor(Color.WHITE)
            holder.tvDayNum.setTextColor(Color.WHITE)
            holder.tvMonth.setTextColor(Color.WHITE)

        } else {
            holder.container.setBackgroundColor(Color.TRANSPARENT)

            val gray = Color.parseColor("#B0B0B0")

            holder.tvDayName.setTextColor(gray)
            holder.tvDayNum.setTextColor(gray)
            holder.tvMonth.setTextColor(gray)
        }

        holder.itemView.setOnClickListener {
            // Тимчасово відключаємо перехоплювач реклами, виконуємо лише логіку вибору
            // AdInterceptor.execute(holder.itemView.context) {
            dates.forEach { it.isSelected = false }
            item.isSelected = true
            notifyDataSetChanged()
            onDateSelected(item.date)
            // }
        }
    }

    override fun getItemCount() = dates.size
}
