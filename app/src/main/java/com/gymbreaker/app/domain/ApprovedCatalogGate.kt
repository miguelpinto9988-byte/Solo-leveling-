package com.gymbreaker.app.domain

/**
 * Contrato explícito para datos de ejercicios revisados. Una familia tiene un
 * único ejercicio principal, y las variantes NO incrementan su contador.
 *
 * Este control no sustituye la revisión técnica: impide que el motor acepte
 * accidentalmente registros de staging o familias sin documentación.
 */
data class ReviewedExerciseVariant(
    val id: String,
    val name: String,
    val equipment: String,
    val sourceReferences: List<String>
)

data class ReviewedExerciseFamily(
    val id: String,
    val localId: Int,
    val name: String,
    val mainCategory: String,
    val subcategory: String,
    val patternKey: String,
    val equipment: String,
    val level: String,
    val reviewStatus: String,
    val planningEligibility: String,
    val restrictionTags: Set<String>,
    val sourceReferences: List<String>,
    val variants: List<ReviewedExerciseVariant>
)

object ApprovedCatalogGate {
    private val mainCategories = CatalogCategory.entries.map { it.label }.toSet()
    private val equipment = setOf("none", "dumbbell", "gym")
    private val level = setOf("inicio", "intermedio", "avanzado")

    fun accept(families: List<ReviewedExerciseFamily>): List<ReviewedExerciseFamily> {
        require(families.map { it.id }.distinct().size == families.size) {
            "Hay identificadores de familias duplicados"
        }
        require(families.map { it.localId }.distinct().size == families.size) {
            "Hay identificadores locales duplicados"
        }
        require(families.map { it.patternKey.lowercase() }.distinct().size ==
            families.size) { "Un mismo patrón se cuenta como dos ejercicios" }

        for (f in families) {
            require(f.id.isNotBlank() && f.localId >= 10_000)
            require(f.name.isNotBlank() && f.patternKey.isNotBlank() && f.subcategory.isNotBlank())
            require(f.mainCategory in mainCategories)
            require(f.equipment in equipment && f.level in level)
            require(f.reviewStatus == "approved") {
                "No se autoriza una ficha de investigación para la APK"
            }
            require(f.planningEligibility in setOf("approved", "catalog_only"))
            require(f.sourceReferences.isNotEmpty())
            require(f.variants.map { it.id }.distinct().size == f.variants.size)
            for (v in f.variants) {
                require(v.id.isNotBlank() && v.name.isNotBlank() && v.sourceReferences.isNotEmpty())
                require(v.equipment in equipment)
            }
            // Los casos técnicos avanzados o con etiquetas preventivas
            // siguen siendo consultables: no pasan al generador automático.
            if (f.level == "avanzado" || f.restrictionTags.isNotEmpty()) {
                require(f.planningEligibility == "catalog_only")
            }
        }
        return families
    }

    fun eligibleForPlanning(family: ReviewedExerciseFamily): Boolean =
        family.reviewStatus == "approved" &&
            family.planningEligibility == "approved" &&
            family.restrictionTags.isEmpty() &&
            family.level != "avanzado"

    fun countDistinctPrimaryExercises(families: List<ReviewedExerciseFamily>): Int =
        accept(families).size
}
