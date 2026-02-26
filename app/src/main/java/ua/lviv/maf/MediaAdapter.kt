package ua.lviv.maf

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(private val videos: List<TournamentRow>) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvMediaMatch: TextView = view.findViewById(R.id.tvMediaMatch)
        val tvMediaLeague: TextView = view.findViewById(R.id.tvMediaLeague)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val match = videos[position]

        // Виводимо текст: Команда1 2:1 Команда2
        holder.tvMediaMatch.text = "${match.team1} ${match.score} ${match.team2}"
        holder.tvMediaLeague.text = "${match.league} • ${match.date}"

        // Магія YouTube: автоматично тягнемо картинку прев'ю
        val thumbnailUrl = "https://img.youtube.com/vi/${match.youtubeId}/hqdefault.jpg"
        Glide.with(holder.itemView.context)
            .load(thumbnailUrl)
            .into(holder.ivThumbnail)

        // Клік по картці -> відкриваємо YouTube
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${match.youtubeId}"))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = videos.size
}
