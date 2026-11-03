package com.gymbreaker.app.onboarding

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.max

/**
 * Fondo artístico original escalable, sin fotografía de terceros. Reproduce
 * el lenguaje visual del boceto: ruinas, luna, sombras, aura y marco violeta.
 * Los componentes de formulario siempre se dibujan por encima.
 */
class HunterOnboardingBackdrop(private val tint: Int) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var alpha = 255
    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.width() < 1 || bounds.height() < 1) return
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width() / 400f, bounds.height() / 820f)
        p.style = Paint.Style.FILL
        p.shader = LinearGradient(0f, 0f, 400f, 820f,
            intArrayOf(Color.rgb(5, 5, 19), Color.rgb(36, 18, 78),
                Color.rgb(13, 9, 40), Color.rgb(3, 5, 20)),
            null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 400f, 820f, p)
        p.shader = null
        // Luna y halo de energía.
        p.shader = RadialGradient(292f, 138f, 135f,
            intArrayOf(Color.argb(175, 142, 82, 243),
                Color.argb(60, 104, 57, 189), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP)
        canvas.drawCircle(292f, 138f, 135f, p)
        p.shader = null
        p.color = Color.argb(105, 183, 145, 255)
        canvas.drawCircle(294f, 126f, 34f, p)
        p.color = Color.argb(215, 20, 13, 51)
        canvas.drawCircle(298f, 120f, 33f, p)
        // Cortinas de niebla y silueta del territorio.
        val mountains = Path().apply {
            moveTo(0f, 690f); lineTo(53f, 628f); lineTo(93f, 663f)
            lineTo(139f, 596f); lineTo(184f, 648f); lineTo(241f, 575f)
            lineTo(298f, 636f); lineTo(338f, 587f); lineTo(400f, 658f)
            lineTo(400f, 820f); lineTo(0f, 820f); close()
        }
        p.color = Color.argb(212, 15, 10, 41)
        canvas.drawPath(mountains, p)
        p.color = Color.argb(207, 8, 9, 32)
        for ((x, size) in listOf(
            30f to 70f, 65f to 100f, 100f to 55f,
            284f to 110f, 325f to 90f, 364f to 115f
        )) {
            canvas.drawRect(x, 660f - size, x + 22f, 802f, p)
            val tower = Path().apply {
                moveTo(x - 9f, 660f - size)
                lineTo(x + 11f, 639f - size)
                lineTo(x + 31f, 660f - size)
                close()
            }
            canvas.drawPath(tower, p)
            canvas.drawRect(x + 8f, 621f - size, x + 14f, 655f - size, p)
        }
        // Aura en las esquinas, sin invadir los textos de acceso.
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        p.color = Color.argb(140, Color.red(tint), Color.green(tint), Color.blue(tint))
        val rune = Path().apply {
            moveTo(0f, 240f); lineTo(19f, 222f); lineTo(16f, 183f)
            moveTo(400f, 510f); lineTo(379f, 525f); lineTo(390f, 559f)
            moveTo(0f, 604f); lineTo(20f, 622f); lineTo(7f, 655f)
        }
        canvas.drawPath(rune, p)
        p.strokeWidth = 2f
        p.color = Color.argb(205, 167, 118, 255)
        canvas.drawRoundRect(7f, 8f, 393f, 812f, 14f, 14f, p)
        canvas.drawLine(21f, 8f, 40f, 25f, p)
        canvas.drawLine(393f, 21f, 377f, 38f, p)
        canvas.drawLine(7f, 790f, 28f, 810f, p)
        canvas.drawLine(374f, 812f, 393f, 793f, p)
        p.style = Paint.Style.FILL
        p.color = tint
        for (i in 0..5) {
            val x = 28f + i * 63f
            canvas.drawCircle(x, 806f, if (i % 2 == 0) 1.7f else 1f, p)
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) { this.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
