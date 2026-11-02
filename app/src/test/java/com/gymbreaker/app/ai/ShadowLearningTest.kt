package com.gymbreaker.app.ai

import com.gymbreaker.app.domain.TrainingObservation
import com.gymbreaker.app.domain.TrainingPlace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowLearningTest {
    private fun record(
        effort: Int,
        discomfort: Boolean = false,
        place: TrainingPlace = TrainingPlace.HOME,
        confirmed: Boolean = true
    ) = TrainingObservation(
        equipment = place,
        exerciseIds = setOf(1, 2),
        perceivedEffort = effort,
        confirmedComplete = confirmed,
        reportedDiscomfort = discomfort
    )

    @Test fun refusesToInferTrendWithTooFewSamples() {
        val result = ShadowLearning.analyze(List(4) { record(5) })
        assertEquals(4, result.comparableSamples)
        assertEquals("Datos insuficientes", result.confidenceLabel)
        assertNull(result.recentAverageEffort)
    }

    @Test fun excludesUnconfirmedAndOtherEquipment() {
        val input = List(4) { record(5) } +
            List(5) { record(3, place = TrainingPlace.GYM) } +
            List(6) { record(8, confirmed = false) }
        val result = ShadowLearning.analyze(input, place = TrainingPlace.HOME)
        assertEquals(4, result.comparableSamples)
    }

    @Test fun highEffortNeverProducesPunitiveEscalation() {
        val result = ShadowLearning.analyze(
            listOf(record(4), record(4), record(4), record(8), record(9))
        )
        assertTrue("descanso" in result.recommendation.lowercase())
        assertTrue("penalizaciones" in result.recommendation.lowercase())
    }

    @Test fun detectsDescriptiveTrendWithoutClaimingMedicalOutcome() {
        val result = ShadowLearning.analyze(
            List(5) { record(3) } + List(5) { record(6) }
        )
        assertEquals("Esfuerzo declarado en aumento", result.trend)
        assertEquals(10, result.comparableSamples)
        assertEquals("Tendencia descriptiva", result.confidenceLabel)
    }

    @Test fun discomfortPrioritizesManualReview() {
        val result = ShadowLearning.analyze(
            List(5) { record(3) } + listOf(record(5, discomfort = true))
        )
        assertTrue(result.recommendation.contains("restricciones"))
    }
}
