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

    // Текст по колу
    private var topText: String = "Миколаївська"
    private var bottomText: String = "асоціація футболу"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 0..1 – прогрес анімації
    private var progress: Float = 0f

    init {
        // колір букв
        paint.color = Color.WHITE

        // розмір шрифту (sp)
        paint.textSize = 18f * resources.displayMetrics.scaledDensity
        paint.style = Paint.Style.FILL

        // шрифт MONTSERRAT
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

    fun startLetterByLetterAnimation(duration: Long = 3000L, onEnd: (() -> Unit)? = null) {
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

        // радіус (трохи більший за логотип)
        val radius = min(widthF, heightF) / 2.4f

        val topChars = topText.toCharArray()
        val bottomChars = bottomText.toCharArray()

        val totalCount = topChars.size + bottomChars.size
        if (totalCount == 0) return

        val visibleTotal = (totalCount * progress).toInt().coerceAtMost(totalCount)

        // скільки букв уже на верхній дузі
        val visibleTop = visibleTotal.coerceAtMost(topChars.size)
        // скільки букв пішло на нижню
        val visibleBottom = (visibleTotal - topChars.size).coerceAtLeast(0)
            .coerceAtMost(bottomChars.size)

        // === ВЕРХНЯ ДУГА: "Миколаївська" ===
        if (visibleTop > 0) {
            val count = topChars.size
            // ширина дуги (в градусах)
            val span = 160f
            // стартовий кут (зліва зверху)
            val startAngle = -90f - span / 2f
            val step = if (count > 1) span / (count - 1) else 0f

            for (i in 0 until visibleTop) {
                val ch = topChars[i].toString()
                val angleDeg = startAngle + i * step
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val x = centerX + radius * cos(angleRad)
                val y = centerY + radius * sin(angleRad)

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

        // === НИЖНЯ ДУГА: "асоціація футболу" ===
        if (visibleBottom > 0) {
            val count = bottomChars.size
            val span = 160f
            // починаємо зліва знизу (кут ~180°)
            val startAngle = 180f - span / 2f
            // рухаємось вправо → кут зменшується
            val step = if (count > 1) -span / (count - 1) else 0f

            for (i in 0 until visibleBottom) {
                val ch = bottomChars[i].toString()
                val angleDeg = startAngle + i * step
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val x = centerX + radius * cos(angleRad)
                val y = centerY + radius * sin(angleRad)

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
