package com.gymbreaker.app.ai

import org.json.JSONArray
import org.json.JSONObject

/**
 * Codificación/decodificación local de un informe interpretado de reglas.
 * No guarda ni envía datos. El usuario decide si lo copia o comparte.
 * No exporta instrucciones para saltarse restricciones de entrenamiento.
 */
object ShadowAuditCodec {
    const val VERSION = 1

    fun encode(report: ShadowAuditReport): String {
        val json = JSONObject()
        json.put("schemaVersion", VERSION)
        json.put("engine", "Shadow Core AI / offline rules")
        json.put("status", report.status)
        val stages = JSONArray()
        report.stages.forEach { stage ->
            stages.put(JSONObject().put("title", stage.title).put("detail", stage.detail))
        }
        json.put("stages", stages)
        json.put("selectedFamilies", JSONArray(report.selectedFamilies))
        json.put("notice", report.disclaimer)
        return json.toString(2)
    }

    fun decode(json: String): ShadowAuditReport {
        require(json.length <= 250_000) { "Informe demasiado grande" }
        val root = JSONObject(json)
        require(root.getInt("schemaVersion") == VERSION)
        require(root.getString("engine") == "Shadow Core AI / offline rules")
        val items = root.getJSONArray("stages")
        require(items.length() in 1..24)
        val stages = (0 until items.length()).map { i ->
            val node = items.getJSONObject(i)
            ShadowAuditStage(node.getString("title"), node.getString("detail"))
        }
        val selected = root.getJSONArray("selectedFamilies")
        require(selected.length() <= 100)
        return ShadowAuditReport(
            status = root.getString("status"),
            stages = stages,
            selectedFamilies = (0 until selected.length()).map { selected.getString(it) },
            disclaimer = root.getString("notice")
        )
    }
}
