package com.gymbreaker.app.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.gymbreaker.app.DashboardState
import com.gymbreaker.app.R
import com.gymbreaker.app.onboarding.HunterSetup
import java.time.LocalDate

/** Panel de inicio nativo con datos reales de Room y perfil local. */
class HunterHomeDashboard(private val context: Context) {
    data class Actions(
        val onRoutine: () -> Unit,
        val onMissions: () -> Unit,
        val onProgress: () -> Unit,
        val onShadows: () -> Unit,
        val onAssistant: () -> Unit
    )

    private val white = Color.WHITE
    private val light = Color.rgb(229, 220, 245)
    private val subtle = Color.rgb(179, 166, 205)
    private fun d(value: Int): Int = (context.resources.displayMetrics.density * value + .5f).toInt()
    private fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private fun text(value: String, size: Float, color: Int = white, bold: Boolean = false) =
        TextView(context).apply {
            text = value
            setTextColor(color)
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            setLineSpacing(d(2).toFloat(), 1.0f)
        }
    private fun spacer(parent: LinearLayout, size: Int = 9) {
        parent.addView(View(context), LinearLayout.LayoutParams(1, d(size)))
    }

    private fun gradient(bgStart: Int, bgEnd: Int, stroke: Int, radius: Int = 18) =
        GradientDrawable(GradientDrawable.Orientation.TL_BR,
            intArrayOf(bgStart, bgEnd)).apply {
                cornerRadius = d(radius).toFloat()
                setStroke(d(1), stroke)
            }

