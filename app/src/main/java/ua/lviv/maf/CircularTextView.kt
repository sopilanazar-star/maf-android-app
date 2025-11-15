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

    // Верхня і нижня дуги, як на емблемі
    private var topText: String = "МИКОЛАЇВСЬКА"
    private var bottomText: String = "АСОЦІАЦІЯ ФУТБОЛУ"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 0..1 – скільки букв уже показано
    private var progress: Float = 0f

    // радіус кола (задамо з IntroActivity під розмір герба)
    private var radius: Float = 0f

    init {
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 40f
        paint.style = Paint.Style.FILL
        paint.letterSpacing = 0.15f   // розріджені букви, як на лого
    }

    fun setTopText(value: String) {
        topText = value
        invalidate()
    }

    fun setBottomText(value: String) {
        bottomText = value
        invalidate()
    }

    /** Встановити радіус кола в пікселях (трохи більший за герб) */
    fun setRadiusPx(r: Float) {
        radius = r
        invalidate()
    }

    fun startLetterByLetterAnimation(duration: Long = 3000L, onEnd: (() -> Unit)? = null) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = duration
        animator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        animator.doOnEnd { onEnd?.invoke() }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalChars = topText.length + bottomText.length
        if (totalChars == 0) return

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        val usedRadius = if (radius > 0f) radius else min(w, h) / 3f

        val visibleTotal = (totalChars * progress).toInt().coerceAtMost(totalChars)
        if (visibleTotal <= 0) return

        val topLen = topText.length
        val bottomLen = bottomText.length

        val visibleTop = visibleTotal.coerceAtMost(topLen)
        val visibleBottom = (visibleTotal - topLen).coerceAtLeast(0).coerceAtMost(bottomLen)

        // Верхня дуга: МИКОЛАЇВСЬКА (приблизно від -160° до -20°)
        if (visibleTop > 0 && topLen > 0) {
            val topStart = -160f
            val topEnd = -20f
            val topSpan = topEnd - topStart

            for (i in 0 until visibleTop) {
                val ch = topText[i].toString()
                val frac = if (topLen == 1) 0.5f else i.toFloat() / (topLen - 1).toFloat()
                val angleDeg = topStart + topSpan * frac
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val x = cx + usedRadius * cos(angleRad)
                val y = cy + usedRadius * sin(angleRad)

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

        // Нижня дуга: АСОЦІАЦІЯ ФУТБОЛУ (приблизно від 200° до 340°)
        if (visibleBottom > 0 && bottomLen > 0) {
            val bottomStart = 200f
            val bottomEnd = 340f
            val bottomSpan = bottomEnd - bottomStart

            for (j in 0 until visibleBottom) {
                val ch = bottomText[j].toString()
                val frac = if (bottomLen == 1) 0.5f else j.toFloat() / (bottomLen - 1).toFloat()
                val angleDeg = bottomStart + bottomSpan * frac
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val x = cx + usedRadius * cos(angleRad)
                val y = cy + usedRadius * sin(angleRad)

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
}
