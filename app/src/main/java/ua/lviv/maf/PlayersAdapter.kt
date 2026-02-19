package ua.lviv.maf

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import ua.lviv.maf.models.Player
import java.security.MessageDigest

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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
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
        fun bind(title: String) {
            tvTitle.text = title
        }
    }

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvPlayerName)
        private val tvNumber: TextView = view.findViewById(R.id.tvPlayerNumber)
        private val ivPhoto: ImageView = view.findViewById(R.id.ivPlayerPhoto)
        private val tvPosition: TextView? = view.findViewById(R.id.tvPlayerPosition)

        fun bind(player: Player, tName: String, tLogo: String) {
            tvName.text = player.name
            tvNumber.text = if (player.number.isNotEmpty()) "#${player.number}" else ""
            tvPosition?.visibility = View.GONE

            if (player.photo.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(player.photo.replace("http://", "https://"))
                    .transform(PlayerTopCropTransformation()) // 🔥 ОСЬ ПРАВИЛЬНА ОБРІЗКА ВІД МАКІВКИ
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivPhoto)
            } else {
                ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, PlayerProfileActivity::class.java)
                
                intent.putExtra("PLAYER_ID", player.id) // Передаємо ID для матчів
                intent.putExtra("PLAYER_NAME", player.name)
                intent.putExtra("PLAYER_PHOTO", player.photo.replace("http://", "https://"))
                intent.putExtra("PLAYER_NUMBER", player.number)
                intent.putExtra("PLAYER_POSITION", player.position)
                intent.putExtra("PLAYER_BIRTHDATE", player.birthDate)
                intent.putExtra("PLAYER_AGE", player.age ?: 0)
                intent.putExtra("TEAM_NAME", tName)
                intent.putExtra("TEAM_LOGO", tLogo)

                context.startActivity(intent)
            }
        }
    }
}

// 🔥 КЛАС ДЛЯ ОБРІЗКИ (Не видаляй його!)
class PlayerTopCropTransformation : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("player_top_crop".toByteArray())
    }
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val size = Math.min(toTransform.width, toTransform.height)
        val x = (toTransform.width - size) / 2
        val y = 0 // Беремо від самого верху!
        val squared = Bitmap.createBitmap(toTransform, x, y, size, size)
        val result = pool.get(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squared, 0f, 0f, paint)
        return result
    }
}
