package ua.maf.lviv   // ⚠️ ЗАМІНИ на свій пакет

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun notifyGoal(message: String) {
        Log.d("MAF_WEBVIEW", "GOAL: $message")
        showNotification("⚽ ГОЛ!", message)
    }

    private fun showNotification(title: String, text: String) {
        val channelId = "maf_live_channel"

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Live матчі",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
