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

        // Радіус дуг (трохи більший за герб)
        val radius = min(widthF, heightF) / 2.35f

        val topChars = topText.toCharArray()
        val bottomChars = bottomText.toCharArray()
        val totalCount = topChars.size + bottomChars.size
        if (totalCount == 0) return

        val visibleTotal = (totalCount * progress).toInt().coerceAtMost(totalCount)
        val visibleTop = visibleTotal.coerceAtMost(topChars.size)
        val visibleBottom = (visibleTotal - topChars.size)
            .coerceAtLeast(0)
            .coerceAtMost(bottomChars.size)

        // ===== ВЕРХНЯ ДУГА: "Миколаївська" =====
        if (visibleTop > 0) {
            val count = topChars.size
            val span = 200f              // ширина дуги (градусів)
            val centerAngle = -90f       // центр зверху
            val startAngle = centerAngle - span / 2f  // зліва зверху
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

        // ===== НИЖНЯ ДУГА: "асоціація футболу" =====
        if (visibleBottom > 0) {
            val count = bottomChars.size
            val span = 200f              // така ж ширина дуги
            val centerAngle = 90f        // центр знизу
            // Починаємо ЗЛІВА знизу → читаємо вправо
            val startAngle = centerAngle + span / 2f  // ~190° (зліва знизу)
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
