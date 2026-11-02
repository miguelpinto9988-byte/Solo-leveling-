package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity

/** Motor offline de decisiones revisables. No diagnostica ni reemplaza a un profesional. */
enum class Experience { BEGINNER, INTERMEDIATE, EXPERIENCED }

data class TrainingPreferences(
    val equipment: TrainingPlace,
    val experience: Experience = Experience.BEGINNER,
    val availableMinutes: Int = 25,
    val excludedCategories: Set<String> = emptySet(),
    val excludedExerciseIds: Set<Int> = emptySet(),
    val requiresReview: Boolean = false,
    val requestedCategories: Set<String> = emptySet()
) {
    fun validated(): TrainingPreferences = copy(availableMinutes = availableMinutes.coerceIn(10, 90))
}

data class TrainingObservation(
    val equipment: TrainingPlace,
    val exerciseIds: Set<Int>,
    val perceivedEffort: Int,
    val confirmedComplete: Boolean,
    val reportedDiscomfort: Boolean = false
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
        observations: List<TrainingObservation> = emptyList()
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
        val allowed = when (p.equipment) {
            TrainingPlace.HOME -> setOf("none")
            TrainingPlace.DUMBBELLS -> setOf("none", "dumbbell")
            TrainingPlace.GYM -> setOf("none", "dumbbell", "gym")
        }
        val eligible = exercises.asSequence()
            .filter { it.isDemoEligible }
            .filter { it.equipment in allowed }
            .filter { it.id !in p.excludedExerciseIds }
            .filter { candidate ->
                p.excludedCategories.none { it.equals(candidate.category, ignoreCase = true) }
            }
            .filter { p.experience != Experience.BEGINNER || it.level == "inicio" }
            .distinctBy { it.id }.toList()
        if (eligible.size < 2) return PlanningDecision.ReviewRequired(
            "No hay suficientes ejercicios compatibles y revisados. " +
            "No se omitirán restricciones declaradas."
        )
        val categories = listOf(
            "Movilidad", "Cardio", "Estabilidad", "Piernas", "Espalda", "Brazos", "Hombros"
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
        val orderedCategories = (requested + categories).distinct()
        val chosen = orderedCategories.mapNotNull { cat ->
            eligible.firstOrNull { it.category.equals(cat, ignoreCase = true) }
        }.distinctBy { it.id }.take(if (p.availableMinutes <= 20) 2 else 4)
        if (chosen.size < 2) return PlanningDecision.ReviewRequired(
            "Hay pocas categorías compatibles; se necesita una revisión manual."
        )
        val plan = PlannedRoutine("Misión personalizada (borrador)", p.equipment, chosen)
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
