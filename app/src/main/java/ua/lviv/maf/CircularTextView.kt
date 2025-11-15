package ua.lviv.maf

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var text: String = "Миколаївська асоціація футболу"
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress: Float = 0f   // 0..1 – скільки букв вже видно

    init {
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 40f   // можна збільшити/зменшити
        paint.style = Paint.Style.FILL
    }

    fun setText(value: String) {
        text = value
        invalidate()
    }

    fun startLetterByLetterAnimation(duration: Long = 3000L, onEnd: (() -> Unit)? = null) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = duration
        animator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        animator.doOnEnd {
            onEnd?.invoke()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (text.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val centerY = height / 2f

        // Радіус кола для букв (трохи більший за логотип 160dp)
        val radius = min(width, height) / 3f

        val chars = text.toCharArray()
        val count = chars.size

        // Скільки букв показувати, виходячи з progress
        val visibleCount = (count * progress).toInt().coerceAtMost(count)

        if (visibleCount <= 0) return

        // Розподіляємо букви по колу
        val startAngleDeg = -90f  // починаємо зверху
        val angleStep = 360f / count.toFloat()

        for (i in 0 until visibleCount) {
            val ch = chars[i].toString()

            val angleDeg = startAngleDeg + i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val x = centerX + radius * cos(angleRad)
            val y = centerY + radius * sin(angleRad)

            // Невелике коригування, щоб буква центрувалась
            val textWidth = paint.measureText(ch)
            val textHeight = paint.descent() - paint.ascent()

            canvas.drawText(
                ch,
                (x - textWidth / 2f).toFloat(),
                (y + textHeight / 4f).toFloat(),
                paint
            )
        }
    }
}
