package com.gymbreaker.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.gymbreaker.app.data.TrainingDatabase
import com.gymbreaker.app.domain.HunterProgression
import com.gymbreaker.app.onboarding.HunterOnboardingBackdrop
import com.gymbreaker.app.onboarding.HunterSetupStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * El perfil es una Activity independiente, sin barra de navegación inferior.
 * Solo se abre desde el retrato de la cabecera de Inicio.
 */
class ProfileActivity : AppCompatActivity() {
    private lateinit var body: LinearLayout
    private fun dp(n: Int) = (n * resources.displayMetrics.density + .5f).toInt()
    private fun label(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            setLineSpacing(dp(2).toFloat(), 1.0f)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("hunter_preferences", MODE_PRIVATE)
        val accent = try { Color.parseColor(prefs.getString("accent", "#A56CFF")) }
            catch (_: Exception) { Color.rgb(165, 108, 255) }
        window.statusBarColor = Color.rgb(8, 7, 22)
        window.navigationBarColor = Color.rgb(8, 7, 22)
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            background = HunterOnboardingBackdrop(accent)
        }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(30))
        }
        scroll.addView(body)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        showProfile()
    }

    private fun avatar(name: String): Int = when (name) {
        "Igris" -> R.drawable.shadow_igris
        "Tank" -> R.drawable.shadow_tank
        "Iron" -> R.drawable.shadow_iron
        "Tusk" -> R.drawable.shadow_tusk
        "Kaisel" -> R.drawable.shadow_kaisel
        else -> R.drawable.shadow_beru
    }

    private fun showProfile() {
        if (!::body.isInitialized) return
        val setup = HunterSetupStore.load(this)
        body.removeAllViews()
        val accent = try { Color.parseColor(setup.accent) }
            catch (_: Exception) { Color.rgb(165, 108, 255) }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val back = MaterialButton(this).apply {
            text = "❮"
            isAllCaps = false
            setTextColor(Color.WHITE)
            strokeColor = ColorStateList.valueOf(accent)
            strokeWidth = dp(1)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(28, 18, 58))
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { finish() }
            contentDescription = "Volver al inicio"
        }
        header.addView(back, LinearLayout.LayoutParams(dp(58), dp(50)))
        header.addView(label("Mi perfil de cazador", 23f, Color.WHITE, true),
            LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(9) })
        body.addView(header)
        body.addView(View(this), LinearLayout.LayoutParams(1, dp(16)))
        val art = ImageView(this).apply {
            val file = File(filesDir, "hunter_profile_photo.jpg")
            if (setup.photoSelected && file.exists()) {
                setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            } else setImageResource(avatar(setup.avatarVariant))
            contentDescription = "Fotografía o avatar de ${setup.hunterName}"
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(32, 22, 64))
                setStroke(dp(3), accent)
            }
            clipToOutline = true
        }
        body.addView(art, LinearLayout.LayoutParams(dp(136), dp(136)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        body.addView(View(this), LinearLayout.LayoutParams(1, dp(14)))
        body.addView(label(setup.hunterName, 25f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
        })
        val stats = label("Rango E · Nivel 1 · 0 XP", 17f,
            Color.rgb(218, 193, 255), true).apply { gravity = Gravity.CENTER }
        body.addView(stats)
        body.addView(View(this), LinearLayout.LayoutParams(1, dp(20)))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(20), dp(17), dp(20))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 22, 75), Color.rgb(9, 12, 34))
            ).apply {
                cornerRadius = dp(18).toFloat()
                setStroke(dp(2), accent)
            }
        }
        fun addLine(title: String, detail: String) {
            card.addView(label(title, 12f, Color.rgb(184, 164, 218), true))
            card.addView(label(detail, 16f, Color.WHITE))
            card.addView(View(this), LinearLayout.LayoutParams(1, dp(12)))
        }
        addLine("OBJETIVO PRINCIPAL", setup.primaryGoal)
        if (setup.subGoal.isNotBlank()) addLine("SUBOBJETIVO", setup.subGoal)
        addLine("DIFICULTAD", when (setup.preferredDifficulty) {
            "avanzado" -> "Avanzado"
            "intermedio" -> "Intermedio"
            else -> "Básico"
        })
        addLine("META SEMANAL", "${setup.weeklyMissions} misiones (máximo 7)")
        addLine("ENTRENAMIENTO", "${setup.minutes} min · ${setup.restSeconds} s de descanso entre bloques")
        addLine("TUS DÍAS", setup.groups.mapIndexed { i, group ->
            "${listOf("D","L","M","X","J","V","S")[i]}: $group"
        }.joinToString("  ·  "))
        addLine("RESTRICCIONES", setup.restrictions.joinToString(", ").ifBlank {
            "Sin restricciones declaradas"
        })
        body.addView(card)
        body.addView(View(this), LinearLayout.LayoutParams(1, dp(18)))
        body.addView(MaterialButton(this).apply {
            text = "EDITAR PERFIL Y SEMANA  ❯"
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(accent)
            minHeight = dp(54)
            setOnClickListener {
                startActivity(Intent(this@ProfileActivity, OnboardingActivity::class.java)
                    .putExtra("edit_profile", true))
            }
        }, LinearLayout.LayoutParams(-1, dp(54)))
        body.addView(label(
            "El rango se obtiene mediante XP confirmado, no depende de la dificultad elegida.",
            12f, Color.rgb(195, 183, 218)
        ).apply { gravity = Gravity.CENTER; setPadding(0, dp(13), 0, 0) })

        lifecycleScope.launch {
            val xp = withContext(Dispatchers.IO) {
                // Los cálculos del juego proceden únicamente de sesiones registradas.
                var total = 0
                TrainingDatabase.get(applicationContext).trainingDao()
                    .observeSessions().collect { sessions ->
                        total = sessions.sumOf { it.xpEarned }
                        return@collect
                    }
                total
            }
            if (!isFinishing && !isDestroyed) {
                stats.text = "Rango ${HunterProgression.rank(xp)} · " +
                    "Nivel ${HunterProgression.level(xp)} · ${xp} XP"
            }
        }
    }
}
