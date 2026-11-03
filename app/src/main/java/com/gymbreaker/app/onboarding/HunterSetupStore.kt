package com.gymbreaker.app.onboarding

import android.content.Context
import com.gymbreaker.app.data.TrainingDatabase
import com.gymbreaker.app.data.TrainingProfileEntity
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Datos de presentación local: no salen del dispositivo ni alteran el catálogo
 * de ejercicios aprobado. El nivel es independiente del rango del cazador.
 */
data class HunterSetup(
    var hunterName: String = "",
    var gender: String = "Prefiero no decirlo",
    var age: String = "",
    var height: String = "",
    var weight: String = "",
    var darkTheme: Boolean = true,
    var accent: String = "#A56CFF",
    var frame: String = "Violeta",
    var photoSelected: Boolean = false,
    var avatarVariant: String = "Igris",
    var preferredDifficulty: String = "inicio",
    var primaryGoal: String = "Mejorar mi condición física",
    var subGoal: String = "",
    var groups: MutableList<String> = mutableListOf(
        "Descanso", "Tren superior", "Descanso", "Tren inferior",
        "Descanso", "Movilidad", "Descanso"
    ),
    var weeklyMissions: Int = 3,
    var restSeconds: Int = 60,
    var minutes: Int = 25,
    var equipment: String = "HOME",
    var restrictions: MutableSet<String> = mutableSetOf()
) {
    companion object {
        val difficultyLevels = listOf("inicio", "intermedio", "avanzado")
        val trainingGroups = listOf(
            "Descanso", "Cuerpo completo", "Tren superior",
            "Tren inferior", "Tren central", "Cardio", "Movilidad"
        )
        val days = listOf("D", "L", "M", "X", "J", "V", "S")
        val goals = listOf(
            "Ganar fuerza", "Mejorar resistencia", "Mejorar movilidad",
            "Mejorar mi condición física", "Sentirme más activo"
        )
        val restrictionOptions = listOf(
            "Molestia o lesión actual", "Sin equipamiento", "Poco tiempo disponible",
            "Evitar impacto", "Movilidad limitada", "Otra restricción"
        )
    }
}

object HunterSetupStore {
    private const val PREFS = "hunter_preferences"
    private const val KEY = "onboarding_profile_v1"
    private const val DONE = "profile_created"

