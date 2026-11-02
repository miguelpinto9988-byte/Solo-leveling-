package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity
import org.junit.Assert.*
import org.junit.Test

class OfflineTrainingIntelligenceTest {
    private val data = listOf(
        ExerciseEntity(1, "Movilidad suave", "Movilidad", "none", "inicio"),
        ExerciseEntity(2, "Caminata", "Cardio", "none", "inicio"),
        ExerciseEntity(3, "Equilibrio asistido", "Estabilidad", "none", "inicio"),
        ExerciseEntity(4, "Remo", "Espalda", "dumbbell", "intermedio"),
        ExerciseEntity(5, "Máquina", "Piernas", "gym", "intermedio")
    )

    @Test fun doesNotSelectExcludedCategoryOrEquipment() {
        val result = OfflineTrainingIntelligence.propose(
            data, TrainingPreferences(
                equipment = TrainingPlace.HOME,
                excludedCategories = setOf("Cardio")
            )
        )
        assertTrue(result is PlanningDecision.Candidate)
        val ids = (result as PlanningDecision.Candidate).routine.exercises.map { it.id }
        assertFalse(2 in ids)
        assertFalse(4 in ids)
        assertFalse(5 in ids)
    }

    @Test fun blocklistCannotBeOverridden() {
        val result = OfflineTrainingIntelligence.propose(
            data, TrainingPreferences(
                equipment = TrainingPlace.HOME,
                excludedExerciseIds = setOf(1)
            )
        ) as PlanningDecision.Candidate
        assertFalse(result.routine.exercises.any { it.id == 1 })
    }

    @Test fun pendingRestrictionBlocksAutomaticPlan() {
        val result = OfflineTrainingIntelligence.propose(
            data, TrainingPreferences(TrainingPlace.HOME, requiresReview = true)
        )
        assertTrue(result is PlanningDecision.ReviewRequired)
    }

    @Test fun highEffortDoesNotTriggerMoreExercises() {
        val result = OfflineTrainingIntelligence.propose(
            data, TrainingPreferences(TrainingPlace.HOME),
            listOf(
                TrainingObservation(TrainingPlace.HOME, setOf(1, 2), 9, true),
                TrainingObservation(TrainingPlace.HOME, setOf(1, 2), 8, true)
            )
        )
        assertTrue(result is PlanningDecision.ReviewRequired)
    }

    @Test fun tooFewObservationsDoesNotProduceEstimate() {
        val records = List(4) {
            TrainingObservation(TrainingPlace.HOME, setOf(1, 2), 5, true)
        }
        assertNull(OfflineTrainingIntelligence.estimateEffort(
            records, TrainingPlace.HOME, setOf(1, 2)
        ))
    }

    @Test fun fiveComparableObservationsYieldLimitedEstimate() {
        val records = List(5) {
            TrainingObservation(TrainingPlace.HOME, setOf(1, 2), 4 + it % 2, true)
        }
        val estimate = OfflineTrainingIntelligence.estimateEffort(
            records, TrainingPlace.HOME, setOf(1, 2)
        )
        assertNotNull(estimate)
        assertEquals(5, estimate!!.sampleSize)
        assertEquals(4.4, estimate.averagePerceivedEffort, 0.01)
    }

    @Test fun nonMatchingOrUnconfirmedRecordsCannotTrainEstimate() {
        val records = List(6) {
            TrainingObservation(TrainingPlace.GYM, setOf(4, 5), 2, false)
        }
        assertNull(OfflineTrainingIntelligence.estimateEffort(
            records, TrainingPlace.HOME, setOf(1, 2)
        ))
    }

    @Test fun monarchFailureOnlyChangesStory() {
        assertTrue(OfflineTrainingIntelligence.monarchNarrative(false).contains("no la carga"))
    }

    @Test fun recentUsageFavoursAlternativeCompatibleExercise() {
        val exercises = listOf(
            ExerciseEntity(1, "Movilidad A", "Movilidad", "none", "inicio"),
            ExerciseEntity(2, "Movilidad B", "Movilidad", "none", "inicio"),
            ExerciseEntity(3, "Caminata", "Cardio", "none", "inicio")
        )
        val records = List(5) {
            TrainingObservation(
                TrainingPlace.HOME, setOf(1, 3), 4, true
            )
        }
        val result = OfflineTrainingIntelligence.propose(
            exercises, TrainingPreferences(TrainingPlace.HOME), records
        ) as PlanningDecision.Candidate
        assertTrue(result.routine.exercises.any { it.id == 2 })
        assertFalse(result.routine.exercises.any { it.id == 1 })
    }
    @Test fun plannerUsesExactlyOneReviewedVariantPerCanonicalFamily() {
        val base = listOf(
            ExerciseEntity(10001, "Familia A", "Piernas", "none", "inicio"),
            ExerciseEntity(10002, "Familia B", "Movilidad", "none", "inicio")
        )
        val variants = listOf(
            ExerciseVariantEntity(
                10001, "variant-a-intermediate", "Realización intermedia A",
                "none", "test-source:1", "intermedio", "approved", ""
            ),
            ExerciseVariantEntity(
                10001, "variant-a-pending", "Realización sin validar",
                "none", "test-source:2", "intermedio", "catalog_only", ""
            )
        )
        val decision = OfflineTrainingIntelligence.propose(
            exercises = base,
            profile = TrainingPreferences(
                TrainingPlace.HOME, experience = Experience.INTERMEDIATE
            ),
            variants = variants
        ) as PlanningDecision.Candidate
        assertEquals(2, decision.routine.exercises.size)
        assertEquals("variant-a-intermediate",
            decision.routine.selectedVariants[10001]?.variantId)
        assertEquals(null, decision.routine.selectedVariants[10002]?.variantId)
    }

    @Test fun beginnerCannotBeEscalatedByHigherVariant() {
        val base = listOf(
            ExerciseEntity(10001, "Familia A", "Piernas", "none", "inicio"),
            ExerciseEntity(10002, "Familia B", "Movilidad", "none", "inicio")
        )
        val variants = listOf(
            ExerciseVariantEntity(
                10001, "variant-higher", "Realización intermedia A",
                "none", "test-source:1", "intermedio", "approved", ""
            )
        )
        val result = OfflineTrainingIntelligence.propose(
            exercises = base,
            profile = TrainingPreferences(TrainingPlace.HOME, Experience.BEGINNER),
            variants = variants
        ) as PlanningDecision.Candidate
        assertEquals("inicio", result.routine.selectedVariants[10001]?.level)
        assertEquals(null, result.routine.selectedVariants[10001]?.variantId)
    }

}
