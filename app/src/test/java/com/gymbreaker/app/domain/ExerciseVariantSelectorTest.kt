package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseVariantSelectorTest {
    private val exercise = ExerciseEntity(
        10001, "Movimiento principal ficticio", "Piernas", "none", "inicio",
        isDemoEligible = true
    )

    private fun variant(
        id: String,
        level: String,
        equipment: String = "none",
        eligibility: String = "approved",
        restrictions: String = ""
    ) = ExerciseVariantEntity(
        exerciseId = exercise.id,
        variantId = id,
        name = "Forma ficticia " + id,
        equipment = equipment,
        sourceRefsCsv = "test:entry",
        difficulty = level,
        planningEligibility = eligibility,
        restrictionTagsCsv = restrictions
    )

    @Test fun beginnerNeverReceivesIntermediateOrAdvancedVariant() {
        val selection = ExerciseVariantSelector.choose(
            exercise,
            listOf(variant("variant-intermediate", "intermedio"),
                variant("variant-advanced", "avanzado")),
            TrainingPlace.HOME,
            Experience.BEGINNER
        )
        assertEquals("inicio", selection?.level)
        assertNull(selection?.variantId)
    }

    @Test fun intermediateMaySelectReviewedIntermediateVariant() {
        val selection = ExerciseVariantSelector.choose(
            exercise,
            listOf(variant("variant-intermediate", "intermedio")),
            TrainingPlace.HOME,
            Experience.INTERMEDIATE,
            preferredDifficulty = "intermedio"
        )
        assertEquals("variant-intermediate", selection?.variantId)
        assertEquals("intermedio", selection?.level)
    }

    @Test fun advancedVariantNeverSelectedAutomatically() {
        val selection = ExerciseVariantSelector.choose(
            exercise,
            listOf(variant("variant-advanced", "avanzado")),
            TrainingPlace.HOME,
            Experience.EXPERIENCED
        )
        assertNull(selection?.variantId)
        assertEquals("inicio", selection?.level)
    }

    @Test fun unevaluatedAndRestrictedVariantsNeverSelected() {
        val selected = ExerciseVariantSelector.choose(
            exercise,
            listOf(
                variant("variant-unknown", "sin_determinar"),
                variant("variant-restricted", "intermedio", restrictions = "manual"),
                variant("variant-pending", "intermedio", eligibility = "catalog_only")
            ),
            TrainingPlace.HOME,
            Experience.INTERMEDIATE
        )
        assertNull(selected?.variantId)
    }

    @Test fun equipmentMismatchRejectsVariation() {
        val selection = ExerciseVariantSelector.choose(
            exercise,
            listOf(variant("variant-gym", "intermedio", equipment = "gym")),
            TrainingPlace.HOME,
            Experience.INTERMEDIATE
        )
        assertNull(selection?.variantId)
    }

    @Test fun recentHistoryRotatesEqualDifficultyChoices() {
        val options = listOf(
            variant("variant-alpha", "intermedio"),
            variant("variant-beta", "intermedio")
        )
        val selection = ExerciseVariantSelector.choose(
            exercise, options, TrainingPlace.HOME, Experience.INTERMEDIATE,
            recentSelections = mapOf("10001:variant-alpha" to 4)
        )
        assertEquals("variant-beta", selection?.variantId)
    }

    @Test fun aCatalogOnlyFamilyCannotBeEnabledByAnApprovedVariant() {
        val family = exercise.copy(isDemoEligible = false)
        assertNull(ExerciseVariantSelector.choose(
            family, listOf(variant("variant-intermediate", "intermedio")),
            TrainingPlace.HOME, Experience.INTERMEDIATE
        ))
    }

    @Test fun noCompatibleExerciseReturnsNull() {
        val family = exercise.copy(equipment = "gym", level = "intermedio")
        assertNull(ExerciseVariantSelector.choose(
            family, emptyList(), TrainingPlace.HOME, Experience.BEGINNER
        ))
    }
    @Test fun requestedInitialDifficultyOverridesIntermediateExperience() {
        val selected = ExerciseVariantSelector.choose(
            exercise,
            listOf(variant("variant-intermediate", "intermedio")),
            TrainingPlace.HOME, Experience.INTERMEDIATE,
            preferredDifficulty = "inicio"
        )
        assertEquals("inicio", selected?.level)
        assertNull(selected?.variantId)
    }

}
