package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity

/** Motor offline de decisiones revisables. No diagnostica ni reemplaza a un profesional. */
enum class Experience { BEGINNER, INTERMEDIATE, EXPERIENCED }

data class TrainingPreferences(
    val equipment: TrainingPlace,
    val experience: Experience = Experience.BEGINNER,
    val availableMinutes: Int = 25,
    val excludedCategories: Set<String> = emptySet(),
    val excludedExerciseIds: Set<Int> = emptySet(),
    val requiresReview: Boolean = false,
    val requestedCategories: Set<String> = emptySet(),
    val preferredDifficulty: String = "inicio"
) {
    fun validated(): TrainingPreferences = copy(availableMinutes = availableMinutes.coerceIn(10, 90))
}

data class TrainingObservation(
    val equipment: TrainingPlace,
    val exerciseIds: Set<Int>,
    val perceivedEffort: Int,
    val confirmedComplete: Boolean,
    val reportedDiscomfort: Boolean = false,
    val selectedVariants: Map<Int, String> = emptyMap()
)

data class LocalEffortEstimate(
    val sampleSize: Int,
    val averagePerceivedEffort: Double,
    val explanation: String
)

sealed interface PlanningDecision {
    data class Candidate(
        val routine: PlannedRoutine,
        val warnings: List<String>,
        val effortEstimate: LocalEffortEstimate?
    ) : PlanningDecision
    data class ReviewRequired(val message: String) : PlanningDecision
}

object OfflineTrainingIntelligence {
    private const val MIN_COMPARABLE_SAMPLES = 5

    fun estimateEffort(
        observations: List<TrainingObservation>,
        equipment: TrainingPlace,
        suggestedIds: Set<Int>
    ): LocalEffortEstimate? {
        if (suggestedIds.isEmpty()) return null
        val comparable = observations.filter { sample ->
            sample.equipment == equipment && sample.confirmedComplete &&
                !sample.reportedDiscomfort && sample.perceivedEffort in 1..10 &&
                sample.exerciseIds.isNotEmpty() &&
                sample.exerciseIds.intersect(suggestedIds).size * 2 >=
                    maxOf(sample.exerciseIds.size, suggestedIds.size)
        }
        if (comparable.size < MIN_COMPARABLE_SAMPLES) return null
        val closest = comparable.takeLast(10)
        return LocalEffortEstimate(
            sampleSize = closest.size,
            averagePerceivedEffort = closest.map { it.perceivedEffort }.average(),
            explanation = "Estimación orientativa del esfuerzo declarado, no una predicción de salud."
        )
    }

