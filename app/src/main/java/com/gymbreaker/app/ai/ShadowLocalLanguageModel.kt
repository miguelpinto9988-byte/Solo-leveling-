package com.gymbreaker.app.ai

import android.content.Context
import android.os.Build
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Inferencia GGUF real, local y sin permisos de Internet.
 *
 * El modelo solo responde preguntas conversacionales de baja criticidad.
 * Rutinas, restricciones, datos del usuario y dificultad siguen bajo las
 * reglas deterministas de OfflineTrainingIntelligence.
 *
 * Una carga fallida no habilita ni altera las decisiones del planificador.
 */
object ShadowLocalLanguageModel {
    const val MODEL_ASSET = "shadow_core/SmolLM2-135M-Instruct-Q2_K.gguf"
    const val MODEL_SHA256 =
        "478cca924101ed67bb2751f597be41532b2f4817ee2ab1b0d7cc379c76e79faf"
    private val mutex = Mutex()
    private const val MODEL_BYTES = 88_202_816L

    /** Prepara los pesos privados verificando el SHA-256 y comunica avance real. */
    suspend fun prepare(context: Context, onProgress: (Int) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            if (!Build.SUPPORTED_ABIS.contains("arm64-v8a")) {
                onProgress(100)
                return@withContext false
            }
            mutex.withLock {
                try {
                    getAuthenticatedModelFile(context.applicationContext, onProgress)
                    true
                } catch (_: Exception) {
                    onProgress(100)
                    false
                }
            }
        }


    private const val SYSTEM_PROMPT =
        "Eres Shadow Core, un asistente de una aplicacion de ejercicios. " +
        "Responde breve, con prudencia y en espanol sencillo. " +
        "No inventes datos de entrenamiento ni prometas resultados. " +
        "No prescribas ejercicios, lesiones, cargas o progresiones. " +
        "Para rutinas, preferencias, molestias y restricciones deriva al " +
        "planificador seguro de la aplicacion. Si no sabes algo, dilo."

    /**
     * Siempre se ejecuta desde un contexto suspendible, nunca en el hilo UI.
     * Limita el texto y la generación para reducir latencia y memoria.
     */
    suspend fun answer(context: Context, question: String): String? =
        withContext(Dispatchers.IO) {
            if (!Build.SUPPORTED_ABIS.contains("arm64-v8a")) return@withContext null
            mutex.lock()
            try {
                val file = getAuthenticatedModelFile(context.applicationContext)
                // No mantener referencias nativas indefinidamente: liberación garantizada.
                val engine = Llama.loadModel(
                    modelPath = file.absolutePath,
                    config = LlamaConfig(contextSize = 1024, threads = 2)
                )
                try {
                    val response = Llama.complete(
                        engine,
                        prompt = question.take(400),
                        systemPrompt = SYSTEM_PROMPT,
                        maxTokens = 96
                    ).text.trim()
                    response.take(900).takeIf { it.isNotBlank() }
                } finally {
                    Llama.releaseModel(engine)
                }
            } catch (failure: Exception) {
                // Error recuperable: nunca fingir que hubo inferencia exitosa.
                null
            } catch (failure: UnsatisfiedLinkError) {
                // Algunos equipos no admiten la ABI nativa; usar reglas locales.
                null
            } finally {
                mutex.unlock()
            }
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun getAuthenticatedModelFile(context: Context, onProgress: (Int) -> Unit = {}): File {
        val dir = File(context.filesDir, "shadow_core")
        check(dir.isDirectory || dir.mkdirs()) { "No se pudo crear el directorio privado" }
        val target = File(dir, "SmolLM2-135M-Instruct-Q2_K.gguf")
        if (target.isFile && sha256(target) == MODEL_SHA256) {
            onProgress(100)
            return target
        }

        // No reutilizar pesos viejos o corruptos.
        target.delete()
        val tmp = File(dir, "model.download.partial")
        tmp.delete()
        try {
            var copied = 0L
            context.assets.open(MODEL_ASSET).buffered().use { input ->
                tmp.outputStream().buffered().use { output ->
                    val buffer = ByteArray(256 * 1024)
                    var lastPercent = -1
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copied += count
                        val percent = (copied * 100 / MODEL_BYTES).toInt().coerceIn(0, 99)
                        if (percent > lastPercent) {
                            lastPercent = percent
                            onProgress(percent)
                        }
                    }
                }
            }
            check(copied == MODEL_BYTES) { "Longitud GGUF inesperada" }
            check(sha256(tmp) == MODEL_SHA256) { "Pesos GGUF incorrectos" }
            check(tmp.renameTo(target)) { "No se pudo instalar el modelo local" }
            onProgress(100)
            return target
        } finally {
            tmp.delete()
        }
    }
}
