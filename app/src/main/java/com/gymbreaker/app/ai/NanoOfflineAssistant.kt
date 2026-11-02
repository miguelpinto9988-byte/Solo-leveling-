package com.gymbreaker.app.ai

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation

data class NanoReply(val available: Boolean, val text: String)

/**
 * Gemini Nano se ejecuta mediante AICore solo en dispositivos compatibles.
 * No se solicita descarga automática y no se utiliza ningún servidor externo.
 * Esta capa redacta explicaciones, pero NO modifica directamente las rutinas.
 */
class NanoOfflineAssistant {
    suspend fun answer(question: String): NanoReply {
        val prompt = question.trim().take(400)
        if (prompt.isEmpty()) return NanoReply(false, "Escribe una consulta.")
        return try {
            val client = Generation.getClient()
            when (client.checkStatus()) {
                FeatureStatus.AVAILABLE -> {
                    val instructions = """
                        Responde en español, de forma clara y breve. Eres un asistente
                        narrativo de una app de entrenamiento, no un médico.
                        No diagnostiques ni prescribas cambios de carga, repeticiones
                        o sesiones. No propongas castigos físicos por misiones fallidas.
                        Para generar o modificar rutinas se utiliza siempre el motor
                        de restricciones de la aplicación y revisión del usuario.
                        Consulta: $prompt
                    """.trimIndent()
                    val result = client.generateContent(instructions)
                    val text = result.candidates.firstOrNull()?.text.orEmpty().trim()
                    if (text.isBlank()) NanoReply(false, "El modelo no devolvió una respuesta útil.")
                    else NanoReply(true, text.take(1800))
                }
                FeatureStatus.DOWNLOADABLE -> NanoReply(
                    false,
                    "Gemini Nano es compatible, pero requiere una descarga autorizada del modelo antes de funcionar offline."
                )
                FeatureStatus.DOWNLOADING -> NanoReply(
                    false, "El modelo local todavía se está preparando."
                )
                else -> NanoReply(
                    false, "Gemini Nano no está disponible en este dispositivo. Se utiliza el motor local por reglas."
                )
            }
        } catch (e: Exception) {
            NanoReply(false, "No fue posible iniciar el modelo local; continúa disponible el asistente por reglas.")
        }
    }
}
