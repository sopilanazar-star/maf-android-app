package ua.lviv.maf

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class PlayerTopCropTransformation : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("player_top_crop_v2".toByteArray())
    }
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val size = Math.min(toTransform.width, toTransform.height)
        val x = (toTransform.width - size) / 2
        val y = 0 // Починаємо з самого верху, щоб не різати голову
        val squared = Bitmap.createBitmap(toTransform, x, y, size, size)
        val result = pool.get(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squared, 0f, 0f, paint)
        return result
    }
}
