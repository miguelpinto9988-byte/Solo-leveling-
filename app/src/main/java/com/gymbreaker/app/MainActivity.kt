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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gymbreaker.app.alarms.TrainingAlarms
import com.gymbreaker.app.databinding.ActivityMainBinding
import com.gymbreaker.app.data.ResearchCatalogLoader
import com.gymbreaker.app.data.ResearchExerciseCandidate
import com.gymbreaker.app.domain.CatalogCategory
import com.gymbreaker.app.domain.TrainingPlace
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class Screen { SYSTEM, MISSIONS, TRAIN, SHADOWS, AI, SETTINGS, CATALOG }

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
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
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
        binding.navCatalog.setOnClickListener { navigate(Screen.CATALOG) }
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
        val companionImage = when (state.companion) {
            "Igris" -> R.drawable.shadow_igris
            "Iron" -> R.drawable.shadow_iron
            "Tank" -> R.drawable.shadow_tank
            "Tusk" -> R.drawable.shadow_tusk
            "Kaisel" -> R.drawable.shadow_kaisel
            else -> R.drawable.shadow_beru
        }
        binding.shadowPortrait.setImageResource(companionImage)
        binding.shadowPortrait.contentDescription = "Sombra " + state.companion
        binding.heroCard.strokeColor = accent
        binding.secondaryCard.strokeColor = accent
        binding.xpBar.progressTintList = ColorStateList.valueOf(accent)
        binding.primaryAction.backgroundTintList = ColorStateList.valueOf(accent)
        binding.secondaryAction.backgroundTintList = ColorStateList.valueOf(accent)
        val tabs = arrayOf(
            binding.navSystem, binding.navMissions, binding.navTrain,
            binding.navShadows, binding.navAi, binding.navSettings, binding.navCatalog
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
            Screen.CATALOG -> renderCatalog(state)
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
        val body = if (routine != null) {
            routine.exercises.joinToString(separator = "\n") {
                val form = routine.selectedVariants[it.id]
                "• " + (form?.displayName ?: it.name) +
                    (form?.let { choice -> " · " + choice.level } ?: "")
            } +
                "\n\n" + (state.planningMessage ?: "") +
                (state.effortEstimate?.let {
                    "\nEstimación local con " + it.sampleSize + " sesiones: esfuerzo declarado medio " +
                        String.format("%.1f", it.averagePerceivedEffort) + "/10."
                } ?: "\nSin suficientes sesiones comparables para una estimación.")
        } else state.planningMessage
            ?: "No hay una rutina activa. Elige Casa, Mancuernas o Gimnasio para crearla."
        show(
            "ENTRENAR",
            "Dificultad deseada: " + state.profile.preferredDifficulty +
                " · Experiencia: " + state.profile.experience +
                " · solo variantes compatibles y autorizadas",
            routine?.title ?: "TU PRÓXIMA RUTINA",
            body,
            if (routine == null) "GENERAR RUTINA" else "REGISTRAR SESIÓN",
            if (routine == null) ({ showPlaceDialog() }) else ({ showCompletedExerciseDialog() }),
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
            "SHADOW CORE AI · OFFLINE",
            "Motor propio de interpretación y aprendizaje estadístico local",
            "SALUDOS, CAZADOR",
            "Comprendo solicitudes de rutinas en español, consulto tus sesiones " +
                "y explico decisiones sujetas a tus restricciones. " +
                "No dependo de conexión a Internet ni de un modelo externo.",
            "CONSULTAR SOMBRA", { askAssistant() },
            "NÚCLEO INTELIGENTE",
            "Interpretación local de lenguaje y parámetros: activa. " +
                "Planificación con filtros obligatorios: activa. " +
                "Análisis de esfuerzo con al menos cinco registros: disponible. " +
                "No soy un modelo de conversación libre.",
            "IR A ENTRENAR", { navigate(Screen.TRAIN) }
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
                ".\nPerfil: " + state.profile.experience +
                " · Dificultad: " + state.profile.preferredDifficulty +
                " · " + state.profile.availableMinutes + " min.\nExclusiones: " +
                state.profile.excludedCategoriesCsv.ifEmpty { "ninguna" } +
                (if (state.profile.requiresReview) "\nRestricción pendiente: no generar rutinas automáticamente." else "") +
                "\n" + TrainingAlarms.label(this) +
                ".\nLas alarmas no son exactas; Android puede retrasarlas.",
            "CONFIGURAR", { showSettingsDialog() }
        )
    }

    private fun renderCatalog(state: DashboardState) {
        val approved = state.exercises.count { it.id >= 10_000 }
        show(
            "CATÁLOGO DE FAMILIAS",
            "Ejercicio principal · variantes · dificultad · equipamiento",
            "2.186 REGISTROS DOCUMENTALES",
            "Investigación offline organizada en 1.823 grupos nominales de las " +
                "cinco categorías. Las posibles variantes se muestran dentro " +
                "de grupos relacionados, pero su identidad aún requiere revisión. " +
                "No son 2.000 ejercicios únicos acreditados.",
            "EXPLORAR FAMILIAS", { showResearchCategoryDialog() },
            "FICHAS AUTORIZADAS PARA LA IA",
            approved.toString() + " familias nuevas importadas. " +
                state.variants.size + " variantes subordinadas en la base de datos. " +
                "Solo los niveles y las variantes aprobados pueden proponerse " +
                "en Entrenar; las entradas documentales quedan en consulta.",
            "VER FAMILIAS APROBADAS", { showApprovedFamiliesDialog() }
        )
    }

    private fun showApprovedFamiliesDialog() {
        val state = model.state.value
        val approved = state.exercises.filter { it.id >= 10_000 }
        if (approved.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Familias autorizadas")
                .setMessage("Todavía no existen ejercicios nuevos con revisión técnica " +
                    "documentada. Los 2.186 registros de investigación pueden consultarse " +
                    "en el catálogo, pero Shadow Core AI no los utiliza.")
                .setPositiveButton("Entendido", null).show()
            return
        }
        AlertDialog.Builder(this).setTitle("Ejercicios principales aprobados")
            .setItems(approved.map { it.name }.toTypedArray()) { _, index ->
                val family = approved[index]
                val variants = state.variants.filter { it.exerciseId == family.id }
                val details = if (variants.isEmpty()) "No tiene variantes aprobadas." else
                    variants.joinToString("\n") {
                        "• " + it.name + " · " + it.difficulty +
                            " · " + it.equipment +
                            " · " + if (it.planningEligibility == "approved")
                                "Elegible" else "Solo consulta"
                    }
                AlertDialog.Builder(this).setTitle(family.name)
                    .setMessage("Familia única · " + family.category +
                        "\nNivel base: " + family.level + "\n\n" + details)
                    .setPositiveButton("Cerrar", null).show()
            }.setNegativeButton("Cerrar", null).show()
    }

    private fun showResearchCategoryDialog() {
        val categories = CatalogCategory.entries
        AlertDialog.Builder(this).setTitle("Selecciona una categoría")
            .setItems(categories.map { it.label }.toTypedArray()) { _, index ->
                showResearchDifficultyDialog(categories[index])
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showResearchDifficultyDialog(category: CatalogCategory) {
        val labels = arrayOf("Todas", "Inicial (informado)", "Intermedio (informado)",
            "Avanzado (informado)", "Sin determinar")
        val levels = arrayOf(null, "inicio", "intermedio", "avanzado", "sin_determinar")
        AlertDialog.Builder(this).setTitle("Dificultad · " + category.label)
            .setItems(labels) { _, index -> showResearchSearchDialog(category, levels[index]) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun showResearchSearchDialog(category: CatalogCategory, difficulty: String?) {
        val input = EditText(this).apply {
            hint = "Nombre de ejercicio o familia (opcional)"
            setSingleLine(true)
        }
        AlertDialog.Builder(this).setTitle("Buscar en " + category.label)
            .setMessage("Datos de investigación. Dificultades declaradas por las fuentes, " +
                "no comprobadas para uso automático.")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val families = ResearchCatalogLoader.load(
                            this@MainActivity, category
                        )
                        showResearchResults(
                            families,
                            ResearchCatalogLoader.search(
                                families, input.text.toString().take(120), difficulty
                            )
                        )
                    } catch (error: Exception) {
                        if (!isFinishing && !isDestroyed) {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Catálogo no disponible")
                                .setMessage("No fue posible abrir los datos offline. " +
                                    "Las rutinas existentes permanecen disponibles.")
                                .setPositiveButton("Cerrar", null).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun showResearchResults(
        all: List<ResearchExerciseCandidate>,
        matches: List<ResearchExerciseCandidate>
    ) {
        if (isFinishing || isDestroyed) return
        if (matches.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Sin coincidencias")
                .setMessage("No se encontraron nombres con esos criterios. " +
                    "Prueba otra dificultad o búsqueda.")
                .setPositiveButton("Cerrar", null).show()
            return
        }
        val visible = matches.take(40)
        val labels = visible.map {
            it.name + " · " + it.records.size + " entradas de origen"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Familias nominales · " + matches.size + " resultados")
            .setMessage("Se muestran hasta 40. Afina la búsqueda para encontrar otras. " +
                "El catálogo aún está en revisión.")
            .setItems(labels) { _, index ->
                val selected = visible[index]
                AlertDialog.Builder(this)
                    .setTitle(selected.name)
                    .setMessage(ResearchCatalogLoader.preview(selected, all))
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
            .setNegativeButton("Cerrar", null)
            .show()
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

    private fun showCompletedExerciseDialog() {
        val routine = model.state.value.routine ?: return
        val names = routine.exercises.map { exercise ->
            routine.selectedVariants[exercise.id]?.displayName ?: exercise.name
        }.toTypedArray()
        val checked = BooleanArray(names.size)
        AlertDialog.Builder(this)
            .setTitle("Confirma solo los ejercicios realizados")
            .setMultiChoiceItems(names, checked) { _, index, value -> checked[index] = value }
            .setPositiveButton("Continuar") { _, _ ->
                val ids = routine.exercises.filterIndexed { index, _ -> checked[index] }
                    .map { it.id }.toSet()
                if (ids.isEmpty()) {
                    Toast.makeText(this, "No se registraron ejercicios sin confirmar", Toast.LENGTH_LONG).show()
                } else showEffortDialog(ids)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEffortDialog(completed: Set<Int>) {
        var selected = 3
        val items = Array(10) { index -> (index + 1).toString() }
        AlertDialog.Builder(this)
            .setTitle("Esfuerzo percibido (1-10)")
            .setSingleChoiceItems(items, selected) { _, index -> selected = index }
            .setPositiveButton("Continuar") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Comprobación final")
                    .setMessage("¿Tuviste molestias o dolor durante la sesión?")
                    .setPositiveButton("Sí") { _, _ ->
                        model.saveSession(selected + 1, completed, discomfort = true)
                        Toast.makeText(this, "Sesión registrada; planificación automática en revisión", Toast.LENGTH_LONG).show()
                        navigate(Screen.SYSTEM)
                    }
                    .setNegativeButton("No") { _, _ ->
                        model.saveSession(selected + 1, completed)
                        navigate(Screen.SYSTEM)
                    }
                    .setNeutralButton("Cancelar", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askAssistant() {
        val input = EditText(this).apply { hint = "Consulta sobre entrenamiento o progreso" }
        AlertDialog.Builder(this)
            .setTitle("Consultar a la sombra local")
            .setView(input)
            .setPositiveButton("Consultar") { _, _ ->
                val question = input.text.toString().trim().take(400)
                if (question.isBlank()) return@setPositiveButton
                lifecycleScope.launch {
                    val response = model.askShadowCore(question)
                    if (!isFinishing && !isDestroyed) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Respuesta del asistente offline")
                            .setMessage(response)
                            .setPositiveButton("Entendido", null)
                            .show()
                    }
                }
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
        AlertDialog.Builder(this).setTitle("Personalización y restricciones")
            .setItems(arrayOf(
                "Cambiar color de acento", "Programar alarma diaria", "Desactivar alarma",
                "Experiencia de entrenamiento", "Tiempo disponible", "Excluir categorías",
                "Marcar molestia o restricción pendiente", "Revisar bloqueo de planificación",
                "Dificultad deseada de las rutinas"
            )) { _, index ->
                when (index) {
                    0 -> showAccentDialog()
                    1 -> showTimePicker()
                    2 -> { TrainingAlarms.disable(this); render(model.state.value) }
                    3 -> showExperienceDialog()
                    4 -> showMinutesDialog()
                    5 -> showCategoriesDialog()
                    6 -> model.savePreferences(requiresReview = true)
                    7 -> showReviewDialog()
                    8 -> showRoutineDifficultyDialog()
                }
            }.show()
    }

    private fun showExperienceDialog() {
        val options = arrayOf("Inicial", "Intermedio", "Con experiencia")
        AlertDialog.Builder(this).setTitle("Experiencia declarada")
            .setItems(options) { _, i ->
                model.savePreferences(experience = com.gymbreaker.app.domain.Experience.entries[i])
            }.show()
    }

    private fun showRoutineDifficultyDialog() {
        val options = arrayOf(
            "Inicial", "Intermedio",
            "Avanzado (no habilitado para selección automática)"
        )
        val levels = arrayOf("inicio", "intermedio", "avanzado")
        AlertDialog.Builder(this)
            .setTitle("Dificultad deseada de tus rutinas")
            .setMessage(
                "La dificultad seleccionada es un máximo, no una progresión forzada. " +
                "La IA también respetará tu experiencia, equipo y restricciones. " +
                "Las variantes avanzadas estarán solo para consulta hasta revisión."
            )
            .setItems(options) { _, index ->
                model.savePreferences(preferredDifficulty = levels[index])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showMinutesDialog() {
        val minutes = arrayOf(15, 20, 25, 30, 40, 50, 60)
        AlertDialog.Builder(this).setTitle("Tiempo disponible (minutos)")
            .setItems(minutes.map { it.toString() }.toTypedArray()) { _, i ->
                model.savePreferences(availableMinutes = minutes[i])
            }.show()
    }

    private fun showCategoriesDialog() {
        val options = arrayOf("Cardio", "Movilidad", "Estabilidad", "Piernas", "Espalda", "Brazos", "Hombros")
        val current = model.state.value.profile.excludedCategoriesCsv
            .split(',').filter { it.isNotBlank() }.toSet()
        val excluded = current.toMutableSet()
        val checked = BooleanArray(options.size) { options[it] in current }
        AlertDialog.Builder(this).setTitle("Categorías excluidas de las rutinas")
            .setMultiChoiceItems(options, checked) { _, index, excludedNow ->
                if (excludedNow) excluded.add(options[index]) else excluded.remove(options[index])
            }
            .setPositiveButton("Guardar exclusiones") { _, _ ->
                model.savePreferences(excludedCategories = excluded)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showReviewDialog() {
        AlertDialog.Builder(this)
            .setTitle("Revisión de restricciones")
            .setMessage(
                "La planificación se bloquea ante molestias declaradas. " +
                "Solo confirma la revisión si tus condiciones actuales lo permiten. " +
                "La app no puede comprobar la recuperación ni reemplaza una evaluación profesional."
            )
            .setPositiveButton("Confirmar revisión") { _, _ ->
                model.savePreferences(requiresReview = false)
            }
            .setNegativeButton("Mantener bloqueo", null)
            .show()
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
