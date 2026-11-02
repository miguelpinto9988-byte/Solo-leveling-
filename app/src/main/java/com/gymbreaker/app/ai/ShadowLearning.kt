package com.gymbreaker.app.ai

import com.gymbreaker.app.domain.TrainingObservation
import com.gymbreaker.app.domain.TrainingPlace
import kotlin.math.abs

/**
 * Aprendizaje estadístico interpretable y ejecutado al consultar datos.
 *
 * No entrena un LLM, no recopila datos externos ni estima lesiones.
 * Usa únicamente sesiones confirmadas que el usuario registró.
 */
data class ShadowProgressInsight(
    val comparableSamples: Int,
    val recentAverageEffort: Double?,
    val olderAverageEffort: Double?,
    val trend: String,
    val confidenceLabel: String,
    val recommendation: String
)

object ShadowLearning {
    private const val MIN_SAMPLES = 5
    private const val WINDOW = 5

    fun analyze(observationsChronological: List<TrainingObservation>, place: TrainingPlace? = null):
        ShadowProgressInsight {
        val valid = observationsChronological.filter {
            it.confirmedComplete && it.perceivedEffort in 1..10 &&
                it.exerciseIds.isNotEmpty() && (place == null || it.equipment == place)
        }
        if (valid.size < MIN_SAMPLES) {
            return ShadowProgressInsight(
                comparableSamples = valid.size,
                recentAverageEffort = null,
                olderAverageEffort = null,
                trend = "Sin tendencia suficiente",
                confidenceLabel = "Datos insuficientes",
                recommendation = "Registra sesiones voluntariamente; no se ajustará la dificultad " +
                    "a partir de registros escasos."
            )
        }

        val recent = valid.takeLast(WINDOW)
        val prior = valid.dropLast(WINDOW).takeLast(WINDOW)
        val recentMean = recent.map { it.perceivedEffort }.average()
        val earlierMean = if (prior.size >= 3) prior.map { it.perceivedEffort }.average() else null
        val discomfort = valid.takeLast(3).any { it.reportedDiscomfort }
        val highEffort = recent.count { it.perceivedEffort >= 8 } >= 2

        val trend = when {
            earlierMean == null -> "Referencia inicial"
            abs(recentMean - earlierMean) < 0.8 -> "Esfuerzo declarado estable"
            recentMean > earlierMean -> "Esfuerzo declarado en aumento"
            else -> "Esfuerzo declarado en descenso"
        }
        val recommendation = when {
            discomfort ->
                "Se declararon molestias recientes. Revisa restricciones y evita generar " +
                    "progresiones automáticas hasta confirmar compatibilidad."
            highEffort ->
                "Se registró esfuerzo elevado recientemente. Considera descanso o una propuesta " +
                    "más ligera, sin penalizaciones."
            earlierMean == null ->
                "Ya hay una referencia inicial. Mantén las propuestas editables y reúne más datos " +
                    "antes de considerar ajustes."
            recentMean > earlierMean + 0.8 ->
                "El esfuerzo registrado aumentó. Conviene revisar la dificultad y mantener " +
                    "opciones conservadoras."
            else ->
                "Puedes explorar variaciones del catálogo compatible si lo deseas. " +
                    "No se incrementará automáticamente la carga física."
        }
        return ShadowProgressInsight(
            comparableSamples = valid.size,
            recentAverageEffort = recentMean,
            olderAverageEffort = earlierMean,
            trend = trend,
            confidenceLabel = if (earlierMean == null) "Referencia preliminar"
                else "Tendencia descriptiva",
            recommendation = recommendation
        )
    }
}
