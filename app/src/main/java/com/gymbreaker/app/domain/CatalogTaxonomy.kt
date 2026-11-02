package com.gymbreaker.app.domain

/**
 * Cinco familias principales del catálogo. Las subcategorías anatómicas
 * (pecho, espalda, etc.) siguen existiendo para filtros, pero no se cuentan
 * como familias adicionales ni como variaciones de un mismo ejercicio.
 */
enum class CatalogCategory(val label: String) {
    UPPER_BODY("Tren superior"),
    LOWER_BODY("Tren inferior"),
    CORE("Zona central"),
    CARDIO("Cardio"),
    MOBILITY("Movilidad")
}

object CatalogTaxonomy {
    fun forCurrentSubcategory(subcategory: String): CatalogCategory? =
        when (subcategory.trim().lowercase()) {
            "pecho", "espalda", "brazos", "hombros", "bíceps", "biceps",
            "tríceps", "triceps", "antebrazos" -> CatalogCategory.UPPER_BODY
            "piernas", "glúteos", "gluteos", "cuádriceps", "cuadriceps",
            "isquiotibiales", "pantorrillas" -> CatalogCategory.LOWER_BODY
            "abdomen", "abdominales", "core", "zona central", "estabilidad" ->
                CatalogCategory.CORE
            "cardio", "cardiovascular" -> CatalogCategory.CARDIO
            "movilidad", "flexibilidad", "estiramientos" -> CatalogCategory.MOBILITY
            else -> null
        }

    const val TARGET_DISTINCT_EXERCISES_PER_CATEGORY = 400
    const val TARGET_DISTINCT_EXERCISES = 2000
}
