package com.gymbreaker.app.onboarding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.LruCache
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * Fondo ilustrado personalizado por etapa del asistente; nunca Monarcas.
 *
 * Los PNG originales de la biblioteca se empaquetan exclusivamente en
 * assets/onboarding_backgrounds/. Si faltan, utiliza los cuatro fondos
 * originales FHD del perfil (sin recreaciones ampliadas de previews).
 */
object ProfileSceneArtwork {
    data class Scene(val label: String, val filename: String)

    // Asignación aprobada visualmente: bienvenida + cinco formularios + cierre.
    // Únicamente escenarios de las categorías General/Exploración/Portal:
    // los Monarcas permanecen reservados a sus misiones.
    val scenes = listOf(
        Scene("Ciudad moderna", "bg_01_ciudad_moderna_fhd.png"),
        Scene("Sede de la Asociación", "bg_03_sede_de_la_asociacion_fhd.png"),
        Scene("Base de Cazadores", "bg_06_base_de_cazadores_fhd.png"),
        Scene("Gimnasio del Cazador", "bg_02_gimnasio_del_cazador_fhd.png"),
        Scene("Sala de Entrenamiento", "bg_04_sala_de_entrenamiento_fhd.png"),
        Scene("Cueva de Cristales", "bg_09_cueva_de_cristales_fhd.png"),
        Scene("Interior del Gate", "bg_24_interior_del_gate_fhd.png")
    )

    private val cache = object : LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun sceneForPage(page: Int): Scene = scenes[page.coerceIn(0, 6)]

    /** Decodificación eficiente; nunca leer todos los fondos FHD de golpe. */
    private fun decode(context: Context, filename: String): Bitmap? {
        cache.get(filename)?.let { if (!it.isRecycled) return it }
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open("onboarding_backgrounds/" + filename).use {
                BitmapFactory.decodeStream(it, null, options)
            }
            if (options.outWidth < 1080 || options.outHeight < 1920) return null
            val sample = if (context.resources.displayMetrics.widthPixels < 800) 2 else 1
            val bitmap = context.assets.open("onboarding_backgrounds/" + filename).use {
                BitmapFactory.decodeStream(it, null,
                    BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return null
            cache.put(filename, bitmap)
            bitmap
        } catch (_: Exception) { null }
    }

    /**
     * Solo cambia el fondo ilustrado; controles, estado, foto y campos
     * continúan siendo componentes nativos accesibles por encima de la capa.
     */
    fun apply(imageView: ImageView, context: Context, page: Int, darkTheme: Boolean) {
        val scene = sceneForPage(page)
        val bitmap = decode(context, scene.filename)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(if (page == 6) {
                ProfileArtwork.summaryBackground(darkTheme)
            } else {
                ProfileArtwork.setupBackground(darkTheme)
            })
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.contentDescription = scene.label
        imageView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    /** Superposición oscura para mantener legible el formulario sobre la ilustración. */
    fun veil(darkTheme: Boolean): Drawable =
        GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            if (darkTheme) intArrayOf(
                Color.argb(117, 7, 6, 22), Color.argb(156, 8, 8, 25),
                Color.argb(191, 9, 8, 29)
            ) else intArrayOf(
                Color.argb(76, 9, 7, 29), Color.argb(133, 9, 8, 29),
                Color.argb(172, 9, 8, 29)
            )
        )
}
