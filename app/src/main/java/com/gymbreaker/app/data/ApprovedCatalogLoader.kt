package com.gymbreaker.app.data

import android.content.Context
import android.util.Log
import com.gymbreaker.app.domain.ApprovedCatalogGate
import com.gymbreaker.app.domain.ReviewedExerciseFamily
import com.gymbreaker.app.domain.ReviewedExerciseVariant
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lee únicamente el asset generado por el gate de revisión humana.
 * Staging vive fuera de app/src/main y nunca es leído por la aplicación.
 */
object ApprovedCatalogLoader {
    private const val TAG = "ApprovedCatalog"
    private const val ASSET = "catalog_approved.json"

    private fun JSONArray.strings(): List<String> =
        (0 until length()).map { getString(it) }

    fun loadFamilies(context: Context): List<ReviewedExerciseFamily> {
        return try {
            val raw = context.assets.open(ASSET).bufferedReader().use { it.readText() }
            val doc = JSONObject(raw)
            require(doc.getInt("schemaVersion") == 1)
            val records = doc.getJSONArray("canonicalExercises")
            val families = (0 until records.length()).map { index ->
                val f = records.getJSONObject(index)
                val sourceRefs = f.getJSONArray("sources")
                val variants = f.getJSONArray("variants")
                ReviewedExerciseFamily(
                    id = f.getString("id"),
                    localId = f.getInt("localId"),
                    name = f.getString("name"),
                    mainCategory = f.getString("category"),
                    subcategory = f.getString("subcategory"),
                    patternKey = f.getString("patternKey"),
                    equipment = f.getString("equipment"),
                    level = f.getString("level"),
                    reviewStatus = f.getString("reviewStatus"),
                    planningEligibility = f.getString("planningEligibility"),
                    reviewedBy = f.getString("reviewedBy"),
                    reviewDate = f.getString("reviewDate"),
                    identityRationale = f.getString("identityRationale"),
                    riskScreeningNotes = f.getString("riskScreeningNotes"),
                    restrictionTags = f.getJSONArray("restrictionTags").strings().toSet(),
                    sourceReferences = (0 until sourceRefs.length()).map {
                        val ref = sourceRefs.getJSONObject(it)
                        ref.getString("source") + ":" + ref.getString("sourceId")
                    },
                    variants = (0 until variants.length()).map { variantIndex ->
                        val v = variants.getJSONObject(variantIndex)
                        val vr = v.getJSONArray("sources")
                        ReviewedExerciseVariant(
                            id = v.getString("id"),
                            name = v.getString("name"),
                            equipment = v.getString("equipment"),
                            difficulty = v.getString("difficulty"),
                            planningEligibility = v.getString("planningEligibility"),
                            restrictionTags = v.getJSONArray("restrictionTags").strings().toSet(),
                            difficultyEvidence = v.getString("difficultyEvidence"),
                            sourceReferences = (0 until vr.length()).map { refIndex ->
                                val ref = vr.getJSONObject(refIndex)
                                ref.getString("source") + ":" + ref.getString("sourceId")
                            }
                        )
                    }
                )
            }
            ApprovedCatalogGate.accept(families)
        } catch (error: Exception) {
            // Fail closed: si el asset es inválido, no se importa ninguna ficha.
            Log.e(TAG, "No se importó el catálogo aprobado", error)
            emptyList()
        }
    }

    fun toExerciseEntities(families: List<ReviewedExerciseFamily>): List<ExerciseEntity> =
        ApprovedCatalogGate.accept(families).map { family ->
            ExerciseEntity(
                id = family.localId,
                name = family.name,
                category = family.subcategory,
                equipment = family.equipment,
                level = family.level,
                isDemoEligible = ApprovedCatalogGate.eligibleForPlanning(family)
            )
        }

    fun toVariantEntities(families: List<ReviewedExerciseFamily>): List<ExerciseVariantEntity> =
        ApprovedCatalogGate.accept(families).flatMap { family ->
            family.variants.map { variant ->
                ExerciseVariantEntity(
                    exerciseId = family.localId,
                    variantId = variant.id,
                    name = variant.name,
                    equipment = variant.equipment,
                    sourceRefsCsv = variant.sourceReferences.joinToString(";"),
                    difficulty = variant.difficulty,
                    planningEligibility = variant.planningEligibility,
                    restrictionTagsCsv = variant.restrictionTags.joinToString(",")
                )
            }
        }
}
