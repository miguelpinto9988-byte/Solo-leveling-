package com.gymbreaker.app.ai

import com.gymbreaker.app.domain.TrainingPlace
import java.text.Normalizer
import java.util.Locale

/**
 * Shadow Core AI: NLU local e interpretable, sin Internet ni modelos remotos.
 *
 * Reconoce intenciones mediante similitud léxica ponderada sobre un pequeño
 * conjunto curado de ejemplos y analiza parámetros explícitos. No es un LLM.
 * Las acciones se someten al planificador de restricciones antes de ejecutarse.
 */
enum class ShadowIntent {
    ROUTINE, PROGRESS, MISSIONS, RESTRICTIONS, RECOVERY, EQUIPMENT, GREETING, UNKNOWN
}

data class ParsedTrainingRequest(
    val preferredPlace: TrainingPlace? = null,
    val minutes: Int? = null,
    val focusCategories: Set<String> = emptySet()
)

data class ShadowInterpretation(
    val intent: ShadowIntent,
    val confidence: Double,
    val parameters: ParsedTrainingRequest = ParsedTrainingRequest(),
    val needsHumanReview: Boolean = false
)

object ShadowCoreAI {
    private val allowedCategories = linkedMapOf(
        "Espalda" to listOf("espalda", "dorsales", "remo"),
        "Piernas" to listOf("pierna", "piernas", "cuadriceps", "gluteos", "sentadilla"),
        "Brazos" to listOf("brazo", "brazos", "biceps", "triceps"),
        "Hombros" to listOf("hombro", "hombros", "deltoides"),
        "Cardio" to listOf("cardio", "caminar", "bicicleta", "resistencia aerobica"),
        "Movilidad" to listOf("movilidad", "movilizar", "flexibilidad", "estiramiento"),
        "Estabilidad" to listOf("equilibrio", "estabilidad", "balance")
    )

    private val prototypes = mapOf(
        ShadowIntent.ROUTINE to listOf(
            "quiero una rutina para entrenar", "crea un entrenamiento personalizado",
            "entrenar espalda manana en casa", "genera ejercicios para hoy",
            "haz un plan de piernas", "tengo 30 minutos para entrenar",
            "necesito trabajar brazos con mancuernas", "quiero hacer ejercicio"
        ),
        ShadowIntent.PROGRESS to listOf(
            "como va mi progreso", "cuanta experiencia xp tengo",
            "consulta el historial de sesiones", "que nivel y rango tengo",
            "muestra mis estadisticas", "cuantas veces entrene"
        ),
        ShadowIntent.MISSIONS to listOf(
            "misiones monarca", "que desafios diarios tengo",
            "quiero ver las recompensas del cazador", "que mision toca hoy",
            "como funciona la racha"
        ),
        ShadowIntent.RESTRICTIONS to listOf(
            "quiero excluir un ejercicio", "necesito cambiar mis restricciones",
            "evitar ejercicios de piernas", "no puedo hacer sentadillas",
            "actualiza mis limitaciones", "modificar mis preferencias"
        ),
        ShadowIntent.RECOVERY to listOf(
            "me duele la rodilla", "tengo molestias", "estoy lesionado",
            "necesito descansar", "me siento agotado",
            "me siento fatigado", "tengo dolor al entrenar"
        ),
        ShadowIntent.EQUIPMENT to listOf(
            "como cambio mi equipamiento", "tengo mancuernas",
            "prefiero entrenar en el gimnasio", "como ajusto mi tiempo disponible"
        ),
        ShadowIntent.GREETING to listOf(
            "hola", "buenos dias", "buenas tardes", "quien eres", "ayuda"
        )
    )

    private val stopWords = setOf(
        "el", "la", "los", "las", "un", "una", "de", "del", "en", "para",
        "por", "con", "que", "quiero", "me", "mi", "mis", "puedo", "como",
        "hay", "tengo", "hoy", "yo", "hacer", "sobre", "favor", "al", "es"
    )

    /** Unicode y tildes normalizados, sin servicios externos. */
    fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun stems(text: String): Set<String> = normalize(text)
        .split(' ')
        .asSequence()
        .filter { it.length > 2 && it !in stopWords }
        .map { word ->
            when {
                word.endsWith("ciones") && word.length > 8 -> word.dropLast(6)
                word.endsWith("mente") && word.length > 9 -> word.dropLast(5)
                word.endsWith("es") && word.length > 5 -> word.dropLast(2)
                word.endsWith("s") && word.length > 5 -> word.dropLast(1)
                else -> word
            }
        }
        .toSet()

