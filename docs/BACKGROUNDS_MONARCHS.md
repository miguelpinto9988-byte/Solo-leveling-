# Biblioteca visual de escenarios — Misiones y monarcas

**Entrega de diseño v1, optimización para tarjetas (no arte FullHD).**

Se generaron **34 fondos WebP 720×320** procedentes de un atlas original de este proyecto, distribuido en la conversación. La versión actual de GitHub procede de una **miniatura 200×133** firmada por el servicio de transferencia; se amplió y oscureció para mantener legibles las tarjetas. Es un recurso visual provisional; para la versión final debe sustituirse por el conjunto de 34 WebP de mayor calidad entregado en `Solo-Leveling-Gym-Breaker-34-Fondos-Tematicos.zip` y pasar de nuevo las pruebas.

## Reparto

| Categoría | Cantidad | Uso |
|---|---:|---|
| Entornos generales | 6 | Sistema, entrenamiento, preferencias |
| Exploración | 6 | Catálogo, zonas futuras |
| Sombras y acompañantes | 7 | Misiones normales y pantalla Sombras |
| Portales | 6 | Panel del asistente y zonas |
| **Monarcas** | **9** | **Solo tarjeta de misión Monarca semanal y selector Monarca** |
| **Total** | **34** | Máximo 2 MB de assets decorativos |

La lista de nueve monarcas usa los nombres confirmados para el universo original: Antares, Rakan, Sillad, Querehsha, Ashborn, Baran, Tarnak, Legia y Yogumunt. El personaje central y las sombras acompañantes no se clasifican como monarcas por estética. Las imágenes de personajes de procedencia externa no se importan a la APK por el mero hecho de estar públicamente disponibles.

## Implementación

- `MissionBackgroundCatalog.kt` contiene exclusivamente las rutas a los recursos de cada categoría.
- `MainActivity.kt` asigna el fondo Monarca **solo a la tarjeta de misión semanal**; las diarias siempre llevan el de la sombra acompañante.
- El selector **EXPLORAR FONDOS MONARCA** permite elegir solo el ambiente. El entrenamiento, dificultad y progreso no cambian.
- Si la persona no elige uno, el tema visual rota cada semana de forma determinista.
- Cada tarjeta incluye un degradado oscuro para garantizar legibilidad del texto. El fondo no aporta semántica a TalkBack.
- El atlas de concepto se almacena en `art/source/background_atlas.png`; los recortes publicados están en `app/src/main/res/drawable-nodpi/mission_bg_*.webp`. Para regenerarlos se usa `python scripts/build_mission_backgrounds.py art/source/background_atlas.png app/src/main/res/drawable-nodpi` con Pillow.

## Criterios pendientes

1. Reemplazar imágenes provisionales ampliadas desde miniatura por los 34 WebP originales de mayor resolución o nuevas ilustraciones independientes y aprobadas.
2. Hacer una prueba visual en Android pequeño/grande y en temas claro/oscuro.
3. Confirmar documentalmente la licencia de uso y modificación de personajes antes de publicar al público.
4. Terminar las recreaciones individuales pendientes: esta entrega cambia **fondos**, no genera aún una ficha final de cada personaje ni una misión distinta por monarca.

**Las misiones Monarca son narrativas y voluntarias: nunca incrementan ejercicios, cargas o intensidad como castigo.**
