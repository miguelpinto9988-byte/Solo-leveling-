package com.gymbreaker.app.ai

import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.ExerciseVariantEntity
import com.gymbreaker.app.domain.CatalogTaxonomy
import com.gymbreaker.app.domain.ExerciseVariantSelector
import com.gymbreaker.app.domain.PlanningDecision
import com.gymbreaker.app.domain.TrainingObservation
import com.gymbreaker.app.domain.TrainingPreferences

/**
 * Rastro explícito de reglas y filtros de Shadow Core AI.
 *
 * No expone pensamientos internos ni diagnósticos: muestra decisiones
 * deterministas, parámetros y estadísticas auditables por el usuario.
 */
data class ShadowAuditStage(
    val title: String,
    val detail: String
)

data class ShadowAuditReport(
    val status: String,
    val stages: List<ShadowAuditStage>,
    val selectedFamilies: List<String>,
    val disclaimer: String =
        "Explicación técnica de reglas locales, no diagnóstico ni razonamiento " +
        "de un modelo generativo. Ninguna variante de investigación se aprobó aquí."
) {
    fun readable(): String {
        val section = stages.joinToString("\n\n") {
            it.title.uppercase() + "\n" + it.detail
        }
        val selected = if (selectedFamilies.isEmpty()) "Ninguna propuesta activa"
            else selectedFamilies.joinToString("\n") { "• " + it }
        return "SHADOW CORE AI · INFORME LOCAL\n" + status +
            "\n\n" + section + "\n\nRESULTADO\n" + selected +
            "\n\n" + disclaimer
    }
}

object ShadowDecisionAudit {
    fun evaluate(
        exercises: List<ExerciseEntity>,
        variants: List<ExerciseVariantEntity>,
        preferences: TrainingPreferences,
        observations: List<TrainingObservation>,
        decision: PlanningDecision?
    ): ShadowAuditReport {
        val p = preferences.validated()
        val allowedEquipment = ExerciseVariantSelector.allowedEquipment(p.equipment)
        val excludedFamilies = exercises.filter { e ->
            e.id in p.excludedExerciseIds ||
                p.excludedCategories.any { excluded ->
                    excluded.equals(e.category, ignoreCase = true) ||
                        excluded.equals(CatalogTaxonomy
                            .forCurrentSubcategory(e.category)?.label,
                            ignoreCase = true)
                }
        }
        val selected = (decision as? PlanningDecision.Candidate)?.routine
        val selectedDisplay = selected?.exercises.orEmpty().map { e ->
            val realization = selected?.selectedVariants?.get(e.id)
            e.name + " → " + (realization?.displayName ?: e.name) +
                " [" + (realization?.level ?: e.level) + "]"
        }
        val fullyApproved = variants.count { it.planningEligibility == "approved" &&
            it.restrictionTagsCsv.isBlank() && it.difficulty in setOf("inicio", "intermedio") }
        val unknownOrUnapproved = variants.size - fullyApproved
        val reasons = when (decision) {
            is PlanningDecision.ReviewRequired -> decision.message
            is PlanningDecision.Candidate ->
                "Se generó una propuesta revisable con " +
                    decision.routine.exercises.size +
                    " familias independientes. La experiencia y el nivel elegido " +
                    "actúan como límites y no como obligación de aumentar dificultad."
            null -> "Aún no hay una propuesta. Puedes generar una desde Entrenar."
        }
        val stages = listOf(
            ShadowAuditStage(
                "1. Entrada local",
                "Equipamiento solicitado: " + p.equipment +
                    "\nExperiencia declarada: " + p.experience +
                    "\nDificultad deseada: " + p.preferredDifficulty +
                    "\nMinutos disponibles: " + p.availableMinutes
            ),
            ShadowAuditStage(
                "2. Lectura y descodificación",
                "Familias en Room: " + exercises.size +
                    "\nVariantes subordinadas: " + variants.size +
                    "\nSesiones observadas: " + observations.size +
                    "\nNo se consultaron servidores ni registros de investigación " +
                    "para generar la propuesta."
            ),
            ShadowAuditStage(
                "3. Restricciones",
                "Familias excluidas por categoría o ID: " + excludedFamilies.size +
                    "\nEquipamiento disponible: " + allowedEquipment.joinToString(", ") +
                    "\nRevisión pendiente: " + if (p.requiresReview) "Sí" else "No"
            ),
            ShadowAuditStage(
                "4. Variantes y dificultad",
                "Variantes con autorización y nivel inicial/intermedio: " +
                    fullyApproved + "\nVariantes pendientes, avanzadas o no compatibles " +
                    "con selección automática: " + unknownOrUnapproved +
                    "\nUna variante avanzada o sin nivel verificable permanece " +
                    "solo para consulta. La selección también revisa equipo y restricciones."
            ),
            ShadowAuditStage(
                "5. Aprendizaje descriptivo",
                "Sesiones confirmadas: " +
                    observations.count { it.confirmedComplete } +
                    "\nLa rotación de variantes utiliza como máximo los diez registros " +
                    "recientes confirmados y sin molestias, solo entre opciones " +
                    "autorizadas del mismo nivel. No aumenta cargas automáticamente."
            ),
            ShadowAuditStage("6. Decisión y explicación", reasons)
        )
        val status = when (decision) {
            null -> "SIN PROPUESTA"
            is PlanningDecision.ReviewRequired -> "BLOQUEADA PARA REVISIÓN"
            is PlanningDecision.Candidate -> "PROPUESTA PARA CONFIRMAR"
        }
        return ShadowAuditReport(status, stages, selectedDisplay)
    }
}
