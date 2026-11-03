package com.gymbreaker.app

import android.Manifest
import android.content.Intent
import com.gymbreaker.app.onboarding.HunterSetupStore
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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
import com.gymbreaker.app.ai.ShadowAuditCodec
import com.gymbreaker.app.ai.ShadowResourceMetricsReader
import com.gymbreaker.app.databinding.ActivityMainBinding
import com.gymbreaker.app.data.ResearchCatalogLoader
import com.gymbreaker.app.data.ResearchExerciseCandidate
import com.gymbreaker.app.data.ResearchFamilyGroupLoader
import com.gymbreaker.app.data.ProposedFamilyGroup
import com.gymbreaker.app.domain.CatalogCategory
import com.gymbreaker.app.domain.TrainingPlace
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            "Perfil: " + HunterSetupStore.load(this).hunterName.ifBlank { "Cazador" } +
                " · XP virtual, no evaluación de salud",
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
        val setup = HunterSetupStore.load(this)
        val target = setup.weeklyMissions.coerceIn(1, 7)
        show(
            "MISIONES DEL CAZADOR",
            "Templo · Mazmorra · Portal · Consecuencias narrativas",
            "MISIÓN MONARCA · SEMANAL",
            "Meta de misiones elegida: " + target +
                " de 7. Sesiones registradas esta semana: " + done + ". " +
                "Desafío narrativo opcional: " + (if (done >= 3) "insignia disponible." else "progreso " + done.coerceAtMost(3) + "/3.") +
                " No hay castigos físicos por descanso o incumplimiento.",
            "GENERAR UNA SESIÓN", { showPlaceDialog() },
            "TU SEMANA · " + setup.groups.count { it != "Descanso" } + " días de rutina",
            "Misiones configuradas: " + target + "/7. " +
                "Rutinas: " + setup.groups.joinToString(" · ") + ". " +
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
            "SHADOW CORE AI · CONTROL COMPLETO",
            "Sin Internet · reglas auditables · datos y variantes locales",
            "PANEL DEL CAZADOR",
            "Puedes consultar al asistente, revisar cómo se aplicaron los " +
                "filtros de tu última rutina, inspeccionar datos disponibles, " +
                "abrir las familias por nivel y exportar un informe JSON. " +
                "Las rutinas siguen un proceso verificable. El diálogo experimental " +
                "utiliza un modelo GGUF local en teléfonos ARM64 compatibles.",
            "CONSULTAR SOMBRA", { askAssistant() },
            "HERRAMIENTAS DE IA",
            "Seis etapas explicables: entrada, lectura de datos, " +
                "restricciones, selección de variantes, historial y decisión. " +
                "Presupuesto máximo de recursos offline: 90 MB. " +
                "Tu perfil y tus restricciones siguen bajo tu control.",
            "ABRIR PANEL DE CONTROL", { showAiControlPanel() }
        )
    }

    private fun renderSettings(state: DashboardState) {
        val dark = preferences.getBoolean("dark_theme", true)
        val setup = HunterSetupStore.load(this)
        show(
            "TEMA Y PERSONALIZACIÓN",
            "Dos temas · un color de acento independiente",
            if (dark) "TEMA OSCURO" else "TEMA CLARO",
            "Los colores de acento afectan bordes, marcos, botones y efectos. " +
                "Todas las letras de las pantallas principales usan negrita.",
            "CAMBIAR TEMA", { showThemeDialog() },
            "MI PERFIL · " + setup.hunterName.ifBlank { "Cazador" },
            "Objetivo: " + setup.primaryGoal + "\\nSubobjetivo: " +
                setup.subGoal.ifBlank { "Sin definir" } +
                "\\nMisiones semanales: " + setup.weeklyMissions + " de 7." +
                "\\nAcento: " + (preferences.getString("accent_name", "Violeta")) +
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
            "3 VARIANTES CON REFERENCIA TÉCNICA",
            "Consulta restringida a 3 denominaciones de nivel inicial " +
                "agrupadas en 3 familias propuestas y respaldadas por fichas ACE. " +
                "Otras 195 denominaciones con solo coincidencia entre catálogos " +
                "se retiraron de la APK y permanecen en auditoría. " +
                "Las variantes se agrupan por nivel y requieren aprobación " +
                "técnica antes de recomendarse automáticamente.",
            "VER GRUPOS Y NIVELES", { showGroupCategoryDialog() },
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
                .setMessage("Todavía no existen ejercicios nuevos con aprobación técnica " +
                    "documentada. El catálogo contiene 3 denominaciones con referencia " +
                    "técnica ACE en 3 familias propuestas. " +
                    "Shadow Core AI aún no las utiliza automáticamente.")
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

    private fun showGroupCategoryDialog() {
        val categories = CatalogCategory.entries
        AlertDialog.Builder(this)
            .setTitle("Familias de movimiento por categoría")
            .setItems(categories.map { it.label }.toTypedArray()) { _, index ->
                showGroupLevelDialog(categories[index])
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun showGroupLevelDialog(category: CatalogCategory) {
        val labels = arrayOf(
            "Todos los niveles", "Inicial", "Intermedio",
            "Avanzado · solo consulta", "Sin determinar"
        )
        val levels = arrayOf(null, "inicio", "intermedio", "avanzado", "sin_determinar")
        AlertDialog.Builder(this)
            .setTitle("Nivel de variantes · " + category.label)
            .setItems(labels) { _, index -> showGroupSearchDialog(category, levels[index]) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun showGroupSearchDialog(category: CatalogCategory, level: String?) {
        val input = EditText(this).apply {
            hint = "Buscar nombre o sinónimo (opcional)"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Buscar familias con referencia ACE")
            .setMessage("Solo se incluyen registros con referencias concordantes " +
                "y nivel documentado. La agrupación por familia sigue " +
                "pendiente de revisión técnica y no cambia las rutinas de IA.")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val all = ResearchFamilyGroupLoader.load(
                            this@MainActivity, category
                        )
                        val selected = ResearchFamilyGroupLoader.search(
                            all, input.text.toString().take(100), level
                        )
                        showGroupResults(selected, level)
                    } catch (error: Exception) {
                        if (!isFinishing && !isDestroyed) {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Familias no disponibles")
                                .setMessage("Los datos locales no se pudieron validar. " +
                                    "No se habilitarán variantes sin aprobación.")
                                .setPositiveButton("Cerrar", null).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun showGroupResults(groups: List<ProposedFamilyGroup>, level: String?) {
        if (isFinishing || isDestroyed) return
        if (groups.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Sin coincidencias")
                .setMessage("Prueba otra categoría, nivel o nombre.")
                .setPositiveButton("Cerrar", null).show()
            return
        }
        val shown = groups.take(40)
        val items = shown.map { group ->
            group.name + " · " + group.documentedVariantCount + " variantes"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Familias propuestas: " + groups.size)
            .setMessage("Se muestran las primeras 40 coincidencias. Las " +
                "familias sugeridas requieren revisión individual.")
            .setItems(items) { _, index ->
                val family = shown[index]
                AlertDialog.Builder(this)
                    .setTitle(family.name)
                    .setMessage(ResearchFamilyGroupLoader.preview(family, level))
                    .setPositiveButton("Cerrar", null)
                    .setNeutralButton("Ver todos sus niveles") { _, _ ->
                        AlertDialog.Builder(this)
                            .setTitle(family.name)
                            .setMessage(ResearchFamilyGroupLoader.preview(family, null))
                            .setPositiveButton("Cerrar", null).show()
                    }.show()
            }
            .setNegativeButton("Cerrar", null).show()
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
                    val busy = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Procesando consulta sin conexión")
                        .setMessage("La primera consulta puede preparar el modelo local de 88 MB.")
                        .setView(ProgressBar(this@MainActivity))
                        .setCancelable(false)
                        .create()
                    busy.show()
                    val response = try {
                        model.askShadowCore(question)
                    } finally {
                        busy.dismiss()
                    }
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

    private fun showAiControlPanel() {
        val actions = arrayOf(
            "Consultar al asistente offline",
            "Ver el proceso de la última decisión",
            "Ver cómo interpretó mi última pregunta",
            "Codificar y copiar un informe JSON",
            "Decodificar un informe JSON local",
            "Tamaño de recursos y datos de la IA",
            "Configurar dificultad de entrenamiento",
            "Explorar familias y variantes por nivel",
            "Ver los registros originales",
            "Limpiar la propuesta actual",
            "Acerca de las capacidades y límites"
        )
        AlertDialog.Builder(this)
            .setTitle("Control de Shadow Core AI")
            .setItems(actions) { _, index ->
                when (index) {
                    0 -> askAssistant()
                    1 -> showAiDecisionAudit()
                    2 -> AlertDialog.Builder(this)
                        .setTitle("Interpretación local de consulta")
                        .setMessage(model.lastInterpretationDetails())
                        .setPositiveButton("Cerrar", null).show()
                    3 -> copyAiAuditJson()
                    4 -> showAiDecodeDialog()
                    5 -> showAiResourceMetrics()
                    6 -> showRoutineDifficultyDialog()
                    7 -> showGroupCategoryDialog()
                    8 -> showResearchCategoryDialog()
                    9 -> AlertDialog.Builder(this)
                        .setTitle("Limpiar propuesta")
                        .setMessage("¿Descartar la propuesta y la última interpretación? " +
                            "Las sesiones y preferencias guardadas permanecerán intactas.")
                        .setPositiveButton("Limpiar") { _, _ ->
                            model.clearCurrentProposal()
                        }.setNegativeButton("Cancelar", null).show()
                    10 -> AlertDialog.Builder(this)
                        .setTitle("Capacidades y límites")
                        .setMessage(
                            "Shadow Core AI funciona sin conexión. Interpreta " +
                                "consultas habituales en español y utiliza reglas, " +
                                "estadísticas y variantes autorizadas. El historial " +
                                "se mantiene local. El catálogo de investigación " +
                                "no está habilitado para planificación. 90 MB es " +
                                "el límite de recursos, no una cantidad utilizada " +
                                "ni memoria RAM. Incluye un modelo GGUF cuantizado " +
                                "de aproximadamente 88 MB, solo para diálogo " +
                                "experimental en dispositivos ARM64. " +
                                "El planificador y sus restricciones no dependen " +
                                "de las respuestas generadas."
                        ).setPositiveButton("Cerrar", null).show()
                }
            }
            .setNegativeButton("Cerrar", null).show()
    }

    private fun showAiDecisionAudit() {
        AlertDialog.Builder(this)
            .setTitle("Proceso de decisión · reglas verificables")
            .setMessage(model.lastDecisionAudit().readable())
            .setNeutralButton("Copiar JSON") { _, _ -> copyAiAuditJson() }
            .setPositiveButton("Cerrar", null).show()
    }

    private fun copyAiAuditJson() {
        val report = model.lastDecisionAudit()
        val content = ShadowAuditCodec.encode(report)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("Shadow Core AI · Informe local", content)
        )
        Toast.makeText(
            this,
            "Informe JSON copiado por solicitud. Podría contener ajustes locales.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showAiDecodeDialog() {
        val input = EditText(this).apply {
            hint = "Pega el JSON generado por Shadow Core AI"
            minLines = 3
            maxLines = 7
        }
        AlertDialog.Builder(this)
            .setTitle("Decodificar informe local")
            .setMessage("El informe se interpreta en el dispositivo. " +
                "No cambiará las decisiones ni los ejercicios aprobados.")
            .setView(input)
            .setPositiveButton("Decodificar") { _, _ ->
                val result = try {
                    ShadowAuditCodec.decode(input.text.toString()).readable()
                } catch (error: Exception) {
                    "No se reconoce el formato o el informe supera el límite."
                }
                AlertDialog.Builder(this)
                    .setTitle("Informe descodificado")
                    .setMessage(result)
                    .setPositiveButton("Cerrar", null).show()
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun showAiResourceMetrics() {
        lifecycleScope.launch {
            val detail = try {
                ShadowResourceMetricsReader.read(this@MainActivity).description()
            } catch (error: Exception) {
                "Los recursos offline no pudieron verificarse. " +
                    "No se considera superada la prueba de integridad."
            }
            if (!isFinishing && !isDestroyed) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Información y presupuesto de la IA")
                    .setMessage(detail)
                    .setPositiveButton("Cerrar", null).show()
            }
        }
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
                "Dificultad deseada de las rutinas", "Licencias del catálogo offline",
                "Editar perfil, foto, objetivo y semana"
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
                    9 -> showCatalogLicenses()
                    10 -> startActivity(Intent(this, OnboardingActivity::class.java)
                        .putExtra("edit_profile", true))
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

    private fun showCatalogLicenses() {
        lifecycleScope.launch {
            val notices = withContext(Dispatchers.IO) {
                try {
                    assets.open("catalog_source_licenses.txt")
                        .bufferedReader().use { it.readText() }
                } catch (error: Exception) {
                    "No se pudo leer la información local de licencias."
                }
            }
            if (!isFinishing && !isDestroyed) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Fuentes y licencias")
                    .setMessage(notices)
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
        }
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
        val options = arrayOf(
            "Tren superior", "Tren inferior", "Zona central", "Cardio", "Movilidad",
            "Estabilidad", "Piernas", "Espalda", "Pecho", "Brazos", "Hombros", "Abdominales"
        )
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
