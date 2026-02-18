package ua.lviv.maf

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
            holder.bind(items[position] as Player, onPlayerClick)
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

        fun bind(player: Player, onClick: (Player) -> Unit) {
            tvName.text = player.name
            tvNumber.text = if (player.number.isNotEmpty()) "#${player.number}" else ""
            tvPosition?.visibility = View.GONE

            if (player.photo.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(player.photo.replace("http://", "https://"))
                    .transform(TopCropCircleTransformation()) // 🔥 Flashscore crop
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivPhoto)
            } else {
                ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener { onClick(player) }
        }
    }
}

//
// 🔥 Flashscore-style crop
//
class TopCropCircleTransformation : BitmapTransformation() {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("top_crop_circle".toByteArray())
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {

        val size = minOf(outWidth, outHeight)
        val x = 0
        val y = (toTransform.height * 0.15).toInt() // ← зміщення ВГОРУ (голова)

        val cropped = Bitmap.createBitmap(
            toTransform,
            x,
            y,
            toTransform.width,
            toTransform.height - y
        )

        val squared = Bitmap.createScaledBitmap(cropped, size, size, true)

        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint()
        paint.isAntiAlias = true

        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squared, 0f, 0f, paint)

        return result
    }
}
