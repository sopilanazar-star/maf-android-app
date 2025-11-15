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

    private var topText: String = "МИКОЛАЇВСЬКА"
    private var bottomText: String = "АСОЦІАЦІЯ ФУТБОЛУ"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress: Float = 0f
    private var radius: Float = 0f
    private var shader: LinearGradient? = null

    init {
        paint.textSize = 82f          // товсті більші букви
        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = true
        paint.letterSpacing = 0.02f   // майже без відступів – як на лого

        try {
            val tf = ResourcesCompat.getFont(context, R.font.cinzel_black)
            if (tf != null) paint.typeface = tf
        } catch (_: Exception) {}
    }

    fun setRadiusPx(r: Float) {
        radius = r
        invalidate()
    }

    fun startLetterByLetterAnimation(duration: Long = 5000L) {
        val a = ValueAnimator.ofFloat(0f, 1f)
        a.duration = duration
        a.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        a.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#0A2A6F"),
                Color.parseColor("#0A3D91"),
                Color.parseColor("#3A6ECF")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        val r = if (radius > 0f) radius else min(width, height) / 3f
        val total = topText.length + bottomText.length
        val visible = (total * progress).toInt().coerceAtMost(total)

        val topLen = topText.length
        val bottomLen = bottomText.length

        val visTop = visible.coerceAtMost(topLen)
        val visBot = (visible - topLen).coerceAtLeast(0).coerceAtMost(bottomLen)

        // верхня дуга — ближче до оригінального лого
        val topStart = 205f
        val topEnd = 335f

        for (i in 0 until visTop) {
            val ch = topText[i].toString()
            val frac = if (topLen == 1) 0.5f else i.toFloat() / (topLen - 1)
            val ang = Math.toRadians((topStart + (topEnd - topStart) * frac).toDouble())
            drawChar(canvas, ch, cx, cy, r, ang)
        }

        // нижня дуга
        val botStart = 155f
        val botEnd = 25f

        for (i in 0 until visBot) {
            val ch = bottomText[i].toString()
            val frac = if (bottomLen == 1) 0.5f else i.toFloat() / (bottomLen - 1)
            val ang = Math.toRadians((botStart + (botEnd - botStart) * frac).toDouble())
            drawChar(canvas, ch, cx, cy, r, ang)
        }
    }

    private fun drawChar(
        canvas: Canvas,
        ch: String,
        cx: Float,
        cy: Float,
        r: Float,
        angle: Double
    ) {
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)

        val w = paint.measureText(ch)
        val h = paint.descent() - paint.ascent()

        canvas.drawText(
            ch,
            (x - w / 2f).toFloat(),
            (y + h / 4f).toFloat(),
            paint
        )
    }
}
