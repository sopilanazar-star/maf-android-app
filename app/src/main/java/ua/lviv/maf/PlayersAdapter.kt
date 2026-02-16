package ua.lviv.maf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.lviv.maf.models.Player

class PlayersAdapter(
    private val items: List<Any>,
    private val onPlayerClick: (Player) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PLAYER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_PLAYER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            // Створюємо заголовок програмно (без XML)
            val textView = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(-1, -2)
                setPadding(40, 30, 40, 30)
                setBackgroundColor(Color.parseColor("#252830"))
                setTextColor(Color.parseColor("#00E676"))
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAllCaps = true
            }
            HeaderViewHolder(textView)
        } else {
            // Картка гравця (можна використовувати item_player.xml або створити тут)
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
            PlayerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_HEADER) {
            (holder as HeaderViewHolder).bind(items[position] as String)
        } else {
            (holder as PlayerViewHolder).bind(items[position] as Player, onPlayerClick)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(title: String) { (itemView as TextView).text = title }
    }

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // У тебе має бути файл res/layout/item_player.xml
        // Якщо його немає, створи його з ID: tvPlayerName, tvPlayerNumber, tvPlayerPosition, ivPlayerPhoto
        private val tvName: TextView = view.findViewById(R.id.tvPlayerName)
        private val tvNumber: TextView = view.findViewById(R.id.tvPlayerNumber)
        private val tvPosition: TextView = view.findViewById(R.id.tvPlayerPosition)
        private val ivPhoto: ImageView = view.findViewById(R.id.ivPlayerPhoto)

        fun bind(player: Player, onClick: (Player) -> Unit) {
            tvName.text = player.name
            tvNumber.text = if (player.number.isNotEmpty()) "#${player.number}" else ""
            tvPosition.text = player.position

            if (player.photo.isNotEmpty()) {
                Glide.with(itemView.context).load(player.photo).circleCrop().into(ivPhoto)
            } else {
                ivPhoto.setImageResource(android.R.drawable.ic_menu_myplaces) // Або твоя заглушка
            }

            itemView.setOnClickListener { onClick(player) }
        }
    }
}
