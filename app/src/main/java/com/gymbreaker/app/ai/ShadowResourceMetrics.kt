package com.gymbreaker.app.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ShadowResourceMetrics(
    val bytes: Long,
    val maxBytes: Long,
    val sourceRecords: Int,
    val nominalNames: Int,
    val proposedGroups: Int,
    val approvedFamilies: Int,
    val assetCount: Int,
    val modelBytes: Long = 0L
) {
    val percentUsed: Double get() =
        if (maxBytes == 0L) 0.0 else 100.0 * bytes / maxBytes

    fun description(): String =
        "NÚCLEO OFFLINE · RECURSOS DE DATOS\n" +
            "Archivos medidos: " + assetCount + "\n" +
            "Tamaño de assets: " + "%.2f".format(bytes / 1_000_000.0) + " MB\n" +
            "Pesos GGUF incluidos: " + "%.2f".format(modelBytes / 1_000_000.0) + " MB\n" +
            "Presupuesto máximo: " + "%.0f".format(maxBytes / 1_000_000.0) + " MB\n" +
            "Porcentaje: " + "%.2f".format(percentUsed) + "%\n\n" +
            "Referencias documentales (no ejercicios): " + sourceRecords + "\n" +
            "Variantes nominales conservadas: " + nominalNames + "\n" +
            "Familias propuestas sin revisión técnica: " + proposedGroups + "\n" +
            "Familias revisadas incorporadas: " + approvedFamilies + "\n\n" +
            "Los tamaños corresponden a los assets de catálogo, índices y " +
            "licencias incluidos; no representan RAM, tamaño total de APK " +
            "ni el código compilado. El GGUF requiere RAM y una CPU ARM64; " +
            "el diálogo generado es experimental y no decide rutinas."
}

/** Cuenta bytes decodificados de recursos locales; no necesita permisos de Internet. */
object ShadowResourceMetricsReader {
    suspend fun read(context: Context): ShadowResourceMetrics =
        withContext(Dispatchers.IO) {
            val manifest = JSONObject(context.assets.open("ai_manifest.json")
                .bufferedReader().use { it.readText() })
            require(manifest.getInt("schemaVersion") == 1)
            require(!manifest.getBoolean("networkRequired"))
            val files = manifest.getJSONArray("assetList")
            var total = 0L
            for (i in 0 until files.length()) {
                val name = files.getString(i)
                require(!name.contains('/') && !name.contains(".."))
                context.assets.open(name).use { stream ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = stream.read(buffer)
                        if (count < 0) break
                        total += count
                    }
                }
            }
            require(manifest.getBoolean("modelWeightsIncluded"))
            val modelPath = manifest.getString("modelAssetPath")
            require(modelPath == ShadowLocalLanguageModel.MODEL_ASSET)
            var modelBytes = 0L
            context.assets.open(modelPath).use { stream ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    modelBytes += count
                }
            }
            total += modelBytes
            val limit = manifest.getLong("storageBudgetBytes")
            require(total <= limit)
            require(limit == 90_000_000L)
            ShadowResourceMetrics(
                bytes = total,
                maxBytes = limit,
                sourceRecords = manifest.getInt("documentedRecords"),
                nominalNames = manifest.getInt("nominalNamesByCategory"),
                proposedGroups = manifest.getInt("proposedGroups"),
                approvedFamilies = manifest.getInt("biomechanicallyApprovedGroups"),
                assetCount = files.length() + 1,
                modelBytes = modelBytes
            )
        }
}
