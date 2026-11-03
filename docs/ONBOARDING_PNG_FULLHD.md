# Biblioteca PNG HD · cinco paneles de creación de perfil

Este documento describe **la pantalla de primera ejecución**, `OnboardingActivity`. **No modifica `ProfileActivity`**, la pantalla donde se consulta el perfil una vez creado. Esa pantalla sigue abriéndose solamente al pulsar la fotografía de la esquina superior de Inicio.

## Cinco paneles aprobados

| Paso | Contenido | Fondo PNG Full HD | Icono PNG transparente |
|---|---|---|---|
| 1 | Identidad: nombre obligatorio y datos opcionales | `bg_onboarding_identity_fullhd.png` | `icon_profile_identity.png` |
| 2 | Apariencia: fotografía propia/avatares, tres marcos, tema y acento | `bg_onboarding_appearance_fullhd.png` | `icon_profile_photo.png`, `icon_profile_theme.png` |
| 3 | Tu semana: Misiones **1–7**, 7 días, grupos, descanso y tiempo | `bg_onboarding_week_fullhd.png` | `icon_profile_week.png` |
| 4 | Objetivo principal, subobjetivo y tres dificultades | `bg_onboarding_goals_fullhd.png` | `icon_profile_goal.png`, `icon_profile_difficulty.png` |
| 5 | Restricciones opcionales, equipamiento y protección | `bg_onboarding_restrictions_fullhd.png` | `icon_profile_restrictions.png` |

La introducción reutiliza el fondo del paso 1. El panel final **¡Perfil creado!** conserva el resumen de rangos, objetivo, semana y restricciones, e incorpora la insignia `badge_rank_e.png`, sin añadir una sexta etapa de configuración.

## Norma de imagen aplicada

- **Cinco fondos PNG 1080 × 1920**, generados directamente a resolución nativa mediante formas originales. No se aumenta una miniatura ni se importa arte ajeno.
- **Nueve iconos PNG 512 × 512 RGBA**, fondo realmente transparente.
- **Tres marcos PNG 1024 × 1024 RGBA**, centro transparente, versiones violeta/cian/dorado.
- **Insignia E PNG 512 × 512 RGBA**.
- El arte maestro se duplica en `assets_master/profile/`; las versiones de Android residen en `app/src/main/res/drawable-nodpi/`.
- `ImageView.ScaleType.CENTER_CROP` conserva proporciones del fondo en pantallas distintas de 9:16. Marcos e iconos usan `FIT_CENTER`; no se estiran fotografías.
- Sobre los fondos se aplica un velo oscuro para legibilidad de campos y controles.
- Los personajes preexistentes `shadow_*.png` **son PNG RGBA de 320 × 320**, por lo que **todavía no cumplen** un estándar HD maestro de personaje: no se reescalan artificialmente ni se presentan como nuevas ilustraciones de alta resolución. Deben sustituirse cuando se disponga de originales adecuados y con derechos documentados.
- La foto del usuario sigue entrando mediante el selector nativo de Android y se conserva privadamente como JPEG optimizado, sin pedir acceso permanente a la galería.

## Reproducción y validación

```bash
python3 -m pip install Pillow==11.3.0
python3 scripts/build_profile_png_assets.py
python3 -m unittest discover -s scripts/tests -p 'test_onboarding_png_assets.py'
```

El workflow `.github/workflows/build-profile-png.yml` genera/verifica los PNG (dimensiones, RGBA y canal alfa) y los incorpora a la rama de diseño. El workflow Android valida tests, Kotlin, recursos y firma.

**Conservación funcional:** el selector de misiones sigue limitado de 1 a 7; la dificultad es Básico/Intermedio/Avanzado; todos comienzan en rango E, el progreso XP no se modifica y Shadow Core propone ajustes de objetivos únicamente previa confirmación. Las restricciones bloquean la planificación no autorizada.

**Criterios para una publicación:** verificar contraste y disposición visual en tamaños pequeños y grandes, selección de imágenes, perfiles existentes, temática clara, y comprobar las licencias de los recursos de terceros. Los nuevos fondos geométricos de los cinco pasos son ilustraciones originales reproducibles, no capturas oficiales del anime.