    private fun atmosphere(@DrawableRes resource: Int) =
        LayerDrawable(arrayOf(
            ContextCompat.getDrawable(context, resource)!!.mutate().apply {
                if (this is BitmapDrawable) gravity = Gravity.FILL
            },
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.argb(202, 10, 10, 35),
                    Color.argb(231, 13, 10, 33), Color.argb(247, 11, 10, 29))
            )
        ))

    private fun surface(parent: LinearLayout, accent: Int, title: String,
                        subtitle: String? = null,
                        @DrawableRes backdrop: Int? = null,
                        content: (LinearLayout) -> Unit) {
        val card = MaterialCardView(context).apply {
            radius = d(18).toFloat()
            strokeWidth = d(1)
            strokeColor = accent
            setCardBackgroundColor(Color.rgb(17, 13, 39))
            cardElevation = d(2).toFloat()
            useCompatPadding = false
        }
        val body = column().apply {
            setPadding(d(15), d(16), d(15), d(18))
            background = backdrop?.let { atmosphere(it) } ?: gradient(
                Color.rgb(32, 21, 62), Color.rgb(11, 13, 33),
                Color.argb(150, 157, 108, 233))
        }
        body.addView(text(title, 19f, white, true))
        if (subtitle != null) {
            spacer(body, 4)
            body.addView(text(subtitle, 12f, subtle))
        }
        spacer(body, 13)
        content(body)
        card.addView(body)
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = d(15) })
    }

    private fun action(text: String, accent: Int, filled: Boolean = true, onClick: () -> Unit) =
        MaterialButton(context).apply {
            this.text = text
            textSize = 13f
            isAllCaps = false
            minHeight = d(48)
            minimumHeight = d(48)
            strokeWidth = d(1)
            strokeColor = ColorStateList.valueOf(accent)
            backgroundTintList = ColorStateList.valueOf(
                if (filled) accent else Color.rgb(22, 18, 49)
            )
            setTextColor(Color.WHITE)
            cornerRadius = d(13)
            setOnClickListener { onClick() }
        }

    private fun portrait(companion: String) = when (companion) {
        "Igris" -> R.drawable.shadow_igris
        "Iron" -> R.drawable.shadow_iron
        "Tank" -> R.drawable.shadow_tank
        "Tusk" -> R.drawable.shadow_tusk
        "Kaisel" -> R.drawable.shadow_kaisel
        else -> R.drawable.shadow_beru
    }

    fun show(host: LinearLayout, state: DashboardState, setup: HunterSetup, accent: Int,
             actions: Actions) {
        host.removeAllViews()
        val weekday = LocalDate.now().dayOfWeek.value % 7
        val currentGroup = setup.groups.getOrNull(weekday) ?: "Descanso"
        val dayName = listOf("Domingo", "Lunes", "Martes", "Miércoles",
            "Jueves", "Viernes", "Sábado")[weekday]
        val difficulty = when (setup.preferredDifficulty) {
            "intermedio" -> "Intermedio"
            "avanzado" -> "Avanzado"
            else -> "Básico"
        }

        val hero = MaterialCardView(context).apply {
            radius = d(20).toFloat()
            strokeWidth = d(2)
            strokeColor = accent
            cardElevation = d(4).toFloat()
            setCardBackgroundColor(Color.rgb(15, 11, 37))
        }
        val art = FrameLayout(context).apply {
            background = atmosphere(MissionBackgroundCatalog.normalBackground(
                "SYSTEM", state.companion, LocalDate.now().dayOfYear
            ))
            minimumHeight = d(204)
        }
        val backing = ImageView(context).apply {
            setImageResource(portrait(state.companion))
            scaleType = ImageView.ScaleType.FIT_END
            alpha = 0.26f
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        art.addView(backing, FrameLayout.LayoutParams(d(220), -1, Gravity.RIGHT))
        val heroContent = column().apply { setPadding(d(18), d(18), d(18), d(21)) }
        heroContent.addView(text("TU SISTEMA DE CAZADOR", 11f, light, true).apply {
            letterSpacing = .16f
        })
        spacer(heroContent, 7)
        heroContent.addView(text("Bienvenido, ${setup.hunterName.ifBlank { "Cazador" }}",
            24f, white, true).apply { maxLines = 2 })
        spacer(heroContent, 8)
        heroContent.addView(text("RANGO ${state.rank}   ·   NIVEL ${state.level}",
            20f, white, true))
        spacer(heroContent, 8)
        heroContent.addView(text("${state.xp} XP confirmados   ·   ${200 - (state.xp % 200)} XP para el siguiente nivel",
            12f, light))
        spacer(heroContent, 9)
        val progress = ProgressBar(context, null,
            android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            this.progress = (state.xp % 200) * 100 / 200
            progressTintList = ColorStateList.valueOf(accent)
            progressBackgroundTintList = ColorStateList.valueOf(Color.rgb(63, 48, 99))
            minimumHeight = d(8)
        }
        heroContent.addView(progress, LinearLayout.LayoutParams(-1, d(9)))
        spacer(heroContent, 12)
        heroContent.addView(text("Dificultad elegida: $difficulty  ·  Objetivo: ${setup.primaryGoal}",
            12f, light))
        art.addView(heroContent, FrameLayout.LayoutParams(-1, -2))
        hero.addView(art)
        host.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = d(15) })

        surface(host, accent, "ENTRENAMIENTO DE HOY", "$dayName · ${setup.minutes} minutos",
            backdrop = MissionBackgroundCatalog.normalBackground("TRAIN", state.companion, LocalDate.now().dayOfYear)) { body ->
            body.addView(text(
                if (currentGroup == "Descanso") "Descanso programado"
                else currentGroup, 22f, white, true
            ))
            spacer(body, 8)
            body.addView(text(
                if (currentGroup == "Descanso")
                    "Hoy tienes un día libre en tu calendario. No hay penalizaciones por descansar."
                else "Tu propuesta se ajustará a la dificultad $difficulty, al equipamiento y a las restricciones del perfil. Solo se usarán ejercicios compatibles y revisados.",
                13f, light
            ))
            spacer(body, 14)
            body.addView(action(if (currentGroup == "Descanso") "Ver calendario semanal"
                else "PREPARAR RUTINA  ›", accent) {
                if (currentGroup == "Descanso") actions.onMissions() else actions.onRoutine()
            })
        }

        surface(host, accent, "TU SEMANA", "Días elegidos · la configuración se edita desde tu perfil",
            backdrop = MissionBackgroundCatalog.normalBackground("CATALOG", state.companion, LocalDate.now().dayOfYear)) { body ->
            val labels = HunterSetup.days
            val line = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            labels.forEachIndexed { index, day ->
                val isToday = index == weekday
                val isRest = setup.groups[index] == "Descanso"
                val badge = text(day, 13f, if (isRest) subtle else white, true).apply {
                    gravity = Gravity.CENTER
                    background = gradient(
                        if (isToday) Color.rgb(78, 38, 153)
                        else if (isRest) Color.rgb(24, 21, 43)
                        else Color.rgb(22, 42, 85),
                        Color.rgb(12, 12, 36),
                        if (isToday) accent else if (isRest) Color.rgb(69, 60, 96)
                        else Color.rgb(80, 135, 234), 50
                    )
                    contentDescription =
                        "${listOf("Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado")[index]}: ${setup.groups[index]}"
                }
                line.addView(badge, LinearLayout.LayoutParams(0, d(39), 1f).apply {
                    marginEnd = if (index == labels.lastIndex) 0 else d(4)
                })
            }
            body.addView(line)
            spacer(body, 13)
            body.addView(text("Hoy: $currentGroup", 14f, white, true))
            spacer(body, 8)
            body.addView(text(
                "Meta de misiones: ${setup.weeklyMissions} de 7 · Sesiones registradas esta semana: ${state.weeklySessions}",
                13f, light
            ))
            spacer(body, 10)
            body.addView(action("ABRIR MISIONES  ›", accent, false, actions.onMissions))
        }

        surface(host, accent, "TU PROGRESO",
            "La experiencia es simbólica y no mide tu salud.",
            backdrop = MissionBackgroundCatalog.normalBackground("SETTINGS", state.companion, LocalDate.now().dayOfYear)) { body ->
            body.addView(text(
                "${state.sessions.count { it.confirmedComplete }} sesiones confirmadas · " +
                    "${state.xp} XP totales", 16f, white, true
            ))
            spacer(body, 10)
            body.addView(action("VER PROGRESO  ›", accent, false, actions.onProgress))
        }

        surface(host, accent, "SOMBRA ACOMPAÑANTE", "Personalización narrativa",
            backdrop = MissionBackgroundCatalog.companionBackground(state.companion)) { body ->
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(ImageView(context).apply {
                setImageResource(portrait(state.companion))
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = "Sombra ${state.companion}"
            }, LinearLayout.LayoutParams(d(84), d(84)).apply { marginEnd = d(12) })
            val info = column()
            info.addView(text(state.companion, 21f, white, true))
            spacer(info, 8)
            info.addView(text("Tu acompañante decorativo no modifica la dificultad ni las restricciones.",
                12f, light))
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            body.addView(row)
            spacer(body, 9)
            body.addView(action("VER SOMBRAS  ›", accent, false, actions.onShadows))
        }

        surface(host, accent, "SHADOW CORE · IA OFFLINE",
            "Consulta el asistente. Las sugerencias se revisan antes de aplicarse.",
            backdrop = MissionBackgroundCatalog.normalBackground("AI", state.companion, LocalDate.now().dayOfYear)) { body ->
            body.addView(text("El modelo local ayuda con consultas, pero el planificador conserva las restricciones y la aprobación de ejercicios.",
                13f, light))
            spacer(body, 12)
            body.addView(action("ABRIR ASISTENTE  ›", accent, false, actions.onAssistant))
        }
    }
}
