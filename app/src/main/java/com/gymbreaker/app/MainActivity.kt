package com.gymbreaker.app

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gymbreaker.app.alarms.TrainingAlarms
import com.gymbreaker.app.databinding.ActivityMainBinding
import com.gymbreaker.app.domain.TrainingPlace
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class Screen { SYSTEM, MISSIONS, TRAIN, SHADOWS, AI, SETTINGS }

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model: TrainingViewModel by viewModels()
    private var currentScreen = Screen.SYSTEM
    private val preferences by lazy { getSharedPreferences("hunter_preferences", MODE_PRIVATE) }
    private val accentOptions = arrayOf("Violeta", "Azul", "Cian", "Rojo")
    private val accentValues = arrayOf("#A56CFF", "#459DFF", "#00BFCB", "#EE638C")

    override fun onCreate(savedInstanceState: Bundle?) {
        val dark = getSharedPreferences("hunter_preferences", MODE_PRIVATE)
            .getBoolean("dark_theme", true)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        bindNavigation()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { render(it) }
            }
        }
    }

    private fun bindNavigation() {
        binding.navSystem.setOnClickListener { navigate(Screen.SYSTEM) }
        binding.navMissions.setOnClickListener { navigate(Screen.MISSIONS) }
        binding.navTrain.setOnClickListener { navigate(Screen.TRAIN) }
        binding.navShadows.setOnClickListener { navigate(Screen.SHADOWS) }
        binding.navAi.setOnClickListener { navigate(Screen.AI) }
        binding.navSettings.setOnClickListener { navigate(Screen.SETTINGS) }
    }

    private fun navigate(screen: Screen) {
        currentScreen = screen
        render(model.state.value)
        binding.rootScroll.smoothScrollTo(0, 0)
        if (ValueAnimator.areAnimatorsEnabled()) {
            ObjectAnimator.ofFloat(binding.heroCard, View.ALPHA, 0.75f, 1f)
                .setDuration(180).start()
        }
    }

    private fun render(state: DashboardState) {
        val accent = Color.parseColor(preferences.getString("accent", "#A56CFF"))
        binding.heroCard.strokeColor = accent
        binding.secondaryCard.strokeColor = accent
        binding.xpBar.progressTintList = ColorStateList.valueOf(accent)
        binding.primaryAction.backgroundTintList = ColorStateList.valueOf(accent)
        binding.secondaryAction.backgroundTintList = ColorStateList.valueOf(accent)
        val tabs = arrayOf(
            binding.navSystem, binding.navMissions, binding.navTrain,
            binding.navShadows, binding.navAi, binding.navSettings
        )
        tabs.forEachIndexed { index, button ->
            button.alpha = if (currentScreen.ordinal == index) 1f else 0.58f
            button.strokeColor = ColorStateList.valueOf(accent)
        }
        binding.xpBar.progress = (state.xp % 200) * 100 / 200
        when (currentScreen) {
            Screen.SYSTEM -> renderSystem(state)
            Screen.MISSIONS -> renderMissions(state)
            Screen.TRAIN -> renderTrain(state)
            Screen.SHADOWS -> renderShadows(state)
            Screen.AI -> renderAi(state)
            Screen.SETTINGS -> renderSettings(state)
        }
    }

    private fun show(
        heading: String, subtitle: String, main: String, description: String,
        firstLabel: String, first: () -> Unit,
        section: String, body: String, secondLabel: String, second: () -> Unit,
        progressVisible: Boolean = false
    ) {
        binding.pageTitle.text = heading
        binding.pageSubtitle.text = subtitle
        binding.heroTitle.text = main
        binding.heroDetail.text = description
        binding.sectionTitle.text = section
        binding.sectionBody.text = body
        binding.primaryAction.text = firstLabel
        binding.primaryAction.setOnClickListener { first() }
        binding.secondaryAction.text = secondLabel
        binding.secondaryAction.setOnClickListener { second() }
        binding.xpBar.visibility = if (progressVisible) View.VISIBLE else View.GONE
    }

    private fun renderSystem(state: DashboardState) {
        show(
            "SISTEMA DEL CAZADOR",
            "Progreso simbólico del juego, no una evaluación de salud",
            "NIVEL " + state.level + " · RANGO " + state.rank,
            state.xp.toString() + " XP · " + state.sessions.size + " sesiones registradas",
            "CREAR RUTINA", { showPlaceDialog() },
            "MISIÓN DEL DÍA",
            "Entrena con el equipo disponible y registra solo lo que realmente realizaste. " +
                "Tu sombra acompañante es " + state.companion + ".",
            "VER MISIONES", { navigate(Screen.MISSIONS) },
            progressVisible = true
        )
    }

    private fun renderMissions(state: DashboardState) {
        val done = state.weeklySessions
        show(
            "MISIONES DEL CAZADOR",
            "Templo · Mazmorra · Portal · Consecuencias narrativas",
            "MISIÓN MONARCA · SEMANAL",
            "Esta semana: " + done + " sesiones registradas. " +
                "Desafío narrativo opcional: " + (if (done >= 3) "insignia disponible." else "progreso " + done.coerceAtMost(3) + "/3.") +
                " No hay castigos físicos por descanso o incumplimiento.",
            "GENERAR UNA SESIÓN", { showPlaceDialog() },
            "MISIONES DIARIAS",
            "Misiones adaptables, XP simbólico y recompensas virtuales. " +
                "La dificultad física no aumenta automáticamente como penalización.",
            "VER ENTRENAMIENTOS", { navigate(Screen.TRAIN) }
        )
    }

    private fun renderTrain(state: DashboardState) {
        val routine = state.routine
        val body = routine?.exercises?.joinToString(separator = "\n") { "• " + it.name }
            ?: "No hay una rutina activa. Elige Casa, Mancuernas o Gimnasio para crearla."
        show(
            "ENTRENAR",
            "Motor local de reglas · rutina editable en próximas entregas",
            routine?.title ?: "TU PRÓXIMA RUTINA",
            body,
            if (routine == null) "GENERAR RUTINA" else "REGISTRAR SESIÓN",
            if (routine == null) ({ showPlaceDialog() }) else ({ showEffortDialog() }),
            "CATÁLOGO DE EJERCICIOS",
            state.exercises.size.toString() +
                " fichas de demostración. Objetivo: 2.000 registros revisados. " +
                "Este prototipo todavía no contiene el catálogo definitivo.",
            "CAMBIAR MODALIDAD", { showPlaceDialog() }
        )
    }

    private fun renderShadows(state: DashboardState) {
        show(
            "SOMBRAS ACOMPAÑANTES",
            "Personajes mini · identidad visual en desarrollo",
            state.companion.uppercase() + " · SOMBRA ACTIVA",
            "Acompañante virtual decorativo. No detecta técnica ni actúa como entrenador profesional.",
            "CAMBIAR SOMBRA", { showCompanionDialog() },
            "SOMBRAS DISPONIBLES",
            "Beru · Igris · Iron · Tank · Tusk · Kaisel. " +
                "Las ilustraciones animadas PNG y el aura de fuego de sombras llegarán en la fase de recursos.",
            "IR AL ASISTENTE", { navigate(Screen.AI) }
        )
    }

    private fun renderAi(state: DashboardState) {
        show(
            "ASISTENTE SOMBRA",
            "100 % local en esta etapa · clasificación por reglas",
            "SALUDOS, CAZADOR",
            "Puedo consultar tus misiones, sesiones, XP y opciones de planificación. " +
                "No soy todavía una IA generativa ni un sistema médico.",
            "PREGUNTAR", { askAssistant() },
            "ESTADO DE LA INTELIGENCIA",
            "Reglas offline: operativas. " +
                "Aprendizaje estadístico: pendiente de suficientes sesiones comparables. " +
                "Modelo generativo offline: pendiente de compatibilidad y pruebas reales.",
            "GENERAR RUTINA", { showPlaceDialog() }
        )
    }

    private fun renderSettings(state: DashboardState) {
        val dark = preferences.getBoolean("dark_theme", true)
        show(
            "TEMA Y PERSONALIZACIÓN",
            "Dos temas · un color de acento independiente",
            if (dark) "TEMA OSCURO" else "TEMA CLARO",
            "Los colores de acento afectan bordes, marcos, botones y efectos. " +
                "Todas las letras de las pantallas principales usan negrita.",
            "CAMBIAR TEMA", { showThemeDialog() },
            "ACENTO Y ALARMAS",
            "Acento: " + (preferences.getString("accent_name", "Violeta")) +
                ".\n" + TrainingAlarms.label(this) +
                ".\nLas alarmas no son exactas; Android puede retrasarlas.",
            "CONFIGURAR", { showSettingsDialog() }
        )
    }

    private fun showPlaceDialog() {
        val names = arrayOf("En casa", "Con mancuernas", "En el gimnasio")
        AlertDialog.Builder(this)
            .setTitle("Selecciona equipamiento")
            .setItems(names) { _, which ->
                model.generateRoutine(TrainingPlace.entries[which])
                navigate(Screen.TRAIN)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEffortDialog() {
        var selected = 3
        val items = Array(10) { index -> (index + 1).toString() }
        AlertDialog.Builder(this)
            .setTitle("¿Cómo percibiste el esfuerzo?")
            .setSingleChoiceItems(items, selected) { _, index -> selected = index }
            .setMessage("Confirma únicamente los ejercicios que realmente completaste.")
            .setPositiveButton("Guardar sesión") { _, _ ->
                model.saveSession(selected + 1)
                Toast.makeText(this, "Sesión enviada al historial local", Toast.LENGTH_SHORT).show()
                navigate(Screen.SYSTEM)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askAssistant() {
        val input = EditText(this)
        input.hint = "Ej.: ¿Cómo va mi progreso?"
        AlertDialog.Builder(this)
            .setTitle("Consultar al asistente local")
            .setView(input)
            .setPositiveButton("Consultar") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Respuesta de tu sombra")
                    .setMessage(model.askAssistant(input.text.toString().take(300)))
                    .setPositiveButton("Entendido", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCompanionDialog() {
        val companions = arrayOf("Beru", "Igris", "Iron", "Tank", "Tusk", "Kaisel")
        AlertDialog.Builder(this).setTitle("Seleccionar sombra")
            .setItems(companions) { _, which -> model.chooseCompanion(companions[which]) }
            .show()
    }

    private fun showThemeDialog() {
        val current = if (preferences.getBoolean("dark_theme", true)) 1 else 0
        AlertDialog.Builder(this).setTitle("Seleccionar tema")
            .setSingleChoiceItems(arrayOf("Claro", "Oscuro"), current) { dialog, index ->
                preferences.edit().putBoolean("dark_theme", index == 1).apply()
                dialog.dismiss()
                AppCompatDelegate.setDefaultNightMode(
                    if (index == 1) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Personalización")
            .setItems(arrayOf("Cambiar color de acento", "Programar alarma diaria", "Desactivar alarma")) { _, index ->
                when (index) {
                    0 -> showAccentDialog()
                    1 -> showTimePicker()
                    else -> {
                        TrainingAlarms.disable(this)
                        render(model.state.value)
                    }
                }
            }.show()
    }

    private fun showAccentDialog() {
        AlertDialog.Builder(this).setTitle("Color de los marcos y efectos")
            .setItems(accentOptions) { _, index ->
                preferences.edit()
                    .putString("accent", accentValues[index])
                    .putString("accent_name", accentOptions[index])
                    .apply()
                render(model.state.value)
            }.show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            TrainingAlarms.setDaily(this, hour, minute)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
            render(model.state.value)
            Toast.makeText(this, "Alarma aproximada programada", Toast.LENGTH_LONG).show()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }
}
