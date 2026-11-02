package com.gymbreaker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApprovedCatalogGateTest {
    private fun family(
        id: String = "family-one",
        localId: Int = 10_001,
        status: String = "approved",
        eligibility: String = "catalog_only",
        tags: Set<String> = emptySet(),
        variants: List<ReviewedExerciseVariant> = emptyList(),
        key: String = "pattern-test"
    ): ReviewedExerciseFamily = ReviewedExerciseFamily(
        id = id,
        localId = localId,
        name = "Ficha ficticia de prueba",
        mainCategory = "Movilidad",
        subcategory = "Movilidad",
        patternKey = key,
        equipment = "none",
        level = "inicio",
        reviewStatus = status,
        planningEligibility = eligibility,
        restrictionTags = tags,
        sourceReferences = listOf("test-source:1"),
        variants = variants
    )

    @Test fun oneParentWithVariantsCountsAsOneExercise() {
        val f = family(variants = listOf(
            ReviewedExerciseVariant("variant-a", "Variante A", "none", listOf("test:2")),
            ReviewedExerciseVariant("variant-b", "Variante B", "gym", listOf("test:3"))
        ))
        assertEquals(1, ApprovedCatalogGate.countDistinctPrimaryExercises(listOf(f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun samePatternCannotBeDuplicatedWithDifferentId() {
        ApprovedCatalogGate.accept(listOf(
            family(id = "family-a", localId = 10001, key = "same-pattern"),
            family(id = "family-b", localId = 10002, key = "same-pattern")
        ))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unreviewedSourceCannotBecomeApprovedExercise() {
        ApprovedCatalogGate.accept(listOf(family(status = "research_only_pending_identity_review")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun restrictionsCannotBeIgnoredForAutomaticPlanning() {
        ApprovedCatalogGate.accept(listOf(
            family(eligibility = "approved", tags = setOf("manual-review-required"))
        ))
    }

    @Test fun approvedWithoutRestrictionsMayBeEligible() {
        assertTrue(ApprovedCatalogGate.eligibleForPlanning(family(eligibility = "approved")))
        assertFalse(ApprovedCatalogGate.eligibleForPlanning(family()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateVariantIdsAreRejected() {
        val v = ReviewedExerciseVariant("variant-a", "A", "none", listOf("test:3"))
        ApprovedCatalogGate.accept(listOf(family(variants = listOf(v, v))))
    }

    @Test fun emptyApprovedManifestDoesNotCountAnyExercise() {
        assertEquals(0, ApprovedCatalogGate.countDistinctPrimaryExercises(emptyList()))
    }
}
