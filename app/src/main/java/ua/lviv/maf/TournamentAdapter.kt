package ua.lviv.maf

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TournamentAdapter(
    private val items: List<TournamentRow>,
    private val onClick: (TournamentRow) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {
        val t1 = card.findViewWithTag<TextView>("t1")
        val t2 = card.findViewWithTag<TextView>("t2")
        val score = card.findViewWithTag<TextView>("score")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val card = CardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(-1, -2).apply {
                setMargins(25, 20, 25, 20)
            }
            radius = 35f
            setCardBackgroundColor(Color.parseColor("#22262B"))
            elevation = 10f

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(40, 60, 40, 60)
                gravity = Gravity.CENTER
            }

            // Команда 1
            val txt1 = TextView(context).apply {
                tag = "t1"; setTextColor(Color.WHITE); textSize = 16f
                gravity = Gravity.CENTER; setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }
            // Рахунок
            val txtScore = TextView(context).apply {
                tag = "score"; setTextColor(Color.WHITE); textSize = 30f
                setPadding(35, 0, 35, 0); setTypeface(null, Typeface.BOLD)
            }
            // Команда 2
            val txt2 = TextView(context).apply {
                tag = "t2"; setTextColor(Color.WHITE); textSize = 16f
                gravity = Gravity.CENTER; setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }

            layout.addView(txt1); layout.addView(txtScore); layout.addView(txt2)
            addView(layout)
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.t1?.text = item.team1
        holder.t2?.text = item.team2
        holder.score?.text = item.score
        holder.card.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
