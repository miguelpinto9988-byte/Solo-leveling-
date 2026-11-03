package com.gymbreaker.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.annotation.DrawableRes
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.gymbreaker.app.onboarding.HunterSetup
import com.gymbreaker.app.onboarding.HunterSetupStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Asistente real de primera ejecución. Cinco paneles y una confirmación.
 * Fondo oscuro, borde luminoso y formularios reales: no es una captura fija.
 * La segunda etapa incorpora fotografía personal Y selección de tema.
 */
class OnboardingActivity : AppCompatActivity() {
    private var setup = HunterSetup()
    private var page = 0
    private var selectedDay = 1
    private var weeklyMissionsTab = false
    private var editing = false
    private var busy = false
    private var mainFrame: FrameLayout? = null
    private val violet = Color.rgb(164, 108, 255)
    private val pale = Color.rgb(239, 230, 255)
    private val muted = Color.rgb(179, 170, 211)
    private val navy = Color.rgb(12, 10, 30)

    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) { installPrivatePhoto(uri) }
            if (ok) {
                setup.photoSelected = true
                render()
            } else {
                Toast.makeText(this@OnboardingActivity, "No se pudo leer esa imagen", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editing = intent.getBooleanExtra("edit_profile", false)
        setup = HunterSetupStore.load(this)
        page = if (editing) 1 else 0
        window.statusBarColor = Color.rgb(8, 7, 22)
        window.navigationBarColor = Color.rgb(8, 7, 22)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (page in 2..5) {
                    page--
                    render()
                } else if (editing) {
                    finish()
                } else {
                    Toast.makeText(this@OnboardingActivity,
                        "Completa tu perfil para continuar", Toast.LENGTH_SHORT).show()
                }
            }
        })
        render()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + .5f).toInt()
    private fun accent(): Int = try { Color.parseColor(setup.accent) } catch (_: Exception) { violet }

    private fun rounded(background: Int, stroke: Int = accent(), radius: Int = 16, width: Int = 1) =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius).toFloat()
            setColor(background)
            setStroke(dp(width), stroke)
        }

    private fun label(text: String, size: Float = 15f, color: Int = pale, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            setTextColor(color)
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            setLineSpacing(dp(2).toFloat(), 1.03f)
        }

    private fun vertical(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    private fun spacing(container: LinearLayout, height: Int = 12) {
        container.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    @DrawableRes
    private fun currentBackground(): Int = when (page) {
        2 -> R.drawable.bg_onboarding_appearance_fullhd
        3 -> R.drawable.bg_onboarding_week_fullhd
        4 -> R.drawable.bg_onboarding_goals_fullhd
        5 -> R.drawable.bg_onboarding_restrictions_fullhd
        else -> R.drawable.bg_onboarding_identity_fullhd
    }

    @DrawableRes
    private fun currentStepIcon(): Int = when (page) {
        2 -> R.drawable.icon_profile_photo
        3 -> R.drawable.icon_profile_week
        4 -> R.drawable.icon_profile_goal
        5 -> R.drawable.icon_profile_restrictions
        6 -> R.drawable.badge_rank_e
        else -> R.drawable.icon_profile_identity
    }

    @DrawableRes
    private fun currentFrame(): Int = when (setup.frame) {
        "Cian" -> R.drawable.frame_profile_cyan
        "Dorado" -> R.drawable.frame_profile_gold
        else -> R.drawable.frame_profile_violet
    }

    // Retrato y marco PNG de 1024 px con interior transparente.
    private fun framedPortrait(size: Int): FrameLayout {
        val frame = FrameLayout(this)
        val portrait = ImageView(this).apply {
            val privatePhoto = File(filesDir, "hunter_profile_photo.jpg")
            if (setup.photoSelected && privatePhoto.isFile) {
                setImageBitmap(BitmapFactory.decodeFile(privatePhoto.absolutePath))
            } else {
                setImageResource(avatarDrawable())
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(22, 13, 46))
            }
            clipToOutline = true
            contentDescription = "Fotografía o avatar de " + setup.hunterName
        }
        val inner = (size * 0.79f).toInt()
        frame.addView(portrait, FrameLayout.LayoutParams(dp(inner), dp(inner), Gravity.CENTER))
        val overlay = ImageView(this).apply {
            setImageResource(currentFrame())
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        frame.addView(overlay, FrameLayout.LayoutParams(dp(size), dp(size), Gravity.CENTER))
        frame.contentDescription = "Retrato con marco " + setup.frame
        return frame
    }

    private fun section(title: String, subtitle: String? = null): LinearLayout {
        val panel = vertical()
        panel.background = rounded(Color.argb(221, 12, 10, 33), Color.argb(220, 137, 83, 240), 18)
        panel.setPadding(dp(16), dp(16), dp(16), dp(18))
        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heading.addView(ImageView(this).apply {
            setImageResource(currentStepIcon())
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, LinearLayout.LayoutParams(dp(43), dp(43)).apply { marginEnd = dp(10) })
        heading.addView(label(title, 20f, Color.WHITE, true),
            LinearLayout.LayoutParams(0, -2, 1f))
        panel.addView(heading)
        if (subtitle != null) {
            spacing(panel, 6)
            panel.addView(label(subtitle, 13f, muted))
        }
        return panel
    }

    private fun button(text: String, selected: Boolean = false, onClick: () -> Unit): MaterialButton =
        MaterialButton(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            cornerRadius = dp(13)
            strokeWidth = dp(if (selected) 2 else 1)
            strokeColor = ColorStateList.valueOf(
                if (selected) Color.rgb(220, 190, 255) else Color.rgb(94, 82, 146)
            )
            backgroundTintList = ColorStateList.valueOf(
                if (selected) Color.argb(255, 91, 44, 179) else Color.argb(245, 24, 20, 55)
            )
            minHeight = dp(48)
            minimumHeight = dp(48)
            minWidth = 0
            minimumWidth = 0
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setOnClickListener { onClick() }
        }

    private fun choiceRow(panel: LinearLayout, choices: List<String>, selected: String, update: (String) -> Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        choices.forEach { choice ->
            val item = button(choice, choice == selected) { update(choice); render() }
            row.addView(item, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                marginEnd = dp(3)
            })
        }
        panel.addView(row)
    }

    private fun choiceGrid(panel: LinearLayout, choices: List<String>, selected: String, update: (String) -> Unit) {
        choices.chunked(2).forEach { pair ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            pair.forEach { item ->
                val option = button(item, item == selected) { update(item); render() }
                row.addView(option, LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                    marginEnd = dp(6)
                })
            }
            if (pair.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
            panel.addView(row)
        }
    }

    private fun entry(panel: LinearLayout, caption: String, value: String, hint: String,
                      numeric: Boolean = false, update: (String) -> Unit): EditText {
        panel.addView(label(caption, 14f, pale, true))
        spacing(panel, 4)
        val input = EditText(this).apply {
            setSingleLine(true)
            setText(value)
            this.hint = hint
            setHintTextColor(Color.rgb(149, 142, 181))
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = rounded(Color.rgb(21, 18, 51), Color.rgb(107, 84, 164), 10)
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            doAfterTextChanged { update(it?.toString() ?: "") }
        }
        panel.addView(input, LinearLayout.LayoutParams(-1, dp(48)))
        spacing(panel, 12)
        return input
    }

    private fun intro(): LinearLayout {
        val panel = section("SOLO LEVELING  ·  GYM BREAKER", "SISTEMA DE SOMBRAS")
        val silhouette = ImageView(this).apply {
            setImageResource(R.drawable.shadow_igris)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Sombra de un caballero"
        }
        panel.addView(silhouette, LinearLayout.LayoutParams(-1, dp(210)))
        spacing(panel, 14)
        panel.addView(label("Crea tu perfil de cazador", 27f, Color.WHITE, true))
        spacing(panel, 12)
        panel.addView(label(
            "Personaliza tu experiencia, define tus objetivos y organiza tu semana. " +
                "Tu rango inicial será E, sin importar la dificultad elegida.",
            15f, pale
        ))
        spacing(panel, 28)
        panel.addView(button("COMENZAR  ❯", true) { page = 1; render() })
        return panel
    }

    private fun information(): LinearLayout {
        val panel = section("Información básica", "Define tu identidad de cazador. Solo el nombre es obligatorio.")
        spacing(panel, 18)
        entry(panel, "Nombre del cazador", setup.hunterName, "Escribe un alias") {
            setup.hunterName = it.take(35)
        }
        panel.addView(label("Género · opcional", 14f, pale, true))
        choiceRow(panel, listOf("Masculino", "Femenino", "Otro"), setup.gender) {
            setup.gender = it
        }
        spacing(panel, 12)
        entry(panel, "Edad · opcional", setup.age, "Sin indicar", true) { setup.age = it.take(3) }
        entry(panel, "Altura (cm) · opcional", setup.height, "Sin indicar", true) {
            setup.height = it.take(6)
        }
        entry(panel, "Peso (kg) · opcional", setup.weight, "Sin indicar", true) {
            setup.weight = it.take(6)
        }
        panel.addView(label(
            "Estos valores no se utilizarán para comparar cuerpos, fijar metas de peso ni " +
                "calcular restricciones alimentarias.", 12f, muted
        ))
        return panel
    }

    private fun avatarDrawable(): Int = when (setup.avatarVariant) {
        "Beru" -> R.drawable.shadow_beru
        "Tank" -> R.drawable.shadow_tank
        "Iron" -> R.drawable.shadow_iron
        "Tusk" -> R.drawable.shadow_tusk
        "Kaisel" -> R.drawable.shadow_kaisel
        else -> R.drawable.shadow_igris
    }

    private fun appearance(): LinearLayout {
        val panel = section("Apariencia del perfil", "Usa tu propia fotografía o un avatar de la aplicación.")
        spacing(panel, 12)
        panel.addView(framedPortrait(186), LinearLayout.LayoutParams(dp(186), dp(186))
            .apply { gravity = Gravity.CENTER_HORIZONTAL })
        spacing(panel, 12)
        choiceRow(panel, listOf("Elegir foto", "Usar avatar"), if (setup.photoSelected) "Elegir foto" else "Usar avatar") {
            if (it == "Elegir foto") photoPicker.launch("image/*")
            else {
                setup.photoSelected = false
                render()
            }
        }
        spacing(panel, 8)
        panel.addView(label("Avatares disponibles", 13f, pale, true))
        listOf("Igris", "Beru", "Tank", "Iron", "Tusk", "Kaisel")
            .chunked(3).forEach { names ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                names.forEach { name ->
                    val item = vertical().apply {
                        setPadding(dp(4), dp(5), dp(4), dp(5))
                        background = rounded(Color.argb(220, 20, 14, 46),
                            if (!setup.photoSelected && setup.avatarVariant == name)
                                accent() else Color.rgb(75, 60, 116), 10)
                        setOnClickListener {
                            setup.avatarVariant = name
                            setup.photoSelected = false
                            render()
                        }
                    }
                    val art = ImageView(this).apply {
                        setImageResource(when (name) {
                            "Beru" -> R.drawable.shadow_beru
                            "Tank" -> R.drawable.shadow_tank
                            "Iron" -> R.drawable.shadow_iron
                            "Tusk" -> R.drawable.shadow_tusk
                            "Kaisel" -> R.drawable.shadow_kaisel
                            else -> R.drawable.shadow_igris
                        })
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        contentDescription = "Avatar " + name
                    }
                    item.addView(art, LinearLayout.LayoutParams(-1, dp(65)))
                    item.addView(label(name, 12f, pale, true).apply { gravity = Gravity.CENTER })
                    row.addView(item, LinearLayout.LayoutParams(0, dp(95), 1f)
                        .apply { marginEnd = dp(6); topMargin = dp(6) })
                }
                panel.addView(row)
            }
        spacing(panel, 14)
        panel.addView(label("Marco de retrato · opcional", 14f, pale, true))
        choiceRow(panel, listOf("Violeta", "Cian", "Dorado"), setup.frame) {
            setup.frame = it
        }
        spacing(panel, 14)
        panel.addView(label("Tema y colores", 20f, Color.WHITE, true))
        panel.addView(label("El tema se configura aquí, en el paso 2. El fondo ilustrado es automático.", 12f, muted))
        spacing(panel, 8)
        choiceRow(panel, listOf("Oscuro", "Claro"), if (setup.darkTheme) "Oscuro" else "Claro") {
            setup.darkTheme = it == "Oscuro"
        }
        spacing(panel, 8)
        val colors = listOf("Violeta" to "#A56CFF", "Azul" to "#459DFF",
            "Cian" to "#00BFCB", "Rojo" to "#EE638C")
        choiceRow(panel, colors.map { it.first }, colors.firstOrNull { it.second == setup.accent }?.first ?: "Violeta") {
            setup.accent = colors.first { pair -> pair.first == it }.second
        }
        return panel
    }

    private fun calendar(): LinearLayout {
        val panel = section("Tu semana", "Organiza tu entrenamiento. Los días sin sesión son de descanso.")
        spacing(panel, 13)
        choiceRow(panel, listOf("Misiones", "Rutinas diarias"),
            if (weeklyMissionsTab) "Misiones" else "Rutinas diarias") {
            weeklyMissionsTab = it == "Misiones"
        }
        spacing(panel, 13)
        if (weeklyMissionsTab) {
            panel.addView(label("Misiones por semana: ${setup.weeklyMissions} de 7", 18f, Color.WHITE, true))
            spacing(panel, 8)
            panel.addView(label(
                "Elige una meta entre 1 y 7 misiones. Es un objetivo narrativo, " +
                    "no obliga a entrenar siete días ni aumenta la dificultad.",
                13f, muted
            ))
            spacing(panel, 12)
            (1..7).toList().chunked(4).forEach { values ->
                val row = LinearLayout(this)
                values.forEach { value ->
                    row.addView(button(value.toString(), value == setup.weeklyMissions) {
                        setup.weeklyMissions = value
                        render()
                    }, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginEnd = dp(5) })
                }
                repeat(4 - values.size) { row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f)) }
                panel.addView(row)
            }
        } else {
            val week = LinearLayout(this)
            HunterSetup.days.forEachIndexed { index, day ->
                week.addView(button(day, index == selectedDay) {
                    selectedDay = index
                    render()
                }, LinearLayout.LayoutParams(0, dp(54), 1f))
            }
            panel.addView(week)
            spacing(panel, 10)
            panel.addView(label(
                listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")[selectedDay] +
                    " · " + setup.groups[selectedDay], 17f, pale, true
            ))
            spacing(panel, 8)
            choiceGrid(panel, HunterSetup.trainingGroups, setup.groups[selectedDay]) {
                setup.groups[selectedDay] = it
            }
            spacing(panel, 10)
            panel.addView(label("Descanso entre bloques", 14f, pale, true))
            choiceRow(panel, listOf("30 s", "60 s", "90 s"), "${setup.restSeconds} s") {
                setup.restSeconds = it.substringBefore(' ').toInt()
            }
        }
        spacing(panel, 12)
        panel.addView(label("Duración por sesión", 14f, pale, true))
        choiceRow(panel, listOf("20 min", "30 min", "45 min"),
            "${setup.minutes} min".takeIf { it in listOf("20 min", "30 min", "45 min") } ?: "") {
            setup.minutes = it.substringBefore(' ').toInt()
        }
        return panel
    }

    private fun goals(): LinearLayout {
        val panel = section("Objetivos y dificultad", "Objetivo principal y subobjetivo personal, con interpretación revisable.")
        spacing(panel, 14)
        panel.addView(label("Objetivo principal", 14f, pale, true))
        panel.addView(button(setup.primaryGoal + "  ▾", true) {
            val options = HunterSetup.goals.toTypedArray()
            AlertDialog.Builder(this).setTitle("Elige tu objetivo")
                .setItems(options) { _, i -> setup.primaryGoal = options[i]; render() }.show()
        })
        spacing(panel, 10)
        val sub = entry(panel, "Subobjetivo · opcional", setup.subGoal,
            "Ej.: mejorar mi movilidad y constancia") { setup.subGoal = it.take(300) }
        sub.setSingleLine(false)
        sub.minLines = 3
        sub.maxLines = 5
        sub.layoutParams.height = dp(105)
        panel.addView(button("Interpretar con Shadow Core", false) {
            val proposal = HunterSetupStore.suggestGoal(setup.subGoal)
            val message = if (setup.subGoal.contains("dolor", true) ||
                setup.subGoal.contains("lesión", true) ||
                setup.subGoal.contains("molestia", true)) {
                "El texto menciona una molestia. Debes revisarla en Restricciones; " +
                    "el sistema no cambiará el plan para ignorar el dolor."
            } else if (proposal == null) {
                "No hay suficiente información para sugerir otro objetivo. " +
                    "Puedes conservar el texto personal y elegir manualmente el objetivo."
            } else {
                "Interpretación orientativa: «$proposal». " +
                    "¿Quieres actualizar el objetivo principal? El subobjetivo se conservará."
            }
            AlertDialog.Builder(this)
                .setTitle("Propuesta de Shadow Core")
                .setMessage(message)
                .setPositiveButton(if (proposal != null) "Aplicar propuesta" else "Entendido") { _, _ ->
                    if (proposal != null) {
                        setup.primaryGoal = proposal
                        render()
                    }
                }
                .setNegativeButton("Mantener actual", null)
                .show()
        })
        spacing(panel, 12)
        panel.addView(label("Nivel de dificultad · solo tres opciones", 16f, pale, true))
        choiceRow(panel, listOf("Básico", "Intermedio", "Avanzado"),
            when (setup.preferredDifficulty) {
                "intermedio" -> "Intermedio"
                "avanzado" -> "Avanzado"
                else -> "Básico"
            }) {
            setup.preferredDifficulty = when (it) {
                "Intermedio" -> "intermedio"
                "Avanzado" -> "avanzado"
                else -> "inicio"
            }
        }
        spacing(panel, 8)
        panel.addView(label(
            "Básico: variantes sencillas. Intermedio: ejercicios estándar. " +
                "Avanzado: variantes más exigentes solo si están aprobadas. " +
                "Todos empiezan en RANGO E; el rango aumenta solo con XP confirmado.",
            12f, muted
        ))
        return panel
    }

    private fun restrictions(): LinearLayout {
        val panel = section("Restricciones y preferencias",
            "Las restricciones son opcionales y el planificador las respetará.")
        spacing(panel, 10)
        HunterSetup.restrictionOptions.forEach { option ->
            val cb = MaterialCheckBox(this).apply {
                text = option
                textSize = 14f
                setTextColor(pale)
                buttonTintList = ColorStateList.valueOf(accent())
                isChecked = option in setup.restrictions
                setOnCheckedChangeListener { _, checked ->
                    if (checked) setup.restrictions.add(option)
                    else setup.restrictions.remove(option)
                }
            }
            panel.addView(cb)
        }
        spacing(panel, 10)
        panel.addView(label("Equipamiento disponible", 14f, pale, true))
        choiceRow(panel, listOf("Casa", "Mancuernas", "Gimnasio"),
            when (setup.equipment) {
                "GYM" -> "Gimnasio"
                "DUMBBELLS" -> "Mancuernas"
                else -> "Casa"
            }) {
            setup.equipment = when (it) {
                "Gimnasio" -> "GYM"
                "Mancuernas" -> "DUMBBELLS"
                else -> "HOME"
            }
        }
        spacing(panel, 12)
        panel.addView(label(
            "Si indicas una lesión o molestia, las propuestas de rutina " +
                "permanecerán bloqueadas hasta una revisión. Ninguna misión " +
                "impone castigos físicos por descansar.", 12f, muted
        ))
        return panel
    }

    private fun success(): LinearLayout {
        val panel = section("¡Perfil creado!", "Tu camino como cazador ha comenzado.")
        spacing(panel, 20)
        panel.addView(framedPortrait(150), LinearLayout.LayoutParams(dp(150), dp(150))
            .apply { gravity = Gravity.CENTER_HORIZONTAL })
        spacing(panel, 10)
        panel.addView(ImageView(this).apply {
            setImageResource(R.drawable.badge_rank_e)
            contentDescription = "Insignia del rango inicial E"
        }, LinearLayout.LayoutParams(dp(68), dp(68)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        spacing(panel, 16)
        panel.addView(label(setup.hunterName.trim(), 23f, Color.WHITE, true))
        spacing(panel, 6)
        panel.addView(label("Rango inicial: E · Nivel inicial: 1", 17f, pale, true))
        spacing(panel, 16)
        panel.addView(label("Objetivo: " + setup.primaryGoal, 15f))
        if (setup.subGoal.isNotBlank()) panel.addView(label("Subobjetivo: " + setup.subGoal, 14f))
        val difficulty = when (setup.preferredDifficulty) {
            "avanzado" -> "Avanzado"
            "intermedio" -> "Intermedio"
            else -> "Básico"
        }
        panel.addView(label("Dificultad elegida: $difficulty", 15f))
        panel.addView(label("Misiones semanales: ${setup.weeklyMissions} de 7", 15f))
        panel.addView(label("Tiempo por sesión: ${setup.minutes} minutos", 15f))
        panel.addView(label("Días con rutina: " +
            setup.groups.count { it != "Descanso" }, 15f))
        spacing(panel, 18)
        panel.addView(label("El planificador solo usará ejercicios autorizados " +
            "y respetará tus restricciones.", 13f, muted))
        spacing(panel, 20)
        panel.addView(button("ENTRAR A LA APLICACIÓN  ❯", true) { openApp() })
        return panel
    }

    private fun addBottomButtons(column: LinearLayout) {
        spacing(column, 14)
        if (page !in 1..5) return
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        nav.addView(button("❮  Atrás") {
            if (page > 1) { page--; render() }
            else if (editing) finish()
            else { page = 0; render() }
        }, LinearLayout.LayoutParams(0, dp(56), 1f).apply { marginEnd = dp(7) })
        nav.addView(button(if (page == 5) "CREAR PERFIL ✓" else "SIGUIENTE ❯", true) {
            if (busy) return@button
            if (page == 1 && setup.hunterName.trim().length !in 2..35) {
                Toast.makeText(this, "Introduce un nombre de 2 a 35 caracteres", Toast.LENGTH_LONG).show()
                return@button
            }
            if (page == 5) saveAndFinish()
            else { page++; render() }
        }, LinearLayout.LayoutParams(0, dp(56), 1.8f))
        column.addView(nav)
    }

    private fun saveAndFinish() {
        if (busy) return
        busy = true
        lifecycleScope.launch {
            try {
                HunterSetupStore.save(this@OnboardingActivity, setup)
                page = 6
                render()
            } catch (error: Exception) {
                Toast.makeText(this@OnboardingActivity,
                    "No se pudo guardar el perfil. Comprueba los datos y reintenta.",
                    Toast.LENGTH_LONG).show()
            } finally {
                busy = false
            }
        }
    }

    private fun render() {
        val root = FrameLayout(this)
        mainFrame = root
        // Cinco ilustraciones originales rasterizadas a 1080×1920 PNG RGB.
        // CENTER_CROP conserva las proporciones en móviles con otras dimensiones.
        root.addView(ImageView(this).apply {
            setImageResource(currentBackground())
            scaleType = ImageView.ScaleType.CENTER_CROP
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, FrameLayout.LayoutParams(-1, -1))
        // La capa oscura permite leer controles sobre fondos ilustrados.
        root.addView(View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.argb(75, 6, 4, 18),
                    Color.argb(130, 5, 6, 22), Color.argb(155, 6, 4, 19)))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, FrameLayout.LayoutParams(-1, -1))
        if (page == 0 || page == 6) {
            val decor = ImageView(this).apply {
                setImageResource(R.drawable.shadow_igris)
                alpha = 0.22f
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            root.addView(decor, FrameLayout.LayoutParams(dp(240), dp(380),
                Gravity.RIGHT or Gravity.CENTER_VERTICAL))
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }
        val content = vertical()
        content.setPadding(dp(18), dp(22), dp(18), dp(24))
        content.addView(label("SOLO LEVELING", 14f, muted, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.22f
        })
        content.addView(label("GYM BREAKER", 27f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
        })
        spacing(content, 8)
        val header = when (page) {
            0 -> "CREACIÓN DE PERFIL"
            6 -> "REGISTRO COMPLETADO"
            else -> "Creación de Perfil  ·  Paso $page de 5"
        }
        content.addView(label(header, 15f, pale, true).apply { gravity = Gravity.CENTER })
        if (page in 1..5) {
            spacing(content, 8)
            content.addView(label(
                (1..5).joinToString("  ") { if (it <= page) "◆" else "◇" },
                17f, accent()
            ).apply { gravity = Gravity.CENTER })
        }
        spacing(content, 18)
        val panel = when (page) {
            0 -> intro()
            1 -> information()
            2 -> appearance()
            3 -> calendar()
            4 -> goals()
            5 -> restrictions()
            else -> success()
        }
        content.addView(panel)
        addBottomButtons(content)
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun installPrivatePhoto(uri: Uri): Boolean {
        return try {
        val resolver = contentResolver
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        require(options.outWidth > 0 && options.outHeight > 0)
        require(options.outWidth.toLong() * options.outHeight.toLong() < 140_000_000)
        var sample = 1
        while (options.outWidth / sample > 1024 || options.outHeight / sample > 1024) sample *= 2
        val decode = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decode)
        } ?: return false
        val output = File(filesDir, "hunter_profile_photo.jpg")
        val temporary = File(filesDir, "hunter_profile_photo.tmp")
        try {
            temporary.outputStream().use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 84, it)) return false
            }
            if (output.exists()) output.delete()
            check(temporary.renameTo(output))
            true
        } finally {
            bitmap.recycle()
            temporary.delete()
        }
        } catch (_: Exception) {
            false
        }
    }

    private fun openApp() {
        if (editing) {
            finish()
            return
        }
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }
}
