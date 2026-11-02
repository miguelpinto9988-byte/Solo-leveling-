package com.gymbreaker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity
import com.gymbreaker.app.data.SessionEntity
import com.gymbreaker.app.data.TrainingDatabase
import com.gymbreaker.app.data.TrainingProfileEntity
import com.gymbreaker.app.data.TrainingRepository
import com.gymbreaker.app.domain.*
import com.gymbreaker.app.ai.ShadowCoreAI
import com.gymbreaker.app.ai.ShadowIntent
import com.gymbreaker.app.ai.ShadowLearning
import com.gymbreaker.app.ai.ShadowDecisionAudit
import com.gymbreaker.app.ai.ShadowAuditReport
import com.gymbreaker.app.ai.ShadowInterpretation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun decodeVariantSelections(csv: String): Map<Int, String> =
    csv.split(';').mapNotNull { item ->
        val parts = item.split('=', limit = 2)
        if (parts.size != 2) return@mapNotNull null
        val id = parts[0].toIntOrNull() ?: return@mapNotNull null
        val variantId = parts[1]
        if (variantId.isBlank()) null else id to variantId
    }.toMap()

data class DashboardState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val variants: List<ExerciseVariantEntity> = emptyList(),
    val sessions: List<SessionEntity> = emptyList(),
    val profile: TrainingProfileEntity = TrainingProfileEntity(),
    val routine: PlannedRoutine? = null,
    val planningMessage: String? = null,
    val effortEstimate: LocalEffortEstimate? = null,
    val companion: String = "Beru",
    val xp: Int = 0,
    val level: Int = 1,
    val rank: String = "E",
    val weeklySessions: Int = 0
)

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("hunter_preferences", 0)
    private val repository = TrainingRepository(TrainingDatabase.get(application).trainingDao(), application.applicationContext)
    private val selectedCompanion = MutableStateFlow(prefs.getString("companion", "Beru") ?: "Beru")
    private val latestDecision = MutableStateFlow<PlanningDecision?>(null)
    private var lastAuditPreferences: TrainingPreferences? = null
    private var lastAuditDecision: PlanningDecision? = null
    private var lastInterpretation: ShadowInterpretation? = null

    private val localCatalog = combine(repository.exercises, repository.variants) {
            exercises, variants -> exercises to variants
    }

    val state: StateFlow<DashboardState> = combine(
        localCatalog, repository.sessions, repository.profile,
        selectedCompanion, latestDecision
    ) { catalog, sessions, savedProfile, companion, decision ->
        val (exercises, variants) = catalog
        val xp = sessions.sumOf { it.xpEarned }
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        DashboardState(
            exercises = exercises,
            variants = variants,
            sessions = sessions,
            profile = savedProfile ?: TrainingProfileEntity(),
            routine = (decision as? PlanningDecision.Candidate)?.routine,
            planningMessage = when (decision) {
                is PlanningDecision.ReviewRequired -> decision.message
                is PlanningDecision.Candidate -> decision.warnings.joinToString(" ")
                else -> null
            },
            effortEstimate = (decision as? PlanningDecision.Candidate)?.effortEstimate,
            companion = companion,
            xp = xp,
            level = HunterProgression.level(xp),
            rank = HunterProgression.rank(xp),
            weeklySessions = sessions.count { it.occurredAtMillis >= monday && it.confirmedComplete }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    init { viewModelScope.launch { repository.initializeSampleData() } }

    private fun observationsFrom(sessions: List<SessionEntity>): List<TrainingObservation> =
        sessions.asReversed().map { session ->
            TrainingObservation(
                equipment = TrainingPlace.entries.firstOrNull {
                    it.name == session.equipment
                } ?: TrainingPlace.HOME,
                exerciseIds = session.exerciseIdsCsv.split(',')
                    .mapNotNull { it.toIntOrNull() }.toSet(),
                perceivedEffort = session.perceivedEffort,
                confirmedComplete = session.confirmedComplete,
                reportedDiscomfort = session.reportedDiscomfort,
                selectedVariants = decodeVariantSelections(session.variantSelectionsCsv)
            )
        }

    fun lastDecisionAudit(): ShadowAuditReport {
        val s = state.value
        val p = s.profile
        val fallback = TrainingPreferences(
            equipment = s.routine?.place ?: TrainingPlace.HOME,
            experience = Experience.entries.firstOrNull { it.name == p.experience }
                ?: Experience.BEGINNER,
            availableMinutes = p.availableMinutes,
            excludedCategories = p.excludedCategoriesCsv.split(',')
                .map { it.trim() }.filter { it.isNotBlank() }.toSet(),
            excludedExerciseIds = p.excludedExerciseIdsCsv.split(',')
                .mapNotNull { it.toIntOrNull() }.toSet(),
            requiresReview = p.requiresReview,
            preferredDifficulty = p.preferredDifficulty
        )
        return ShadowDecisionAudit.evaluate(
            exercises = s.exercises,
            variants = s.variants,
            preferences = lastAuditPreferences ?: fallback,
            observations = observationsFrom(s.sessions),
            decision = latestDecision.value ?: lastAuditDecision
        )
    }

    fun lastInterpretationDetails(): String {
        val r = lastInterpretation
            ?: return "Aún no se ha interpretado ninguna consulta en esta sesión."
        return "INTENCIÓN: " + r.intent.name +
            "\nCoincidencia léxica orientativa: " +
            "%.1f".format(r.confidence * 100.0) + "%" +
            "\nEquipamiento detectado: " +
            (r.parameters.preferredPlace?.name ?: "No especificado") +
            "\nTiempo detectado: " +
            (r.parameters.minutes?.toString() ?: "No indicado") + " min." +
            "\nCategorías detectadas: " +
            r.parameters.focusCategories.ifEmpty { setOf("Ninguna") }.joinToString(", ") +
            "\nRevisión pendiente: " + (if (r.needsHumanReview) "Sí" else "No")
    }

    fun clearCurrentProposal() {
        latestDecision.value = null
        lastAuditPreferences = null
        lastAuditDecision = null
        lastInterpretation = null
    }

    fun generateRoutine(place: TrainingPlace) {
        val s = state.value
        val p = s.profile
        val experience = Experience.entries.firstOrNull { it.name == p.experience }
            ?: Experience.BEGINNER
        val restrictions = p.excludedCategoriesCsv.split(',').map { it.trim() }
            .filter { it.isNotBlank() }.toSet()
        val excludedIds = p.excludedExerciseIdsCsv.split(',').mapNotNull { it.toIntOrNull() }.toSet()
        val observations = s.sessions.asReversed().map { session ->
            TrainingObservation(
                equipment = TrainingPlace.entries.firstOrNull { it.name == session.equipment }
                    ?: TrainingPlace.HOME,
                exerciseIds = session.exerciseIdsCsv.split(',').mapNotNull { it.toIntOrNull() }.toSet(),
                perceivedEffort = session.perceivedEffort,
                confirmedComplete = session.confirmedComplete,
                reportedDiscomfort = session.reportedDiscomfort,
                selectedVariants = decodeVariantSelections(session.variantSelectionsCsv)
            )
        }
        val preferences = TrainingPreferences(
            place, experience, p.availableMinutes, restrictions,
            excludedIds, p.requiresReview, preferredDifficulty = p.preferredDifficulty
        )
        lastAuditPreferences = preferences
        val decision = OfflineTrainingIntelligence.propose(
            s.exercises, preferences, observations, variants = s.variants
        )
        latestDecision.value = decision
        lastAuditDecision = decision
    }

    fun saveSession(effort: Int, confirmedExerciseIds: Set<Int>, discomfort: Boolean = false) {
        val routine = (latestDecision.value as? PlanningDecision.Candidate)?.routine ?: return
        val selected = routine.exercises.filter { it.id in confirmedExerciseIds }
        if (selected.isEmpty() || effort !in 1..10) return
        viewModelScope.launch {
            repository.completeSession(
                summary = selected.joinToString(", ") {
                    routine.selectedVariants[it.id]?.displayName ?: it.name
                }, effort = effort,
                equipment = routine.place.name, exerciseIds = selected.map { it.id },
                selectedVariantIds = selected.associate { exercise ->
                    exercise.id to (routine.selectedVariants[exercise.id]?.variantId ?: "__base__")
                },
                confirmed = true, discomfort = discomfort
            )
            if (discomfort) repository.saveProfile(state.value.profile.copy(requiresReview = true))
            latestDecision.value = null
        }
    }

    fun savePreferences(
        experience: Experience? = null,
        preferredDifficulty: String? = null,
        availableMinutes: Int? = null,
        excludedCategories: Set<String>? = null,
        requiresReview: Boolean? = null
    ) {
        viewModelScope.launch {
            val original = state.value.profile
            repository.saveProfile(original.copy(
                experience = experience?.name ?: original.experience,
                preferredDifficulty = preferredDifficulty ?: original.preferredDifficulty,
                availableMinutes = (availableMinutes ?: original.availableMinutes).coerceIn(10, 90),
                excludedCategoriesCsv = excludedCategories?.joinToString(",")
                    ?: original.excludedCategoriesCsv,
                requiresReview = requiresReview ?: original.requiresReview
            ))
            latestDecision.value = null
            lastAuditDecision = null
            lastAuditPreferences = null
        }
    }

    fun chooseCompanion(name: String) {
        if (name !in setOf("Beru", "Igris", "Iron", "Tank", "Tusk", "Kaisel")) return
        prefs.edit().putString("companion", name).apply()
        selectedCompanion.value = name
    }

    /**
     * El asistente nunca omite el planificador validado. El texto del usuario
     * no modifica permanentemente el perfil sin una acción explícita en Ajustes.
     */
    fun askAssistant(question: String): String {
        val parsed = ShadowCoreAI.interpret(question)
        lastInterpretation = parsed
        val s = state.value
        return when (parsed.intent) {
            ShadowIntent.RECOVERY ->
                "He detectado una posible molestia, restricción o necesidad de descanso. " +
                    "No generaré una rutina a partir de esta consulta. " +
                    "Revisa tus exclusiones en Ajustes y evita movimientos que provoquen dolor. " +
                    "Si las molestias continúan, consulta a un profesional adecuado."

            ShadowIntent.ROUTINE -> {
                val requested = parsed.parameters
                val minutes = requested.minutes
                if (minutes != null && minutes !in 10..90) {
                    "El tiempo solicitado queda fuera del intervalo disponible (10 a 90 minutos). " +
                        "Indica un tiempo válido o cámbialo en Ajustes."
                } else {
                    val p = s.profile
                    val experience = Experience.entries.firstOrNull { it.name == p.experience }
                        ?: Experience.BEGINNER
                    val exclusions = p.excludedCategoriesCsv.split(',')
                        .map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                    val excludedIds = p.excludedExerciseIdsCsv.split(',')
                        .mapNotNull { it.toIntOrNull() }.toSet()
                    val observed = s.sessions.asReversed().map { session ->
                        TrainingObservation(
                            equipment = TrainingPlace.entries.firstOrNull {
                                it.name == session.equipment
                            } ?: TrainingPlace.HOME,
                            exerciseIds = session.exerciseIdsCsv.split(',')
                                .mapNotNull { it.toIntOrNull() }.toSet(),
                            perceivedEffort = session.perceivedEffort,
                            confirmedComplete = session.confirmedComplete,
                            reportedDiscomfort = session.reportedDiscomfort,
                            selectedVariants = decodeVariantSelections(session.variantSelectionsCsv)
                        )
                    }
                    val choice = requested.preferredPlace ?: TrainingPlace.HOME
                    val preferences = TrainingPreferences(
                        equipment = choice,
                        experience = experience,
                        availableMinutes = minutes ?: p.availableMinutes,
                        excludedCategories = exclusions,
                        excludedExerciseIds = excludedIds,
                        requiresReview = p.requiresReview,
                        requestedCategories = requested.focusCategories,
                        preferredDifficulty = p.preferredDifficulty
                    )
                    lastAuditPreferences = preferences
                    val decision = OfflineTrainingIntelligence.propose(
                        exercises = s.exercises,
                        profile = preferences,
                        observations = observed,
                        variants = s.variants
                    )
                    latestDecision.value = decision
                    lastAuditDecision = decision
                    when (decision) {
                        is PlanningDecision.Candidate ->
                            "He preparado una propuesta offline con " +
                                decision.routine.exercises.size +
                                " ejercicios. Está disponible en Entrenar. " +
                                "Debes revisarla y confirmar cada ejercicio realizado. " +
                                decision.warnings.joinToString(" ")
                        is PlanningDecision.ReviewRequired -> decision.message
                    }
                }
            }

            ShadowIntent.PROGRESS -> {
                val observed = s.sessions.asReversed().map { session ->
                    TrainingObservation(
                        equipment = TrainingPlace.entries.firstOrNull {
                            it.name == session.equipment
                        } ?: TrainingPlace.HOME,
                        exerciseIds = session.exerciseIdsCsv.split(',')
                            .mapNotNull { it.toIntOrNull() }.toSet(),
                        perceivedEffort = session.perceivedEffort,
                        confirmedComplete = session.confirmedComplete,
                        reportedDiscomfort = session.reportedDiscomfort
                    )
                }
                val insight = ShadowLearning.analyze(observed)
                "Tienes " + s.sessions.count { it.confirmedComplete } +
                    " sesiones confirmadas, " + s.xp + " XP virtuales, nivel " +
                    s.level + " y rango " + s.rank + ". " +
                    insight.trend + " (" + insight.comparableSamples +
                    " sesiones evaluables). " + insight.recommendation
            }

            ShadowIntent.MISSIONS ->
                "Esta semana llevas " + s.weeklySessions +
                    " sesiones registradas. Las misiones Monarca ofrecen recompensas " +
                    "virtuales; no aplican incrementos físicos por fallar."

            ShadowIntent.RESTRICTIONS ->
                "Las restricciones y exclusiones se configuran en Ajustes. " +
                    "No modifico ese perfil automáticamente desde el chat. " +
                    "Los ejercicios excluidos nunca se incluyen en propuestas."

            ShadowIntent.EQUIPMENT ->
                "Selecciona Casa, Mancuernas o Gimnasio al crear una rutina. " +
                    "Puedes cambiar el tiempo y el nivel declarado desde Ajustes. " +
                    "La solicitud no cambia tus preferencias sin confirmación."

            ShadowIntent.CATALOG ->
                "El catálogo offline de investigación reúne 2.186 registros " +
                    "en 1.823 nombres por categoría y 1.089 agrupaciones propuestas. " +
                    "Las variantes se presentan por dificultad y procedencia. " +
                    "Entra en Catálogo para buscar y revisar. Ninguna de esas " +
                    "agrupaciones es automáticamente un ejercicio aprobado."

            ShadowIntent.EXPLANATION -> lastDecisionAudit().readable()

            ShadowIntent.SYSTEM ->
                "Shadow Core AI opera localmente con reglas, historial y " +
                    "decodificación JSON. El límite de recursos de la IA es " +
                    "90 MB de almacenamiento (no RAM). En el Panel de Control " +
                    "puedes medir los archivos incluidos, inspeccionar la " +
                    "última decisión y copiar o decodificar su informe. " +
                    "No se ha instalado un modelo generativo general."

            ShadowIntent.GREETING -> ShadowCoreAI.limitationMessage()
            ShadowIntent.UNKNOWN -> ShadowCoreAI.limitationMessage() +
                " Puedes escribir, por ejemplo: «Rutina de espalda en casa de 30 minutos»."
        }
    }

    suspend fun askShadowCore(question: String): String = askAssistant(question)
}
