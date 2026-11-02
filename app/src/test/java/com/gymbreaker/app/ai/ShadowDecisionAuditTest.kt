package com.gymbreaker.app.ai

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity
import com.gymbreaker.app.domain.Experience
import com.gymbreaker.app.domain.OfflineTrainingIntelligence
import com.gymbreaker.app.domain.PlanningDecision
import com.gymbreaker.app.domain.TrainingPlace
import com.gymbreaker.app.domain.TrainingPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowDecisionAuditTest {
    private val exercises = listOf(
        ExerciseEntity(1, "Ficha A", "Movilidad", "none", "inicio"),
        ExerciseEntity(2, "Ficha B", "Cardio", "none", "inicio"),
        ExerciseEntity(3, "Ficha C", "Piernas", "gym", "intermedio")
    )

    @Test fun auditExplainsExclusionsAndNeverInflatesFamilyCount() {
        val p = TrainingPreferences(
            equipment = TrainingPlace.HOME,
            experience = Experience.BEGINNER,
            excludedCategories = setOf("Cardio"),
            preferredDifficulty = "inicio"
        )
        val decision = OfflineTrainingIntelligence.propose(exercises, p)
        val report = ShadowDecisionAudit.evaluate(exercises, emptyList(), p, emptyList(), decision)
        assertEquals("BLOQUEADA PARA REVISIÓN", report.status)
        assertTrue(report.readable().contains("Familias excluidas por categoría o ID: 1"))
        assertTrue(report.readable().contains("Dificultad deseada: inicio"))
        assertEquals(6, report.stages.size)
    }

    @Test fun approvedVariantsAreCountedSeparatelyFromFamilies() {
        val variants = listOf(
            ExerciseVariantEntity(1, "safe-a", "Variante A", "none", "source:1",
                "inicio", "approved", ""),
            ExerciseVariantEntity(1, "pending-a", "Variante B", "none", "source:2",
                "sin_determinar", "catalog_only", "")
        )
        val p = TrainingPreferences(
            equipment = TrainingPlace.HOME,
            preferredDifficulty = "inicio"
        )
        val decision = OfflineTrainingIntelligence.propose(exercises, p, variants = variants)
        assertTrue(decision is PlanningDecision.Candidate)
        val report = ShadowDecisionAudit.evaluate(exercises, variants, p, emptyList(), decision)
        assertEquals(2, report.selectedFamilies.size)
        assertTrue(report.readable().contains("Variantes con autorización y nivel inicial/intermedio: 1"))
        assertTrue(report.readable().contains("pendientes, avanzadas o no compatibles"))
    }

    @Test fun unapprovedResearchDoesNotBecomeRoutinelySelectable() {
        val onlyResearch = listOf(
            ExerciseEntity(10001, "Pendiente", "Movilidad", "none", "inicio",
                isDemoEligible = false)
        )
        val p = TrainingPreferences(equipment = TrainingPlace.HOME)
        val decision = OfflineTrainingIntelligence.propose(onlyResearch, p)
        val report = ShadowDecisionAudit.evaluate(
            onlyResearch, emptyList(), p, emptyList(), decision
        )
        assertTrue(decision is PlanningDecision.ReviewRequired)
        assertTrue(report.selectedFamilies.isEmpty())
        assertFalse(report.readable().contains("Pendiente → Pendiente"))
    }
}
