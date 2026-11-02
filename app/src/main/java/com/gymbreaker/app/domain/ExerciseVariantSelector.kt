package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity

/**
 * Selección determinista y offline de una sola realización por familia.
 *
 * La dificultad escogida es un techo, nunca una obligación de progresar.
 * Las variantes requieren aprobación individual; entradas de investigación
 * o sin nivel verificado no son seleccionables por el motor.
 */
data class SelectedExerciseVariant(
    val familyId: Int,
    val variantId: String?,
    val displayName: String,
    val level: String,
    val equipment: String
)

object ExerciseVariantSelector {
    fun allowedEquipment(place: TrainingPlace): Set<String> = when (place) {
        TrainingPlace.HOME -> setOf("none")
        TrainingPlace.DUMBBELLS -> setOf("none", "dumbbell")
        TrainingPlace.GYM -> setOf("none", "dumbbell", "gym")
    }

    private fun maxAutoLevel(experience: Experience): Int = when (experience) {
        Experience.BEGINNER -> 1
        Experience.INTERMEDIATE, Experience.EXPERIENCED -> 2
    }

    private fun levelOrder(level: String): Int = when (level) {
        "inicio" -> 1
        "intermedio" -> 2
        // Nunca se propone automáticamente un nivel avanzado.
        else -> 99
    }

    fun choose(
        family: ExerciseEntity,
        variants: List<ExerciseVariantEntity>,
        place: TrainingPlace,
        experience: Experience
    ): SelectedExerciseVariant? {
        if (!family.isDemoEligible) return null
        val equipment = allowedEquipment(place)
        val maximum = maxAutoLevel(experience)

        val choices = mutableListOf<SelectedExerciseVariant>()
        if (family.equipment in equipment && levelOrder(family.level) <= maximum) {
            choices += SelectedExerciseVariant(
                familyId = family.id, variantId = null,
                displayName = family.name,
                level = family.level, equipment = family.equipment
            )
        }
        for (variant in variants) {
            if (variant.exerciseId != family.id) continue
            if (variant.planningEligibility != "approved") continue
            if (variant.restrictionTagsCsv.isNotBlank()) continue
            if (variant.equipment !in equipment) continue
            if (levelOrder(variant.difficulty) > maximum) continue
            choices += SelectedExerciseVariant(
                familyId = family.id,
                variantId = variant.variantId,
                displayName = variant.name,
                level = variant.difficulty,
                equipment = variant.equipment
            )
        }
        // Preferir la dificultad elegida cuando exista una opción verificada.
        // El desempate es estable, no dependiente de la nube.
        return choices.sortedWith(
            compareByDescending<SelectedExerciseVariant> { levelOrder(it.level) }
                .thenBy { it.variantId ?: "" }
        ).firstOrNull()
    }
}
