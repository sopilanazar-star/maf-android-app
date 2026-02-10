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
        val team1Txt = card.findViewWithTag<TextView>("t1")
        val team2Txt = card.findViewWithTag<TextView>("t2")
        val scoreTxt = card.findViewWithTag<TextView>("score")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val card = CardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(-1, -2).apply {
                setMargins(20, 15, 20, 15)
            }
            radius = 30f
            setCardBackgroundColor(Color.parseColor("#22262B"))
            elevation = 8f

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                padding(40, 50, 40, 50)
                gravity = Gravity.CENTER
            }

            // Команда 1
            val t1 = TextView(context).apply {
                tag = "t1"
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }

            // Рахунок
            val score = TextView(context).apply {
                tag = "score"
                layoutParams = LinearLayout.LayoutParams(-2, -2)
                setTextColor(Color.WHITE)
                textSize = 28f
                setPadding(30, 0, 30, 0)
                setTypeface(null, Typeface.BOLD)
            }

            // Команда 2
            val t2 = TextView(context).apply {
                tag = "t2"
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }

            layout.addView(t1)
            layout.addView(score)
            layout.addView(t2)
            addView(layout)
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.team1Txt.text = item.team1
        holder.team2Txt.text = item.team2
        holder.scoreTxt.text = item.score
        holder.card.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    private fun android.view.View.padding(l: Int, t: Int, r: Int, b: Int) = setPadding(l, t, r, b)
}
