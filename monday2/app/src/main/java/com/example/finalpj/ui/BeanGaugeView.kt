package com.example.finalpj.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.finalpj.R
import kotlin.math.sin

class BeanGaugeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // 0.0 ~ 1.0, 0%~100%
    var caffeineRatio: Float = 0.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val beanMask = BitmapFactory.decodeResource(resources, R.drawable.bean_empty)
    private val wavePaint = Paint().apply { color = Color.parseColor("#B03E2C") }

    // 애니메이션 관련
    private var wavePhase = 0f
    private val waveAnim = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1200L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            wavePhase = it.animatedFraction
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!waveAnim.isStarted) waveAnim.start()
    }

    override fun onDetachedFromWindow() {
        waveAnim.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        // 크기/마스킹
        val maskRect = Rect(0, 0, width, height)
        val saveLayer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // 1. 물결 패스 그리기
        val fillPercent = 1.0f - caffeineRatio // 0.0=top, 1.0=bottom
        val waveHeight = height * fillPercent

        val wavePath = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(0f, waveHeight)

            val waveAmplitude = 18f
            val waveLength = width.toFloat()
            val phase = wavePhase * waveLength

            for (x in 0..width) {
                val y = (waveHeight
                        + waveAmplitude * sin((x + phase) / waveLength * 2 * Math.PI)).toFloat()
                lineTo(x.toFloat(), y)
            }
            lineTo(width.toFloat(), height.toFloat())
            close()
        }

        canvas.drawPath(wavePath, wavePaint)

        // 2. 콩 마스킹
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawBitmap(beanMask, null, maskRect, maskPaint)

        // 3. 원래 상태로 복구
        canvas.restoreToCount(saveLayer)
    }
}
