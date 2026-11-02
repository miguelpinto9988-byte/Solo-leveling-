package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutinePlannerTest {
    private val samples = listOf(
        ExerciseEntity(1, "A", "Movilidad", "none", "inicio"),
        ExerciseEntity(2, "B", "Piernas", "none", "inicio"),
        ExerciseEntity(3, "C", "Cardio", "none", "inicio"),
        ExerciseEntity(4, "D", "Espalda", "gym", "inicio")
    )

    @Test fun homeNeverSelectsGymOnly() {
        val result = RoutinePlanner.generate(samples, TrainingPlace.HOME)
        assertTrue(result.exercises.isNotEmpty())
        assertTrue(result.exercises.all { it.equipment == "none" })
    }

    @Test fun gymMayIncludeGymOnly() {
        val result = RoutinePlanner.generate(samples, TrainingPlace.GYM)
        assertTrue(result.exercises.any { it.equipment == "gym" })
    }

    @Test fun ranksAreSymbolicAndDeterministic() {
        assertEquals("E", HunterProgression.rank(0))
        assertEquals("S", HunterProgression.rank(4000))
        assertEquals(1, HunterProgression.level(0))
    }

    @Test fun assistantDoesNotPromiseGenerativeAI() {
        assertTrue(LocalAssistant.answer("Hola", 0, 0).contains("aún no"))
    }
}
