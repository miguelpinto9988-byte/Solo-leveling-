# Gym Breaker — estándar de imágenes PNG HD / Full HD (perfil)

**Estado: requisito aprobado para nuevas pantallas y recursos.** La revisión 0.9.1 incorpora y audita en GitHub **20 PNG originales** reproducibles para el perfil, sin ampliar miniaturas.

## Matriz de recursos

| Clase | Formato | Tamaño de archivo maestro | Transparencia | Ubicación Android |
|---|---|---:|---|---|
| Fondo del asistente oscuro / claro | PNG RGB | 1080 × 1920 mínimo | No | `drawable-nodpi/bg_profile_setup_*_fhd.png` |
| Fondo del perfil consultable oscuro / claro | PNG RGB | 1080 × 1920 mínimo | No | `drawable-nodpi/bg_profile_summary_*_fhd.png` |
| Marcos de foto Violeta / Cian / Dorado | PNG RGBA | 512 × 512 | Sí: centro y exterior libres | `drawable-nodpi/frame_profile_*.png` |
| Insignias E, D, C, B, A, S | PNG RGBA | 512 × 512 | Sí: contorno exterior libre | `drawable-nodpi/badge_rank_*.png` |
| Iconos: cámara, semana, objetivos, restricciones, tema, editar, dificultad | PNG RGBA | 512 × 512 | Sí | `drawable-nodpi/icon_profile_*.png` |
| Fotografía elegida por el usuario | PNG (origen PNG) o JPG (otro) | Hasta 1536 px conservando proporción | Se conserva en PNG | `filesDir/` privado; nunca se empaqueta |
| Nuevos recortes completos de personajes | PNG RGBA | 1200 px de alto como mínimo | Sí | Biblioteca de originales y exportaciones Android **solo tras aprobación** |
| Banners horizontales nuevos | PNG RGB | 1920 × 1080 mínimo | Normalmente no | `drawable-nodpi/` y catálogo de uso |

No se usa PNG escalado desde 200×133, 256×256 o 720×320 para simular Full HD. Los cuatro fondos son **arte geométrico original dibujado directamente a resolución 1080×1920**; su calidad deberá revisarse también en móvil, con texto legible y diferentes formatos de pantalla.

## Convención de capas

1. `ImageView` de fondo PNG FHD con `CENTER_CROP` para mantener la proporción; las áreas críticas del formulario no dependen de una zona recortable.
2. Tarjetas nativas, textos, inputs y botones **encima del fondo**. La pantalla no es una imagen plana.
3. Foto circular independiente, almacenada sin datos EXIF y sin permisos permanentes. Marco PNG RGBA superpuesto **sin aplicar filtros ni modificar la fotografía**. Se conserva compatibilidad con `hunter_profile_photo.jpg` de versiones anteriores.
4. Las insignias PNG se eligen por el rango calculado desde XP real (E a S); la dificultad del planificador continúa siendo Básico / Intermedio / Avanzado.
5. Los fondos Monarca **no se importan** al perfil y se mantienen exclusivos de su misión semanal.

## Comprobaciones obligatorias

- `python3 scripts/generate_profile_png_assets.py`: crear fondos, marcos, insignias e iconos. Requiere Pillow.
- `python3 scripts/validate_profile_png_assets.py`: audita 20 archivos, tamaño, canal RGB/RGBA, SHA-256 y manifiesto, **sin dependencias adicionales**.
- `python3 -m unittest discover -s scripts/tests`: incluye tests de primera ejecución, acceso desde la fotografía y del catálogo de perfiles.
- CI Android `:app:testDebugUnitTest :app:assembleDebug`: comprobar compilación, integridad y firma del APK.

La referencia de arte queda en `scripts/generate_profile_png_assets.py`, y sus hashes en `art/profile_asset_manifest.json`. El generador de GitHub hace una sola importación controlada al actualizar el script. Ningún recurso se declara HD basándose únicamente en el tamaño de un PNG producido por ampliación.

## Pendientes de migración del resto de la aplicación

**Esta entrega convierte el perfil, no toda la biblioteca de la aplicación.** El resto conserva:
- 34 fondos temáticos `mission_bg_*.webp` de **720×320**: son previews, no Full HD. Exigen nuevos masters **originales** en PNG, no una conversión y ampliación.
- Las seis imágenes `shadow_*.png` históricas, de resolución menor: siguen como avatares heredados y necesitan maestros RGBA HD aportados o aprobados.
- `loading_poster.png` previo **992×1585**, menor de 1080×1920; sustitución por una obra fuente realmente FullHD pendiente.

No se considerará apta para una publicación final la aplicación completa hasta solucionar esas tres familias, realizar pruebas en equipos reales y documentar los derechos de redistribución del arte. El progreso de IA offline, el catálogo y las restricciones no se alteran con esta norma.

## Registro de generación en GitHub (03-11-2026)

Los **20 PNG originales** ya están incorporados físicamente a `app/src/main/res/drawable-nodpi/` mediante el flujo reproducible. Los cuatro fondos tienen resolución FHD vertical (1080 × 1920); los otros 16 recursos son RGBA 512 × 512. La suma de los PNG finales es inferior a 0,5 MB, gracias a las formas originales de geometría/gradientes, sin subir imágenes de baja resolución.

Los resultados de `validate_profile_png_assets.py` son condición obligatoria de CI. El perfil continúa en una rama de revisión, separado de `main`.
