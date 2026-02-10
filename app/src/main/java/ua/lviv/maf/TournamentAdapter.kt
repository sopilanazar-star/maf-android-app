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
        // Використовуємо теги, щоб знайти текстові поля в коді
        val team1Txt: TextView = card.findViewWithTag("t1")
        val team2Txt: TextView = card.findViewWithTag("t2")
        val scoreTxt: TextView = card.findViewWithTag("score")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        
        // Створюємо картку програмно, щоб не возитися з XML
        val card = CardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(-1, -2).apply {
                setMargins(20, 15, 20, 15)
            }
            radius = 30f
            setCardBackgroundColor(Color.parseColor("#22262B")) // Темний колір картки
            elevation = 8f

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(40, 50, 40, 50)
                gravity = Gravity.CENTER
            }

            // Назва команди 1
            val t1 = TextView(context).apply {
                tag = "t1"
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }

            // Рахунок по центру
            val score = TextView(context).apply {
                tag = "score"
                layoutParams = LinearLayout.LayoutParams(-2, -2)
                setTextColor(Color.WHITE)
                textSize = 28f
                setPadding(30, 0, 30, 0)
                setTypeface(null, Typeface.BOLD)
            }

            // Назва команди 2
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
        
        // Заповнюємо дані
        holder.team1Txt.text = item.team1
        holder.team2Txt.text = item.team2
        holder.scoreTxt.text = item.score

        // Обробка натискання
        holder.card.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
