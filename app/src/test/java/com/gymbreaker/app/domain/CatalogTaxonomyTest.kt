package com.gymbreaker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogTaxonomyTest {
    @Test fun hasExactlyFivePrimaryCategories() {
        assertEquals(5, CatalogCategory.entries.size)
        assertEquals(2000, CatalogTaxonomy.TARGET_DISTINCT_EXERCISES)
        assertEquals(400, CatalogTaxonomy.TARGET_DISTINCT_EXERCISES_PER_CATEGORY)
    }

    @Test fun legacySubcategoriesMapToFiveFamilies() {
        val mapping = mapOf(
            "Hombros" to CatalogCategory.UPPER_BODY,
            "Brazos" to CatalogCategory.UPPER_BODY,
            "Piernas" to CatalogCategory.LOWER_BODY,
            "Estabilidad" to CatalogCategory.CORE,
            "Cardio" to CatalogCategory.CARDIO,
            "Movilidad" to CatalogCategory.MOBILITY
        )
        mapping.forEach { (subcategory, expected) ->
            assertEquals(expected, CatalogTaxonomy.forCurrentSubcategory(subcategory))
        }
        assertNull(CatalogTaxonomy.forCurrentSubcategory("Desconocido"))
    }
}
