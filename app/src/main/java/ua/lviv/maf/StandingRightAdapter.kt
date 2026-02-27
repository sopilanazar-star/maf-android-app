package ua.lviv.maf

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StandingRightAdapter(
    private val items: List<StandingRow>,
    private val onItemClick: (StandingRow) -> Unit
) : RecyclerView.Adapter<StandingRightAdapter.ViewHolder>() {

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

        holder.tvGames.text = item.games.toString()
        holder.tvWins.text = item.win.toString()
        holder.tvDraws.text = item.draw.toString()
        holder.tvLosses.text = item.loss.toString()
        holder.tvGoalsDiff.text = "${item.goals_for}-${item.goals_against}"
        holder.tvPoints.text = item.points.toString()

        holder.layoutForm.removeAllViews()
        item.form?.forEach { result ->
            val circle = createFormView(holder.itemView.context, result)
            holder.layoutForm.addView(circle)
        }

        // Обробка кліку
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    private fun createFormView(context: Context, result: String): View {
        val tv = TextView(context)
        val density = context.resources.displayMetrics.density
        val size = (20 * density).toInt()
        val params = LinearLayout.LayoutParams(size, size)
        params.setMargins((2 * density).toInt(), 0, (2 * density).toInt(), 0)
        
        tv.layoutParams = params
        tv.gravity = Gravity.CENTER
        tv.textSize = 10f
        tv.setTextColor(Color.WHITE)
        tv.typeface = android.graphics.Typeface.DEFAULT_BOLD

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL

        when (result.uppercase()) {
            "W", "В" -> {
                tv.text = "В"
                shape.setColor(Color.parseColor("#4CAF50"))
            }
            "D", "Н" -> {
                tv.text = "Н"
                shape.setColor(Color.parseColor("#757575"))
            }
            "L", "П" -> {
                tv.text = "П"
                shape.setColor(Color.parseColor("#F44336"))
            }
            else -> {
                tv.text = result
                shape.setColor(Color.LTGRAY)
            }
        }
        
        tv.background = shape
        return tv
    }
}