    fun propose(
        exercises: List<ExerciseEntity>,
        profile: TrainingPreferences,
        observations: List<TrainingObservation> = emptyList(),
        variants: List<ExerciseVariantEntity> = emptyList()
    ): PlanningDecision {
        val p = profile.validated()
        if (p.requiresReview) {
            return PlanningDecision.ReviewRequired(
                "Hay una restricción o molestia pendiente de revisión. " +
                "No se genera una rutina automáticamente hasta confirmar ejercicios compatibles."
            )
        }
        if (observations.takeLast(2).count { it.perceivedEffort >= 8 } >= 2) {
            return PlanningDecision.ReviewRequired(
                "Los últimos registros indican esfuerzo elevado. " +
                "Descansa o revisa una propuesta más ligera antes de continuar."
            )
        }
        // Frecuencias de variantes realmente confirmadas (últimas diez sesiones
        // sin molestias). Solo desempatan alternativas del mismo nivel.
        val selectionCounts = observations.takeLast(10)
            .filter { it.confirmedComplete && !it.reportedDiscomfort }
            .flatMap { observation ->
                observation.selectedVariants.map { (familyId, variantId) ->
                    familyId.toString() + ":" + variantId
                }
            }.groupingBy { it }.eachCount()

        // Seleccionar previamente una realización compatible para cada familia.
        // Una variante sólo entra aquí si tiene nivel y autorización explícitos.
        val variantsByFamily = variants.groupBy { it.exerciseId }
        val availableChoices = exercises.asSequence()
            .filter { it.id !in p.excludedExerciseIds }
            .filter { candidate ->
                val mainCategory = CatalogTaxonomy
                    .forCurrentSubcategory(candidate.category)?.label
                p.excludedCategories.none { excluded ->
                    excluded.equals(candidate.category, ignoreCase = true) ||
                        (mainCategory != null &&
                            excluded.equals(mainCategory, ignoreCase = true))
                }
            }
            .mapNotNull { exercise ->
                val choice = ExerciseVariantSelector.choose(
                    exercise, variantsByFamily[exercise.id].orEmpty(),
                    p.equipment, p.experience,
                    preferredDifficulty = p.preferredDifficulty,
                    recentSelections = selectionCounts
                )
                if (choice == null) null else exercise.id to choice
            }
            .toMap()
        val eligible = exercises.filter { it.id in availableChoices }

        if (eligible.size < 2) return PlanningDecision.ReviewRequired(
            "No hay suficientes ejercicios compatibles y revisados. " +
            "No se omitirán restricciones declaradas."
        )
        val categories = listOf(
            "Movilidad", "Cardio", "Estabilidad", "Zona central", "Abdominales",
            "Piernas", "Glúteos", "Pantorrillas", "Espalda", "Pecho",
            "Brazos", "Hombros", "Antebrazos"
        )
        val requested = categories.filter { cat ->
            p.requestedCategories.any { it.equals(cat, ignoreCase = true) }
        }
        if (p.requestedCategories.isNotEmpty() &&
            requested.none { cat -> eligible.any { it.category.equals(cat, ignoreCase = true) } }
        ) return PlanningDecision.ReviewRequired(
            "El catálogo disponible no contiene ejercicios compatibles para ese objetivo. " +
                "Revisa el equipo, las restricciones y la selección solicitada."
        )
        // El foco solicitado tiene prioridad, pero no anula exclusiones ni experiencia.
        // La movilidad complementaria solo se incluye si está permitida.
        val orderedCategories = (requested + categories + eligible.map { it.category }).distinct()
        // Personalización local simple: da variedad a ejercicios compatibles
        // según el uso reciente; nunca cambia automáticamente cargas ni repeticiones.
        val recentUsage = observations.takeLast(10).asSequence()
            .filter { it.confirmedComplete && !it.reportedDiscomfort }
            .flatMap { it.exerciseIds.asSequence() }
            .groupingBy { it }.eachCount()
        val chosen = orderedCategories.mapNotNull { cat ->
            eligible.filter { it.category.equals(cat, ignoreCase = true) }
                .minWithOrNull(compareBy<ExerciseEntity> { recentUsage[it.id] ?: 0 }
                    .thenBy { it.id })
        }.distinctBy { it.id }.take(if (p.availableMinutes <= 20) 2 else 4)
        if (chosen.size < 2) return PlanningDecision.ReviewRequired(
            "Hay pocas categorías compatibles; se necesita una revisión manual."
        )
        val plan = PlannedRoutine(
            "Misión personalizada (borrador)", p.equipment, chosen,
            selectedVariants = chosen.associate { it.id to availableChoices.getValue(it.id) }
        )
        return PlanningDecision.Candidate(
            routine = plan,
            warnings = listOf(
                "Revisa la propuesta: el catálogo inicial es de demostración.",
                "No se aumenta la carga física como castigo por misiones incompletas."
            ),
            effortEstimate = estimateEffort(observations, p.equipment, chosen.map { it.id }.toSet())
        )
    }

    fun monarchNarrative(completed: Boolean): String =
        if (completed) "Misión Monarca completada: insignia virtual disponible."
        else "Misión Monarca pendiente: cambia la narrativa, no la carga de entrenamiento."
}
