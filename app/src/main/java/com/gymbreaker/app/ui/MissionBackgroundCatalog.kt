package com.gymbreaker.app.ui

import androidx.annotation.DrawableRes
import com.gymbreaker.app.R

/**
 * Biblioteca estricta: las imágenes de los monarcas pertenecen únicamente
 * a la misión Monarca semanal y a su selector visual.
 * Ninguna otra pantalla utiliza mission_bg_monarch_*.
 */
object MissionBackgroundCatalog {
    enum class MonarchTheme(
        val label: String,
        val narrative: String,
        @DrawableRes val background: Int
    ) {
        DESTRUCTION("Antares · Destrucción", "Ruinas carmesí", R.drawable.mission_bg_monarch_destruction),
        BEASTS("Rakan · Colmillos", "Reino de las bestias", R.drawable.mission_bg_monarch_beasts),
        FROST("Sillad · Escarcha", "Santuario de hielo", R.drawable.mission_bg_monarch_frost),
        PLAGUE("Querehsha · Plagas", "Nido sombrío", R.drawable.mission_bg_monarch_plague),
        SHADOWS("Ashborn · Sombras", "Trono de las sombras", R.drawable.mission_bg_monarch_shadows),
        WHITE_FLAMES("Baran · Llamas blancas", "Dominio del fuego", R.drawable.mission_bg_monarch_white_flames),
        IRON_BODY("Tarnak · Cuerpo de hierro", "Fortaleza de piedra", R.drawable.mission_bg_monarch_iron_body),
        BEGINNING("Legia · Principio", "Ruinas de los gigantes", R.drawable.mission_bg_monarch_beginning),
        TRANSFIGURATION("Yogumunt · Transfiguración", "Portal de los espectros", R.drawable.mission_bg_monarch_transfiguration)
    }

    val monarchs = MonarchTheme.entries.toList()

    fun monarchOfWeek(weekOfYear: Int): MonarchTheme =
        monarchs[(weekOfYear.coerceAtLeast(1) - 1) % monarchs.size]

    fun selectedMonarch(saved: String?, weekOfYear: Int): MonarchTheme =
        monarchs.firstOrNull { it.name == saved } ?: monarchOfWeek(weekOfYear)

    // La rotación diaria reutiliza todos los escenarios generales,
    // de exploración y portales sin exponer fondos Monarca.
    private val general = intArrayOf(
        R.drawable.mission_bg_general_city_sunset,
        R.drawable.mission_bg_general_gym_hall,
        R.drawable.mission_bg_general_association,
        R.drawable.mission_bg_general_training_room,
        R.drawable.mission_bg_general_night_city,
        R.drawable.mission_bg_general_hunters_base
    )
    private val exploration = intArrayOf(
        R.drawable.mission_bg_exploration_forest,
        R.drawable.mission_bg_exploration_ruins,
        R.drawable.mission_bg_exploration_crystals,
        R.drawable.mission_bg_exploration_mineshaft,
        R.drawable.mission_bg_exploration_canyon,
        R.drawable.mission_bg_exploration_shadow_plain
    )
    private val portals = intArrayOf(
        R.drawable.mission_bg_portal_blue_gate,
        R.drawable.mission_bg_portal_purple_gate,
        R.drawable.mission_bg_portal_crimson_gate,
        R.drawable.mission_bg_portal_unstable,
        R.drawable.mission_bg_portal_shadow_world,
        R.drawable.mission_bg_portal_gate_hall
    )

    private fun rotate(items: IntArray, number: Int): Int =
        items[Math.floorMod(number, items.size)]

    @DrawableRes
    fun normalBackground(screen: String, companion: String, dayOfYear: Int = 0): Int =
        when (screen) {
            "SYSTEM" -> rotate(general, dayOfYear)
            "TRAIN" -> rotate(general, dayOfYear + 1)
            "SHADOWS", "MISSIONS" -> companionBackground(companion)
            "AI" -> rotate(portals, dayOfYear)
            "SETTINGS" -> rotate(general, dayOfYear + 3)
            "CATALOG" -> rotate(exploration, dayOfYear)
            else -> rotate(exploration, dayOfYear + 2)
        }

    @DrawableRes
    fun companionBackground(companion: String): Int = when (companion.lowercase()) {
        "igris" -> R.drawable.mission_bg_shadow_igris
        "beru" -> R.drawable.mission_bg_shadow_beru
        "tank" -> R.drawable.mission_bg_shadow_tank
        "iron" -> R.drawable.mission_bg_shadow_iron
        "tusk" -> R.drawable.mission_bg_shadow_tusk
        "kaisel" -> R.drawable.mission_bg_shadow_kaisel
        else -> R.drawable.mission_bg_shadow_kiba
    }

    @DrawableRes
    fun monarchBackground(theme: MonarchTheme): Int = theme.background
}
