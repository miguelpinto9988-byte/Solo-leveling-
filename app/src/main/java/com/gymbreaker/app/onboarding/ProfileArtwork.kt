package com.gymbreaker.app.onboarding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.LruCache
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.gymbreaker.app.R
import java.io.File

/**
 * Recursos PNG originales de la biblioteca del perfil.
 * Fondos FHD 1080x1920; marcos / íconos / insignias RGBA 512x512.
 * Ningún recurso Monarca ni miniatura reescalada entra en estas vistas.
 */
object ProfileArtwork {
    @DrawableRes
    fun setupBackground(dark: Boolean): Int =
        if (dark) R.drawable.bg_profile_setup_dark_fhd
        else R.drawable.bg_profile_setup_light_fhd

    @DrawableRes
    fun summaryBackground(dark: Boolean): Int =
        if (dark) R.drawable.bg_profile_summary_dark_fhd
        else R.drawable.bg_profile_summary_light_fhd

    @DrawableRes
    fun frame(name: String): Int = when (name) {
        "Cian" -> R.drawable.frame_profile_cyan
        "Dorado" -> R.drawable.frame_profile_gold
        else -> R.drawable.frame_profile_violet
    }

    @DrawableRes
    fun rankBadge(rank: String): Int = when (rank) {
        "S" -> R.drawable.badge_rank_s
        "A" -> R.drawable.badge_rank_a
        "B" -> R.drawable.badge_rank_b
        "C" -> R.drawable.badge_rank_c
        "D" -> R.drawable.badge_rank_d
        else -> R.drawable.badge_rank_e
    }

    private val imageFiles = linkedMapOf(
        "Sung Jinwoo" to "avatar_01_sung_jinwoo.png",
        "Woo Jinchul" to "avatar_02_woo_jinchul.png",
        "Sung Jinah" to "avatar_03_sung_jinah.png",
        "Go Gunhee" to "avatar_04_go_gunhee.png",
        "Choi Jong-in" to "avatar_05_choi_jongin.png",
        "Baek Yoonho" to "avatar_06_baek_yoonho.png",
        "Liu Zhigang" to "avatar_07_liu_zhigang.png",
        "Cha Hae-in" to "avatar_18_cha_haein.png"
    )
    private val avatarCache = LruCache<String, Bitmap>(12 * 1024) {
        _, bitmap -> bitmap.byteCount / 1024
    }

    fun avatarNames(): List<String> = imageFiles.keys.toList()
    fun avatarAssetName(name: String): String? = imageFiles[name]

    /** Decode using sample size to avoid eight full 1254px bitmaps in memory on selector. */
    private fun readAvatar(context: Context, name: String, thumbnail: Boolean): Bitmap? {
        val file = imageFiles[name] ?: return null
        val key = file + if (thumbnail) ":thumb" else ":portrait"
        avatarCache.get(key)?.let { if (!it.isRecycled) return it }
        return try {
            val options = BitmapFactory.Options().apply { inSampleSize = if (thumbnail) 4 else 2 }
            context.assets.open("profile_avatars/" + file).use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }?.also { avatarCache.put(key, it) }
        } catch (_: Exception) { null }
    }

    fun showAvatar(imageView: ImageView, context: Context, name: String,
                   thumbnail: Boolean = true) {
        val bitmap = readAvatar(context, name, thumbnail)
        if (bitmap != null) imageView.setImageBitmap(bitmap)
        else imageView.setImageResource(R.drawable.icon_profile_camera)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.contentDescription = "Avatar " + name +
            if (bitmap == null) " · imagen aún no incorporada" else ""
    }

    fun photoFile(context: Context): File? {
        val png = File(context.filesDir, "hunter_profile_photo.png")
        val jpg = File(context.filesDir, "hunter_profile_photo.jpg")
        return when {
            png.isFile -> png
            jpg.isFile -> jpg // compatibilidad con perfiles creados en 0.8 y 0.9
            else -> null
        }
    }

    fun showPortrait(imageView: ImageView, context: Context, setup: HunterSetup) {
        val image = if (setup.photoSelected) photoFile(context) else null
        val bitmap = image?.let { BitmapFactory.decodeFile(it.absolutePath) }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            showAvatar(imageView, context, setup.avatarVariant, thumbnail = false)
        }
        imageView.contentDescription = "Fotografía o avatar personal de " +
            setup.hunterName.ifBlank { "cazador" }
    }

    /**
     * Capa fotográfica circular y anillo PNG RGBA real en una vista accesible.
     * El marco nunca modifica píxeles ni el archivo de la fotografía.
     */
    fun portrait(context: Context, setup: HunterSetup, sizeDp: Int): FrameLayout {
        val density = context.resources.displayMetrics.density
        fun d(x: Int): Int = (x * density + 0.5f).toInt()
        val container = FrameLayout(context)
        val photo = ImageView(context).apply {
            showPortrait(this, context, setup)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(19, 13, 43))
            }
            clipToOutline = true
        }
        container.addView(photo, FrameLayout.LayoutParams(d(sizeDp-15), d(sizeDp-15), Gravity.CENTER))
        if (setup.frame != "Sin marco") {
            val frameImage = ImageView(context).apply {
                setImageResource(frame(setup.frame))
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
            }
            container.addView(frameImage, FrameLayout.LayoutParams(-1, -1))
        }
        container.contentDescription = photo.contentDescription
        return container
    }

    /** Copia de imagen privada, sin EXIF: conserva PNG si el origen es PNG. */
    fun importPhoto(context: Context, uri: Uri): Boolean {
        return try {
        val resolver = context.contentResolver
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        require(options.outWidth > 0 && options.outHeight > 0)
        require(options.outWidth.toLong() * options.outHeight.toLong() < 130_000_000L)
        var sample = 1
        while (options.outWidth / sample > 1536 || options.outHeight / sample > 1536)
            sample *= 2
        val decode = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decode)
        } ?: return false
        val isPng = resolver.getType(uri)?.lowercase() == "image/png"
        val extension = if (isPng) "png" else "jpg"
        val candidate = File(context.filesDir, "hunter_profile_photo.$extension")
        val temp = File(context.filesDir, "hunter_profile_photo_$extension.tmp")
        try {
            temp.outputStream().use { output ->
                val compression = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                check(bitmap.compress(compression, if (isPng) 100 else 91, output))
            }
            require(temp.length() in 1..8_000_000L)
            if (candidate.exists()) candidate.delete()
            check(temp.renameTo(candidate))
            File(context.filesDir,
                if (isPng) "hunter_profile_photo.jpg" else "hunter_profile_photo.png").delete()
            true
        } finally {
            bitmap.recycle()
            temp.delete()
        }
        } catch (_: Exception) {
            false
        }
    }
}
