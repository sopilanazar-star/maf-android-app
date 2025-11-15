package ua.lviv.maf

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
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

    private var topText: String = "МИКОЛАЇВСЬКА"
    private var bottomText: String = "АСОЦІАЦІЯ ФУТБОЛУ"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progress: Float = 0f
    private var radius: Float = 0f

    // Градієнт
    private var shader: LinearGradient? = null

    init {
        paint.textSize = 52f
        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = true
        paint.letterSpacing = 0.18f
    }

    fun setTopText(value: String) {
        topText = value
        invalidate()
    }

    fun setBottomText(value: String) {
        bottomText = value
        invalidate()
    }

    fun setRadiusPx(r: Float) {
        radius = r
        invalidate()
    }

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

        // Трикольоровий синій градієнт (як на емблемах)
        shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#0A2A6F"), // дуже темно-синій
                Color.parseColor("#0A3D91"), // основний синій з твого лого
                Color.parseColor("#3A6ECF")  // світліший синьо-металевий
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalChars = topText.length + bottomText.length
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        if (totalChars == 0) return

        val usedRadius = if (radius > 0f) radius else min(w, h) / 3f

        val visibleTotal = (totalChars * progress).toInt().coerceAtMost(totalChars)
        val topLen = topText.length
        val bottomLen = bottomText.length

        val visibleTop = visibleTotal.coerceAtMost(topLen)
        val visibleBottom = (visibleTotal - topLen).coerceAtLeast(0).coerceAtMost(bottomLen)

        // === ВЕРХНЯ ДУГА (над гербом) ===
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
                x.toFloat() - textWidth / 2,
                y.toFloat() + textHeight / 4,
                paint
            )
        }

        // === НИЖНЯ ДУГА (під гербом) ===
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
                x.toFloat() - textWidth / 2,
                y.toFloat() + textHeight / 4,
                paint
            )
        }
    }
}