    fun completed(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(DONE, false)

    fun load(context: Context): HunterSetup {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val result = HunterSetup(
            accent = prefs.getString("accent", "#A56CFF") ?: "#A56CFF",
            darkTheme = prefs.getBoolean("dark_theme", true)
        )
        val raw = prefs.getString(KEY, null) ?: return result
        return try {
            val obj = JSONObject(raw)
            result.apply {
                hunterName = obj.optString("name", "")
                gender = obj.optString("gender", "Prefiero no decirlo")
                age = obj.optString("age", "")
                height = obj.optString("height", "")
                weight = obj.optString("weight", "")
                darkTheme = obj.optBoolean("darkTheme", true)
                accent = obj.optString("accent", "#A56CFF")
                frame = obj.optString("frame", "Violeta")
                photoSelected = obj.optBoolean("photoSelected", false)
                avatarVariant = obj.optString("avatarVariant", "Igris")
                    .takeIf { it in listOf("Igris", "Beru", "Tank", "Iron", "Tusk", "Kaisel") } ?: "Igris"
                preferredDifficulty = obj.optString("difficulty", "inicio")
                    .takeIf { it in HunterSetup.difficultyLevels } ?: "inicio"
                primaryGoal = obj.optString("goal", HunterSetup.goals[3])
                    .takeIf { it in HunterSetup.goals } ?: HunterSetup.goals[3]
                subGoal = obj.optString("subGoal", "").take(300)
                val stored = obj.optJSONArray("groups")
                if (stored != null && stored.length() == 7) {
                    groups = (0 until 7).map { index ->
                        stored.optString(index).takeIf {
                            it in HunterSetup.trainingGroups
                        } ?: "Descanso"
                    }.toMutableList()
                }
                weeklyMissions = obj.optInt("weeklyMissions", 3).coerceIn(1, 7)
                restSeconds = obj.optInt("restSeconds", 60)
                    .takeIf { it in listOf(30, 60, 90) } ?: 60
                minutes = obj.optInt("minutes", 25).coerceIn(10, 90)
                equipment = obj.optString("equipment", "HOME")
                    .takeIf { it in setOf("HOME", "DUMBBELLS", "GYM") } ?: "HOME"
                restrictions = obj.optJSONArray("restrictions")?.let { arr ->
                    (0 until arr.length()).mapNotNull { i ->
                        arr.optString(i).takeIf { it in HunterSetup.restrictionOptions }
                    }.toMutableSet()
                } ?: mutableSetOf()
            }
        } catch (_: Exception) {
            result
        }
    }

    suspend fun save(context: Context, data: HunterSetup) {
        require(data.hunterName.trim().length in 2..35)
        require(data.preferredDifficulty in HunterSetup.difficultyLevels)
        require(data.groups.size == 7 && data.groups.all {
            it in HunterSetup.trainingGroups
        })
        require(data.weeklyMissions in 1..7)
        require(data.minutes in 10..90)
        require(data.restSeconds in setOf(30, 60, 90))
        require(data.primaryGoal in HunterSetup.goals)
        val obj = JSONObject().apply {
            put("name", data.hunterName.trim())
            put("gender", data.gender)
            put("age", data.age)
            put("height", data.height)
            put("weight", data.weight)
            put("darkTheme", data.darkTheme)
            put("accent", data.accent)
            put("frame", data.frame)
            put("photoSelected", data.photoSelected)
            put("avatarVariant", data.avatarVariant)
            put("difficulty", data.preferredDifficulty)
            put("goal", data.primaryGoal)
            put("subGoal", data.subGoal.take(300))
            put("groups", JSONArray(data.groups))
            put("weeklyMissions", data.weeklyMissions)
            put("restSeconds", data.restSeconds)
            put("minutes", data.minutes)
            put("equipment", data.equipment)
            put("restrictions", JSONArray(data.restrictions.toList()))
        }
        withContext(Dispatchers.IO) {
            val dao = TrainingDatabase.get(context.applicationContext).trainingDao()
            val existing = dao.getProfile() ?: TrainingProfileEntity()
            // Si se declara dolor, la revisión de seguridad siempre queda pendiente.
            val review = existing.requiresReview ||
                "Molestia o lesión actual" in data.restrictions
            dao.upsertProfile(
                existing.copy(
                    experience = when (data.preferredDifficulty) {
                        "avanzado" -> "EXPERIENCED"
                        "intermedio" -> "INTERMEDIATE"
                        else -> "BEGINNER"
                    },
                    preferredDifficulty = data.preferredDifficulty,
                    availableMinutes = data.minutes,
                    requiresReview = review,
                    excludedCategoriesCsv = (
                        existing.excludedCategoriesCsv.split(',').filter { it.isNotBlank() } +
                        if ("Movilidad limitada" in data.restrictions) listOf("Alto impacto")
                        else emptyList()
                    ).distinct().joinToString(",")
                )
            )
        }
        // No marcar onboarding terminado hasta que Room haya guardado el perfil.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, obj.toString())
            .putBoolean("dark_theme", data.darkTheme)
            .putString("accent", data.accent)
            .putBoolean(DONE, true)
            .commit()
    }

    /** La interpretación propone; jamás cambia el perfil sin una confirmación. */
    fun suggestGoal(freeText: String): String? {
        val normalized = java.text.Normalizer.normalize(
            freeText.lowercase(Locale.ROOT), java.text.Normalizer.Form.NFD
        ).replace(Regex("[\\u0300-\\u036f]"), "")
        return when {
            normalized.contains("dolor") || normalized.contains("lesion") ||
                normalized.contains("molestia") -> null // Revisión humana primero.
            listOf("resist", "cardio", "correr", "caminar").any { it in normalized } ->
                "Mejorar resistencia"
            listOf("movil", "flexib", "estir").any { it in normalized } ->
                "Mejorar movilidad"
            listOf("fuerza", "fortal", "musculo").any { it in normalized } ->
                "Ganar fuerza"
            listOf("activo", "energia", "habito").any { it in normalized } ->
                "Sentirme más activo"
            listOf("condicion", "bienestar", "salud").any { it in normalized } ->
                "Mejorar mi condición física"
            else -> null
        }
    }
}
