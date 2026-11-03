package com.gymbreaker.app.ai

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.domain.Experience
import com.gymbreaker.app.domain.OfflineTrainingIntelligence
import com.gymbreaker.app.domain.PlanningDecision
import com.gymbreaker.app.domain.TrainingPlace
import com.gymbreaker.app.domain.TrainingPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowCoreAITest {
    @Test fun understandsRoutineWithTimePlaceAndFocus() {
        val reply = ShadowCoreAI.interpret(
            "Quiero entrenar espalda en casa durante 30 minutos."
        )
        assertEquals(ShadowIntent.ROUTINE, reply.intent)
        assertEquals(TrainingPlace.HOME, reply.parameters.preferredPlace)
        assertEquals(30, reply.parameters.minutes)
        assertTrue("Espalda" in reply.parameters.focusCategories)
    }

    @Test fun handlesAccentsAndPluralWords() {
        val reply = ShadowCoreAI.interpret("¿Cuántas sesiones he entrenado y cómo va mi progreso?")
        assertEquals(ShadowIntent.PROGRESS, reply.intent)
        assertEquals("misiones", ShadowCoreAI.normalize("Misiónes").replace(" ", ""))
    }

    @Test fun painTakesPriorityOverTrainingRequest() {
        val reply = ShadowCoreAI.interpret(
            "Me duele la rodilla, quiero rutina de piernas en el gimnasio"
        )
        assertEquals(ShadowIntent.RECOVERY, reply.intent)
        assertTrue(reply.needsHumanReview)
    }

    @Test fun unknownTextDoesNotPretendToUnderstand() {
        val reply = ShadowCoreAI.interpret("galaxias y viajes interplanetarios")
        assertEquals(ShadowIntent.UNKNOWN, reply.intent)
    }

    @Test fun shortRequestCanSelectDurationAndEquipment() {
        val reply = ShadowCoreAI.interpret("Una sesión de brazos con mancuernas de 25 min")
        assertEquals(ShadowIntent.ROUTINE, reply.intent)
        assertEquals(TrainingPlace.DUMBBELLS, reply.parameters.preferredPlace)
        assertEquals(25, reply.parameters.minutes)
        assertTrue("Brazos" in reply.parameters.focusCategories)
    }

    @Test fun injuryRequestCannotTriggerTrainingEvenIfSpecific() {
        val reply = ShadowCoreAI.interpret("No puedo hacer sentadillas, rutina de 20 minutos")
        assertEquals(ShadowIntent.RECOVERY, reply.intent)
    }

    @Test fun requestedCategoryDoesNotBypassRestrictions() {
        val input = listOf(
            ExerciseEntity(1, "Movilidad", "Movilidad", "none", "inicio"),
            ExerciseEntity(2, "Caminata", "Cardio", "none", "inicio"),
            ExerciseEntity(3, "Espalda con mancuerna", "Espalda", "dumbbell", "intermedio")
        )
        val result = OfflineTrainingIntelligence.propose(
            input,
            TrainingPreferences(
                equipment = TrainingPlace.HOME,
                experience = Experience.BEGINNER,
                requestedCategories = setOf("Espalda")
            )
        )
        assertTrue(result is PlanningDecision.ReviewRequired)
    }

    @Test fun preferenceFocusIsAppliedOnlyToCompatibleExercises() {
        val input = listOf(
            ExerciseEntity(1, "Movilidad", "Movilidad", "none", "inicio"),
            ExerciseEntity(2, "Caminar", "Cardio", "none", "inicio"),
            ExerciseEntity(3, "Remo", "Espalda", "dumbbell", "intermedio"),
            ExerciseEntity(4, "Curl", "Brazos", "dumbbell", "intermedio")
        )
        val result = OfflineTrainingIntelligence.propose(
            input,
            TrainingPreferences(
                equipment = TrainingPlace.DUMBBELLS,
                experience = Experience.INTERMEDIATE,
                requestedCategories = setOf("Espalda"),
                excludedExerciseIds = setOf(4),
                // La experiencia intermedia no sustituye el nivel solicitado.
                preferredDifficulty = "intermedio"
            )
        )
        assertTrue(result is PlanningDecision.Candidate)
        val chosen = (result as PlanningDecision.Candidate).routine.exercises
        assertEquals("Espalda", chosen.first().category)
        assertFalse(chosen.any { it.id == 4 })
    }

    @Test fun parsesDifferentMinuteExpressions() {
        assertEquals(30, ShadowCoreAI.interpret("Crea una rutina de media hora").parameters.minutes)
        assertEquals(60, ShadowCoreAI.interpret("Crea una rutina de una hora").parameters.minutes)
    }
    @Test fun familyQueriesRemainCatalogQueriesNotWorkouts() {
        val query = ShadowCoreAI.interpret("Muéstrame las familias de ejercicios")
        assertEquals(ShadowIntent.CATALOG, query.intent)
    }

    @Test fun explainsRuleChoicesOnNaturalLanguageRequest() {
        val query = ShadowCoreAI.interpret("Explica por qué elegiste esta variante")
        assertEquals(ShadowIntent.EXPLANATION, query.intent)
    }

    @Test fun offlineStorageQuestionsAreNotTreatedAsWorkoutCommands() {
        val query = ShadowCoreAI.interpret("Cuánta memoria usa la inteligencia artificial")
        assertEquals(ShadowIntent.SYSTEM, query.intent)
    }

    @Test fun focusNeverOverridesSelectedInitialDifficulty() {
        val input = listOf(
            ExerciseEntity(1, "Movilidad", "Movilidad", "none", "inicio"),
            ExerciseEntity(2, "Caminar", "Cardio", "none", "inicio"),
            ExerciseEntity(3, "Remo", "Espalda", "dumbbell", "intermedio")
        )
        val result = OfflineTrainingIntelligence.propose(
            input,
            TrainingPreferences(
                equipment = TrainingPlace.DUMBBELLS,
                experience = Experience.INTERMEDIATE,
                requestedCategories = setOf("Espalda"),
                preferredDifficulty = "inicio"
            )
        )
        assertTrue("No se debe sugerir espalda intermedia con nivel inicial",
            result is PlanningDecision.ReviewRequired)
    }

}
