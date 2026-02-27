package ua.lviv.maf

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StandingRightAdapter(private val items: List<StandingRow>) : 
    RecyclerView.Adapter<StandingRightAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGames: TextView = view.findViewById(R.id.tvGames)
        val tvWins: TextView = view.findViewById(R.id.tvWins)
        val tvDraws: TextView = view.findViewById(R.id.tvDraws)
        val tvLosses: TextView = view.findViewById(R.id.tvLosses)
        val tvGoalsDiff: TextView = view.findViewById(R.id.tvGoalsDiff)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val layoutForm: LinearLayout = view.findViewById(R.id.layoutForm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_right, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Заповнюємо цифри з твого StandingRow
        holder.tvGames.text = item.games.toString()
        holder.tvWins.text = item.win.toString()
        holder.tvDraws.text = item.draw.toString()
        holder.tvLosses.text = item.loss.toString()
        
        // Різниця м'ячів (Забиті - Пропущені)
        holder.tvGoalsDiff.text = "${item.goals_for}-${item.goals_against}"
        
        holder.tvPoints.text = item.points.toString()

        // Очищуємо і малюємо форму (кружечки)
        holder.layoutForm.removeAllViews()
        item.form.forEach { result ->
            val circle = createFormView(holder.itemView.context, result)
            holder.layoutForm.addView(circle)
        }
    }

    override fun getItemCount() = items.size

    // Допоміжна функція для створення кружечків форми (W, D, L)
    private fun createFormView(context: android.content.Context, result: String): View {
        val tv = TextView(context)
        val params = LinearLayout.LayoutParams(
            (20 * context.resources.displayMetrics.density).toInt(),
            (20 * context.resources.displayMetrics.density).toInt()
        )
        params.setMargins(2, 0, 2, 0)
        tv.layoutParams = params
        tv.gravity = Gravity.CENTER
        tv.textSize = 10sp
        tv.setTextColor(Color.WHITE)
        
        when (result.uppercase()) {
            "W", "В" -> {
                tv.text = "В"
                tv.setBackgroundResource(R.drawable.bg_form_win) // Створи такий drawable (зелений)
            }
            "D", "Н" -> {
                tv.text = "Н"
                tv.setBackgroundResource(R.drawable.bg_form_draw) // (сірий)
            }
            "L", "П" -> {
                tv.text = "П"
                tv.setBackgroundResource(R.drawable.bg_form_loss) // (червоний)
            }
        }
        return tv
    }
}
