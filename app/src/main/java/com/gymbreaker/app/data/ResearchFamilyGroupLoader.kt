package com.gymbreaker.app.data

import android.content.Context
import com.gymbreaker.app.domain.CatalogCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Vista de grupos propuestos y niveles. NUNCA devuelve ExerciseEntity,
 * porque una similitud léxica no certifica identidad ni seguridad.
 */
data class ProposedVariantRecord(
    val name: String,
    val source: String,
    val sourceId: String,
    val equipment: String,
    val originalDifficulty: String,
    val suggestedLevel: String
)

data class ProposedFamilyGroup(
    val id: String,
    val name: String,
    val category: String,
    val aliases: List<String>,
    val patternHint: String?,
    val variantsByLevel: Map<String, List<ProposedVariantRecord>>
) {
    val sourceCount: Int get() = variantsByLevel.values.sumOf { it.size }
    val isProposedMerge: Boolean get() = aliases.size > 1

    fun levelsSummary(): String = ResearchFamilyGroupLoader.levels
        .joinToString(" · ") { (label, key) ->
            label + ": " + variantsByLevel[key].orEmpty().size
        }
}

object ResearchFamilyGroupLoader {
    val levels = listOf(
        "Inicial" to "inicio",
        "Intermedio" to "intermedio",
        "Avanzado" to "avanzado",
        "Sin determinar" to "sin_determinar"
    )

    private val assetNames = mapOf(
        CatalogCategory.UPPER_BODY to "family_groups_upper.json",
        CatalogCategory.LOWER_BODY to "family_groups_lower.json",
        CatalogCategory.CORE to "family_groups_core.json",
        CatalogCategory.CARDIO to "family_groups_cardio.json",
        CatalogCategory.MOBILITY to "family_groups_mobility.json"
    )

    private fun JSONArray.strings(): List<String> =
        (0 until length()).map { getString(it) }

    suspend fun load(context: Context, category: CatalogCategory): List<ProposedFamilyGroup> =
        withContext(Dispatchers.IO) {
            val content = context.assets.open(assetNames.getValue(category))
                .bufferedReader().use { it.readText() }
            val root = JSONObject(content)
            require(root.getInt("schemaVersion") == 2)
            require(root.getString("kind") == "research_family_proposals")
            require(root.getString("category") == category.label)
            require(root.getInt("verifiedFamilies") == 0)

            val groups = root.getJSONArray("groups")
            val decoded = (0 until groups.length()).map { index ->
                val node = groups.getJSONObject(index)
                require(node.getString("reviewStatus") == "research_only_unverified")
                require(!node.getBoolean("semanticMergeVerified"))
                require(!node.getBoolean("autoPlanningEligible"))
                val buckets = node.getJSONObject("variantsByLevel")
                val byLevel = levels.associate { (_, level) ->
                    val entries = buckets.getJSONArray(level)
                    level to (0 until entries.length()).map { i ->
                        val v = entries.getJSONObject(i)
                        require(!v.getBoolean("difficultyVerified"))
                        require(!v.getBoolean("autoPlanningEligible"))
                        ProposedVariantRecord(
                            name = v.getString("name"),
                            source = v.getString("source"),
                            sourceId = v.getString("sourceId"),
                            equipment = v.getString("equipment"),
                            originalDifficulty = v.getString("sourceDifficulty"),
                            suggestedLevel = level
                        )
                    }
                }
                ProposedFamilyGroup(
                    id = node.getString("id"),
                    name = node.getString("label"),
                    category = category.label,
                    aliases = node.getJSONArray("relatedNames").strings(),
                    patternHint = if (node.isNull("patternHint")) null
                        else node.getString("patternHint"),
                    variantsByLevel = byLevel
                ).also { require(it.sourceCount == node.getInt("totalRecords")) }
            }
            require(decoded.size == root.getInt("proposedGroups"))
            require(decoded.sumOf { it.sourceCount } == root.getInt("sourceRecords"))
            decoded
        }

    fun search(
        groups: List<ProposedFamilyGroup>,
        query: String,
        level: String?
    ): List<ProposedFamilyGroup> {
        val term = query.trim().lowercase()
        return groups.filter { group ->
            (term.isBlank() || group.name.lowercase().contains(term) ||
                group.aliases.any { it.lowercase().contains(term) }) &&
                (level == null || group.variantsByLevel[level].orEmpty().isNotEmpty())
        }
    }

    fun preview(group: ProposedFamilyGroup, level: String?): String {
        val selectedLevels = if (level == null) levels else levels.filter { it.second == level }
        val detail = selectedLevels.joinToString("\n\n") { (label, key) ->
            val entries = group.variantsByLevel[key].orEmpty()
            val lines = entries.take(16).joinToString("\n") { variant ->
                "• " + variant.name + " (" + variant.equipment + ")\n" +
                    "  " + variant.source + " · " + variant.sourceId
            }
            "$label: ${entries.size} registros" +
                (if (entries.isNotEmpty()) "\n" + lines else "") +
                (if (entries.size > 16) "\n… " + (entries.size - 16) +
                    " registros adicionales; afina la consulta." else "")
        }
        return "FAMILIA PROPUESTA: " + group.name +
            "\nCATEGORÍA: " + group.category +
            "\nESTADO: SIN REVISIÓN BIOMECÁNICA" +
            "\nNombres relacionados: " + group.aliases.size +
            "\nVariantes documentales: " + group.sourceCount +
            "\n\n" + detail +
            "\n\nEstos registros se agrupan por nombre o patrón sugerido. " +
            "No están aprobados para rutinas automáticas. Los niveles " +
            "son etiquetas de sus fuentes, no una valoración profesional."
    }
}
