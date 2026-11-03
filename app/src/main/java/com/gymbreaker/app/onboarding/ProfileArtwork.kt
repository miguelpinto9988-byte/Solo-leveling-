package com.gymbreaker.app.onboarding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

    /** Ocho retratos PNG RGBA individuales, sin anillo decorativo. */
    val avatars = listOf(
        "Sung Jinwoo", "Woo Jinchul", "Cazadora violeta", "Go Gunhee",
        "Choi Jong-in", "Baek Yoonho", "Liu Zhigang", "Cha Hae-in"
    )

    @DrawableRes
    fun avatar(name: String): Int = when (name) {
        "Woo Jinchul" -> R.drawable.avatar_02_woo_jinchul
        "Cazadora violeta" -> R.drawable.avatar_03_cazadora_violeta
        "Go Gunhee" -> R.drawable.avatar_04_go_gunhee
        "Choi Jong-in" -> R.drawable.avatar_05_choi_jongin
        "Baek Yoonho" -> R.drawable.avatar_06_baek_yoonho
        "Liu Zhigang" -> R.drawable.avatar_07_liu_zhigang
        "Cha Hae-in" -> R.drawable.avatar_18_cha_haein
        else -> R.drawable.avatar_01_sung_jinwoo
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
        if (bitmap != null) imageView.setImageBitmap(bitmap)
        else imageView.setImageResource(avatar(setup.avatarVariant))
        imageView.scaleType = if (bitmap != null) ImageView.ScaleType.CENTER_CROP
        else ImageView.ScaleType.FIT_CENTER
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
            clipToOutline = setup.photoSelected
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