    private fun weightedSimilarity(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val common = a.intersect(b).size.toDouble()
        return 2.0 * common / (a.size + b.size)
    }

    private fun classify(normalized: String): Pair<ShadowIntent, Double> {
        if (normalized.isBlank()) return ShadowIntent.UNKNOWN to 0.0
        val tokens = stems(normalized)
        val results = prototypes.mapValues { (_, examples) ->
            examples.maxOf { weightedSimilarity(tokens, stems(it)) }
        }
        val ranked = results.entries.sortedByDescending { it.value }
        val best = ranked.firstOrNull() ?: return ShadowIntent.UNKNOWN to 0.0
        val margin = best.value - (ranked.getOrNull(1)?.value ?: 0.0)
        val confidence = (0.75 * best.value + 0.25 * margin).coerceIn(0.0, 1.0)
        return if (best.value < 0.24 || (margin < 0.05 && best.value < 0.51))
            ShadowIntent.UNKNOWN to confidence
        else best.key to confidence
    }

    private fun containsToken(text: String, token: String): Boolean {
        val cleaned = normalize(token)
        return Regex("(^| )" + Regex.escape(cleaned) + "( |$)").containsMatchIn(text)
    }

    private fun parsePlace(text: String): TrainingPlace? = when {
        listOf("gimnasio", "gym", "maquinas").any { containsToken(text, it) } ->
            TrainingPlace.GYM
        listOf("mancuernas", "mancuerna", "pesas").any { containsToken(text, it) } ->
            TrainingPlace.DUMBBELLS
        listOf("casa", "hogar", "sin equipo").any { text.contains(it) } ->
            TrainingPlace.HOME
        else -> null
    }

    private fun parseMinutes(text: String): Int? {
        val explicit = Regex("\\b(\\d{1,3})\\s*(minutos|minuto|mins|min|m)\\b")
            .find(text)?.groupValues?.get(1)?.toIntOrNull()
        if (explicit != null) return explicit
        return when {
            "media hora" in text -> 30
            "una hora" in text -> 60
            "un cuarto de hora" in text -> 15
            else -> null
        }
    }

    private fun parseCategories(text: String): Set<String> =
        allowedCategories.filter { (_, aliases) ->
            aliases.any { containsToken(text, it) }
        }.keys.toSet()

    /**
     * El dolor/molestia/restricción personal prevalece sobre una petición de rutina.
     * En esos casos no debe generarse un plan automáticamente.
     */
    fun interpret(raw: String): ShadowInterpretation {
        val text = normalize(raw.take(500))
        if (text.isBlank()) return ShadowInterpretation(ShadowIntent.UNKNOWN, 0.0)
        val place = parsePlace(text)
        val minutes = parseMinutes(text)
        val categories = parseCategories(text)
        val parameters = ParsedTrainingRequest(place, minutes, categories)

        val riskWords = listOf(
            "dolor", "duele", "molestia", "molestias", "lesion", "lesionado",
            "lesionada", "lastimado", "lastimada", "no puedo", "restringido",
            "agotado", "agotada", "fatiga", "fatigado", "fatigada"
        )
        if (riskWords.any { containsToken(text, it) || it.contains(' ') && text.contains(it) })
            return ShadowInterpretation(
                ShadowIntent.RECOVERY, 1.0, parameters, needsHumanReview = true
            )

        val (intent, confidence) = classify(text)
        val trainingVerbs = listOf(
            "entrenar", "entrenamiento", "rutina", "ejercicios", "plan",
            "trabajar", "prepara", "crea", "genera", "sesion"
        )
        val asksTraining = trainingVerbs.any { containsToken(text, it) }
        val isQuestionAboutSettings = listOf("cambiar", "configurar", "ajustar")
            .any { containsToken(text, it) } && !asksTraining
        val selected = when {
            asksTraining && intent !in setOf(ShadowIntent.RESTRICTIONS, ShadowIntent.PROGRESS) ->
                ShadowIntent.ROUTINE
            isQuestionAboutSettings -> ShadowIntent.EQUIPMENT
            intent == ShadowIntent.UNKNOWN && (categories.isNotEmpty() || minutes != null) ->
                ShadowIntent.ROUTINE
            else -> intent
        }
        return ShadowInterpretation(
            selected, if (selected != intent) maxOf(confidence, 0.65) else confidence,
            parameters
        )
    }

    fun limitationMessage(): String =
        "Soy Shadow Core AI, un motor especializado que funciona sin Internet. " +
        "Entiendo solicitudes de rutinas, progreso, misiones y preferencias; " +
        "no soy un modelo de conversación general ni sustituyo una valoración profesional."
}
