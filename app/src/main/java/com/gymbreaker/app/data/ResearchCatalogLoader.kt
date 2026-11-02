package com.gymbreaker.app.data

import android.content.Context
import com.gymbreaker.app.domain.CatalogCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Catálogo documental empaquetado offline: SOLO CONSULTA.
 * Estos registros no son movimientos revisados y jamás se convierten en
 * ExerciseEntity ni se envían al planificador de Shadow Core AI.
 */
data class ResearchSourceEntry(
    val name: String,
    val source: String,
    val sourceId: String,
    val sourceDifficulty: String,
    val suggestedDifficulty: String,
    val equipment: String
)

data class ResearchExerciseCandidate(
    val name: String,
    val category: String,
    val relatedFamilyHint: String?,
    val records: List<ResearchSourceEntry>
)

object ResearchCatalogLoader {
    private val files = mapOf(
        CatalogCategory.UPPER_BODY to "research_upper.json",
        CatalogCategory.LOWER_BODY to "research_lower.json",
        CatalogCategory.CORE to "research_core.json",
        CatalogCategory.CARDIO to "research_cardio.json",
        CatalogCategory.MOBILITY to "research_mobility.json"
    )

    private val knownLevels = setOf("inicio", "intermedio", "avanzado", "sin_determinar")

    suspend fun load(context: Context, category: CatalogCategory): List<ResearchExerciseCandidate> =
        withContext(Dispatchers.IO) {
            val fileName = files.getValue(category)
            val contents = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val doc = JSONObject(contents)
            require(doc.getInt("schemaVersion") == 1)
            require(doc.getString("kind") == "research_only")
            require(doc.getString("category") == category.label)
            require(doc.getInt("verifiedFamilies") == 0)
            val families = doc.getJSONArray("families")
            (0 until families.length()).map { index ->
                val family = families.getJSONObject(index)
                require(family.getString("reviewStatus") == "research_only")
                val records = family.getJSONArray("records")
                val candidates = (0 until records.length()).map { recordIndex ->
                    val row = records.getJSONObject(recordIndex)
                    require(!row.getBoolean("autoPlanningEligible"))
                    val difficulty = row.getString("suggestedDifficulty")
                    require(difficulty in knownLevels)
                    require(!row.getBoolean("difficultyVerified"))
                    ResearchSourceEntry(
                        name = row.getString("name"),
                        source = row.getString("source"),
                        sourceId = row.getString("sourceId"),
                        sourceDifficulty = row.getString("sourceDifficulty"),
                        suggestedDifficulty = difficulty,
                        equipment = row.getString("equipment")
                    )
                }
                require(candidates.isNotEmpty())
                ResearchExerciseCandidate(
                    name = family.getString("name"),
                    category = category.label,
                    relatedFamilyHint = if (family.isNull("relatedFamilyHint")) null
                        else family.getString("relatedFamilyHint").ifBlank { null },
                    records = candidates
                )
            }
        }

    fun search(
        families: List<ResearchExerciseCandidate>,
        search: String,
        difficulty: String?
    ): List<ResearchExerciseCandidate> {
        val query = search.trim().lowercase()
        return families.filter { family ->
            (query.isEmpty() ||
                family.name.lowercase().contains(query) ||
                family.relatedFamilyHint.orEmpty().lowercase().contains(query)) &&
                (difficulty == null || family.records.any {
                    it.suggestedDifficulty == difficulty
                })
        }
    }

    fun preview(family: ResearchExerciseCandidate, all: List<ResearchExerciseCandidate>): String {
        val variants = family.records.joinToString("\n") { entry ->
            val level = when (entry.suggestedDifficulty) {
                "inicio" -> "Inicial (informado por fuente)"
                "intermedio" -> "Intermedio (informado por fuente)"
                "avanzado" -> "Avanzado (informado por fuente)"
                else -> "Sin determinar"
            }
            "• " + entry.name + " · " + level + "\n" +
                "  " + entry.equipment + " · " + entry.source
        }
        val related = family.relatedFamilyHint?.let { hint ->
            all.filter { it !== family && it.relatedFamilyHint == hint }
                .take(12).map { it.name }
        }.orEmpty()
        val suggestions = if (related.isEmpty()) "" else
            "\n\nPOSIBLES VARIANTES RELACIONADAS (SIN VERIFICAR):\n" +
                related.joinToString("\n") { "• " + it }
        return "CATEGORÍA: " + family.category +
            "\nESTADO: INVESTIGACIÓN — NO APTO PARA RUTINAS\n\n" +
            "ENTRADAS DE ORIGEN:\n" + variants + suggestions +
            "\n\nLos nombres relacionados no prueban identidad biomecánica. " +
            "No se importarán automáticamente a Shadow Core AI."
    }
}
