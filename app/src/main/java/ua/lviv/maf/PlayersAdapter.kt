package ua.lviv.maf

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.lviv.maf.models.Player
// Додаємо наш перехоплювач
//import ua.lviv.maf.AdInterceptor

class PlayersAdapter(
    private val items: List<Any>,
    private val teamName: String = "",
    private val teamLogo: String = "",
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_section_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_player, parent, false)
            PlayerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(items[position] as String)
        } else if (holder is PlayerViewHolder) {
            holder.bind(items[position] as Player, teamName, teamLogo)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvHeaderTitle)
        // Додаємо безпечне посилання на нашу нову іконку (з ? для стабільності, якщо її раптом не буде в layout)
        private val ivIcon: ImageView? = view.findViewById(R.id.ivPositionIcon)

        fun bind(title: String) {
            tvTitle.text = title

            // Визначаємо іконку залежно від тексту заголовка
            val iconResId = when (title.uppercase()) {
                "ВОРОТАРІ", "ГОЛКІПЕРИ" -> R.drawable.ic_goalkeeper
                "ЗАХИСНИКИ" -> R.drawable.ic_defender
                "ПІВЗАХИСНИКИ" -> R.drawable.ic_midfielder
                "НАПАДНИКИ" -> R.drawable.ic_forward
                else -> 0 // Повертаємо 0 (немає іконки), якщо категорія інша (наприклад, "ТРЕНЕРИ")
            }

            // Якщо іконка є - показуємо її, якщо ні - просто приховуємо місце під неї
            if (iconResId != 0) {
                ivIcon?.visibility = View.VISIBLE
                ivIcon?.setImageResource(iconResId)
            } else {
                ivIcon?.visibility = View.GONE
            }
        }
    }

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvPlayerName)
        private val tvNumber: TextView = view.findViewById(R.id.tvPlayerNumber)
        private val ivPhoto: ImageView = view.findViewById(R.id.ivPlayerPhoto)
        private val tvPosition: TextView? = view.findViewById(R.id.tvPlayerPosition)

        fun bind(player: Player, tName: String, tLogo: String) {
            tvName.text = player.name ?: ""
            tvNumber.text = if (!player.number.isNullOrEmpty()) "#${player.number}" else ""
            tvPosition?.visibility = View.GONE

            if (!player.photo.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(player.photo?.replace("http://", "https://"))
                    .transform(PlayerTopCropTransformation())
                    // Встановлюємо твій новий преміальний аватар як заглушку під час завантаження
                    .placeholder(R.drawable.ic_player_placeholder)
                    .into(ivPhoto)
            } else {
                // Встановлюємо твій новий преміальний аватар, якщо фото взагалі немає
                ivPhoto.setImageResource(R.drawable.ic_player_placeholder)
            }

            // Клік: рекламу відключено, переходимо до профілю напряму
            itemView.setOnClickListener {
                val context = itemView.context
                // AdInterceptor.execute(context) {
                val intent = Intent(context, PlayerProfileActivity::class.java)

                intent.putExtra("PLAYER_ID", player.id)
                intent.putExtra("PLAYER_NAME", player.name)
                intent.putExtra("PLAYER_PHOTO", player.photo?.replace("http://","https://") ?: "")
                intent.putExtra("PLAYER_NUMBER", player.number)
                intent.putExtra("PLAYER_POSITION", player.position)
                intent.putExtra("PLAYER_BIRTHDATE", player.birthDate)
                intent.putExtra("PLAYER_AGE", player.age ?: 0)
                intent.putExtra("TEAM_NAME", tName)
                intent.putExtra("TEAM_LOGO", tLogo.replace("http://", "https://"))

                context.startActivity(intent)
                // }
            }
        }
    }
}