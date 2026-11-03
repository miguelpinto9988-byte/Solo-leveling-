package com.gymbreaker.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Pantalla de inicio con la composición aprobada y elementos vivos superpuestos.
 * La barra nunca es un temporizador inventado: refleja el trabajo de StartupActivity.
 * El corredor se mueve de forma independiente con el avance y un ciclo de zancada.
 */
class SplashArtView(context: Context) : View(context) {
    private val poster: Bitmap = BitmapFactory.decodeResource(
        resources, R.drawable.loading_poster
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress = 0f
    private var motionPhase = 0f
    private var runnerAnimator: ValueAnimator? = null
    private var progressAnimator: ValueAnimator? = null
    private var lastAnnounced = -1

    private val purple = Color.rgb(150, 80, 255)
    private val pale = Color.rgb(255, 237, 255)
    private val dark = Color.rgb(15, 10, 38)
    private val panelTop = 942f
    private val panelBottom = 1195f

    init {
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "Cargando el sistema de entrenamiento, 0 por ciento"
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun updateProgress(value: Int) {
        val target = value.coerceIn(0, 100) / 100f
        if (target <= progress) return
        progressAnimator?.cancel()
        if (!ValueAnimator.areAnimatorsEnabled()) {
            progress = target
            invalidate()
        } else {
            progressAnimator = ValueAnimator.ofFloat(progress, target).apply {
                duration = 190L
                interpolator = LinearInterpolator()
                addUpdateListener { animation ->
                    progress = animation.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }
        val percent = value.coerceIn(0, 100)
        contentDescription = "Cargando el sistema de entrenamiento, $percent por ciento"
        val milestone = percent / 25
        if (milestone > lastAnnounced && percent in 25..100 && percent % 25 == 0) {
            lastAnnounced = milestone
            announceForAccessibility("$percent por ciento")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (ValueAnimator.areAnimatorsEnabled()) {
            runnerAnimator = ValueAnimator.ofFloat(0f, (2 * PI).toFloat()).apply {
                duration = 800L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    motionPhase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }
    }

    override fun onDetachedFromWindow() {
        runnerAnimator?.cancel()
        progressAnimator?.cancel()
        runnerAnimator = null
        progressAnimator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(10, 7, 29))
        val w = width.toFloat()
        val h = height.toFloat()
        // FIT_CENTER: el título, la silueta de Igris y los cuatro iconos no se recortan.
        val scale = min(w / 992f, h / 1585f)
        val left = (w - 992f * scale) / 2f
        val top = (h - 1585f * scale) / 2f
        canvas.save()
        canvas.translate(left, top)
        canvas.scale(scale, scale)
        paint.reset()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        canvas.drawBitmap(poster, null, RectF(0f, 0f, 992f, 1585f), paint)
        drawAnimatedPanel(canvas)
        canvas.restore()
    }

    private fun drawAnimatedPanel(canvas: Canvas) {
        // Tapa solamente el corredor, la barra fija y el porcentaje del boceto.
        // El resto de la ilustración aprobada permanece intacto.
        paint.shader = LinearGradient(
            0f, panelTop, 0f, panelBottom,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(245, 13, 9, 37),
                Color.argb(252, 14, 10, 40),
                Color.argb(249, 14, 10, 40),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, .16f, .34f, .85f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, panelTop, 992f, panelBottom, paint)
        paint.shader = null

        val start = 134f
        val finish = 790f
        val barTop = 1093f
        val barBottom = 1126f
        val bar = RectF(start, barTop, finish, barBottom)
        paint.color = Color.argb(230, 18, 14, 42)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(bar, 18f, 18f, paint)

        paint.color = Color.rgb(210, 186, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.6f
        canvas.drawRoundRect(bar, 18f, 18f, paint)
        paint.style = Paint.Style.FILL

        val filledEnd = start + (finish - start) * progress
        canvas.save()
        canvas.clipRect(start, barTop + 2, filledEnd, barBottom - 2)
        paint.shader = LinearGradient(
            start, barTop, finish, barTop,
            intArrayOf(Color.rgb(115, 47, 251), Color.rgb(164, 88, 255), Color.rgb(246, 154, 251)),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(RectF(start + 2, barTop + 3, finish - 2, barBottom - 3), 14f, 14f, paint)
        paint.shader = null
        canvas.restore()

        // La sombra sigue el borde del progreso; la zancada oscila de forma cíclica.
        val runnerX = (start + 20f + (finish - start - 40f) * progress).coerceIn(start + 18, finish - 18)
        val runnerY = barTop - 12f + sin(motionPhase * 2f) * 2.5f
        drawRunner(canvas, runnerX, runnerY)

        paint.color = pale
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        paint.textSize = 39f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("${(progress * 100).toInt().coerceIn(0, 100)} %", 807f, 1122f, paint)

        paint.textSize = 23f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        canvas.drawText("Cargando tu sistema de entrenamiento...", 493f, 1171f, paint)
    }

    private fun drawRunner(canvas: Canvas, x: Float, groundY: Float) {
        canvas.save()
        canvas.translate(x, groundY)
        // Espalda inclinada y brazadas alternas: dos poses interpoladas.
        val swing = sin(motionPhase) * 12f
        val bob = cos(motionPhase * 2f) * 2f
        canvas.translate(0f, bob)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 9f
        paint.color = Color.argb(170, 136, 65, 255)
        val silhouette = Path().apply {
            moveTo(-5f, -46f)
            lineTo(14f, -74f)
            moveTo(-5f, -46f)
            lineTo(-17f + swing, -23f)
            lineTo(-35f + swing, -10f)
            moveTo(-5f, -46f)
            lineTo(4f - swing, -25f)
            lineTo(17f - swing, -10f)
            moveTo(3f, -63f)
            lineTo(-13f - swing * .55f, -56f)
            lineTo(-21f - swing * .4f, -42f)
            moveTo(9f, -64f)
            lineTo(22f + swing * .45f, -59f)
            lineTo(30f + swing * .3f, -44f)
        }
        canvas.drawPath(silhouette, paint)
        paint.strokeWidth = 6f
        paint.color = Color.rgb(12, 8, 29)
        canvas.drawPath(silhouette, paint)
        paint.style = Paint.Style.FILL
        paint.color = purple
        canvas.drawCircle(19f, -86f, 11f, paint)
        paint.color = Color.rgb(10, 8, 29)
        canvas.drawCircle(19f, -86f, 8f, paint)
        paint.color = Color.argb(115, 146, 68, 255)
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        for (i in 1..3) {
            val trail = i * 22f
            canvas.drawLine(-35f - trail, -8f, -43f - trail, -9f, paint)
        }
        paint.style = Paint.Style.FILL
        canvas.restore()
    }
}
