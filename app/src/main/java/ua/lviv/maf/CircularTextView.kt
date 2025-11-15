package ua.lviv.maf

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Верхній і нижній написи
    private var topText: String = "Миколаївська"
    private var bottomText: String = "асоціація футболу"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress: Float = 0f  // 0..1

    init {
        // 🔹 ТЕМНО-СИНІЙ, як на логотипі
        paint.color = Color.parseColor("#004B8F")

        // 🔹 Розмір шрифту (sp)
        paint.textSize = 18f * resources.displayMetrics.scaledDensity
        paint.style = Paint.Style.FILL

        // 🔹 Шрифт MONTSERRAT EXTRABOLD
        val typeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
        if (typeface != null) {
            paint.typeface = typeface
        }
    }

    fun setTexts(top: String, bottom: String) {
        topText = top
        bottomText = bottom
        invalidate()
    }

    fun startLetterByLetterAnimation(duration: Long = 5000L, onEnd: (() -> Unit)? = null) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            doOnEnd { onEnd?.invoke() }
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val widthF = width.toFloat()
    val heightF = height.toFloat()
    val centerX = widthF / 2f
    val centerY = heightF / 2f

    // Дуже точний радіус, як у твоїй емблемі
    val radius = min(widthF, heightF) / 2.1f

    val topChars = topText.toCharArray()
    val bottomChars = bottomText.toCharArray()

    val totalCount = topChars.size + bottomChars.size
    val visibleTotal = (totalCount * progress).toInt().coerceAtMost(totalCount)

    val visibleTop = visibleTotal.coerceAtMost(topChars.size)
    val visibleBottom = (visibleTotal - topChars.size)
        .coerceAtLeast(0)
        .coerceAtMost(bottomChars.size)

    // ===== ВЕРХНЯ ДУГА =====
    if (visibleTop > 0) {
        val span = 165f               // точна ширина дуги
        val centerAngle = -90f
        val startAngle = centerAngle - span / 2f
        val step = span / (topChars.size - 1)

        for (i in 0 until visibleTop) {
            val ch = topChars[i].toString()
            val angle = Math.toRadians((startAngle + i * step).toDouble())

            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            val w = paint.measureText(ch)
            val h = paint.descent() - paint.ascent()

            canvas.drawText(ch, x.toFloat() - w / 2f, y.toFloat() + h / 4f, paint)
        }
    }

    // ===== НИЖНЯ ДУГА =====
    if (visibleBottom > 0) {
        val span = 170f               // точна ширина дуги
        val centerAngle = 90f
        val startAngle = centerAngle + span / 2f
        val step = -span / (bottomChars.size - 1)

        for (i in 0 until visibleBottom) {
            val ch = bottomChars[i].toString()
            val angle = Math.toRadians((startAngle + i * step).toDouble())

            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            val w = paint.measureText(ch)
            val h = paint.descent() - paint.ascent()

            canvas.drawText(ch, x.toFloat() - w / 2f, y.toFloat() + h / 4f, paint)
        }
    }
}
}
