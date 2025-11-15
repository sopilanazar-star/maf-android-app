package ua.lviv.maf

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
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

    // Текст зверху і знизу – як на емблемі
    private var topText: String = "МИКОЛАЇВСЬКА"
    private var bottomText: String = "АСОЦІАЦІЯ ФУТБОЛУ"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 0..1 – прогрес побуквенної появи
    private var progress: Float = 0f

    // Радіус кола для букв (задаємо з IntroActivity під розмір герба)
    private var radius: Float = 0f

    // Градієнт для синіх букв
    private var shader: LinearGradient? = null

    init {
        // Розмір та стиль букв
        paint.textSize = 52f
        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = true        // товсті букви
        paint.letterSpacing = 0.18f        // розріджені, як на емблемі

        // Підключаємо шрифт Montserrat ExtraBold із res/font/montserrat_extrabold.ttf
        val tf = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
        if (tf != null) {
            paint.typeface = tf
        }
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

    /** Побуквенна анімація (стандартно 5 секунд) */
    fun startLetterByLetterAnimation(duration: Long = 5000L, onEnd: (() -> Unit)? = null) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = duration
        animator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        animator.doOnEnd { onEnd?.invoke() }
        animator.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Вертикальний синій градієнт для тексту
        shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#0A2A6F"), // дуже темно-синій
                Color.parseColor("#0A3D91"), // основний синій з емблеми
                Color.parseColor("#3A6ECF")  // трохи світліший синій
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = shader
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

        // === ВЕРХНЯ ДУГА: "МИКОЛАЇВСЬКА" ===
        // Кути приблизно 200°..340° (над гербом)
        if (visibleTop > 0 && topLen > 0) {
            val topStart = 200f
            val topEnd = 340f
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

        // === НИЖНЯ ДУГА: "АСОЦІАЦІЯ ФУТБОЛУ" ===
        // Кути 20°..160° (під гербом)
        if (visibleBottom > 0 && bottomLen > 0) {
            val bottomStart = 20f
            val bottomEnd = 160f
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
