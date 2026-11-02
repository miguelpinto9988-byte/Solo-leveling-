package com.gymbreaker.app.domain

import com.gymbreaker.app.data.ExerciseEntity

enum class TrainingPlace { HOME, DUMBBELLS, GYM }

data class PlannedRoutine(
    val title: String,
    val place: TrainingPlace,
    val exercises: List<ExerciseEntity>,
    // ID principal -> variante concreta elegida (o ejercicio base).
    // El conteo de ejercicios se hace con exercises, nunca con selectedVariants.
    val selectedVariants: Map<Int, SelectedExerciseVariant> = emptyMap()
)

object RoutinePlanner {
    fun generate(exercises: List<ExerciseEntity>, place: TrainingPlace): PlannedRoutine {
        val allowedEquipment = when (place) {
            TrainingPlace.HOME -> setOf("none")
            TrainingPlace.DUMBBELLS -> setOf("none", "dumbbell")
            TrainingPlace.GYM -> setOf("none", "dumbbell", "gym")
        }
        val candidates = exercises.filter { it.isDemoEligible && it.equipment in allowedEquipment }
        val balanced = listOf("Movilidad", "Piernas", "Espalda", "Cardio", "Hombros", "Estabilidad")
            .mapNotNull { category -> candidates.firstOrNull { it.category == category } }
            .distinctBy { it.id }
            .take(4)
        val chosen = if (balanced.size >= 3) balanced else candidates.take(4)
        return PlannedRoutine("Misión del Cazador", place, chosen)
    }
}

// La progresión del juego no aumenta automáticamente las cargas físicas.
object HunterProgression {
    fun rank(totalXp: Int): String = when {
        totalXp >= 4000 -> "S"
        totalXp >= 2400 -> "A"
        totalXp >= 1300 -> "B"
        totalXp >= 650 -> "C"
        totalXp >= 250 -> "D"
        else -> "E"
    }

    fun level(totalXp: Int): Int = (totalXp / 200) + 1
}

object LocalAssistant {
    fun answer(question: String, sessions: Int, xp: Int): String {
        val text = question.lowercase()
        return when {
            listOf("progreso", "nivel", "experiencia", "xp").any { it in text } ->
                "Has registrado " + sessions + " sesiones y " + xp +
                    " XP simbólicos. Los rangos del juego no miden tu salud."
            listOf("rutina", "ejercicio", "entren").any { it in text } ->
                "Ve a Entrenar y elige Casa, Mancuernas o Gimnasio para crear una propuesta local editable."
            listOf("monarca", "misión").any { it in text } ->
                "Las misiones Monarca son desafíos opcionales. Descansar no genera castigos ni aumentos de carga."
            listOf("descanso", "dolor", "molest").any { it in text } ->
                "Puedes pausar la sesión o descansar. Si un movimiento provoca dolor, detén esa actividad y busca orientación profesional adecuada."
            else ->
                "Soy el asistente local por reglas (versión inicial). Puedo explicar misiones, rutinas y progreso. La IA generativa offline aún no está integrada."
        }
    }
}
