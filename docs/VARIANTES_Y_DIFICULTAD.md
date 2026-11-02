# Familias, variantes y dificultad de Shadow Core AI — v0.5.0

## Requisito de diseño

- Un **ejercicio principal** es una familia canónica de movimiento.
- Las **variantes** son hijas de esa familia y no incrementan los contadores de
  ejercicios principales ni las cuotas de 400 por categoría.
- Cada realización debe conservar: denominación, equipamiento, dificultad y
  estado de revisión; los registros públicos conservan su fuente.
- La IA no puede proponer una variante de una familia no aprobada ni una
  variante cuyos metadatos de dificultad no hayan sido validados.
- Los cambios de nivel no se hacen por castigo o por acumulación de XP.

## Lo que contiene la APK en la versión 0.5

**Catálogo documental offline:** 2.186 registros originales, agrupados
provisionalmente en 1.823 nombres normalizados por categoría (1.794 nombres
distintos globales). Se distribuyen en cinco assets de consulta:
`research_upper.json`, `research_lower.json`, `research_core.json`,
`research_cardio.json` y `research_mobility.json`.

Cada registro documental incluye fuente, ID, equipamiento aproximado y
dificultad tal como la declara su origen cuando coincide inequívocamente con
`beginner/intermediate/advanced`. Si la fuente usa otra escala (p. ej. 1-10)
o no hay dato, la app muestra **Sin determinar** en lugar de asignar un
nivel arbitrario.

La vista del catálogo ofrece búsqueda, selección de las cinco categorías,
filtro de dificultad y **posibles variantes relacionadas** por semejanza
léxica. **Este agrupamiento no demuestra equivalencia biomecánica.**
Cuando hay variantes con distinto nombre, se muestran como sugerencias
pendientes; aún no están fusionadas como familias definitivas.

**Catálogo validado:** permanece **vacío** (0 fichas nuevas aprobadas) porque
todavía no existe una revisión especializada documentada de las 2.186 entradas.
Los doce ejercicios de demostración anteriores siguen disponibles como
prototipo, no como catálogo biomecánicamente validado.

## Modelos y filtros del motor offline

`ApprovedCatalogGate.kt` representa una familia y sus variantes, y exige
nivel, procedencia y justificación para cualquier variante elegible.
`ApprovedCatalogLoader.kt` lee solo `catalog_approved.json`.
`ResearchCatalogLoader.kt` lee solo los cinco archivos de investigación,
con acceso de **solo consulta**.

Room almacena por separado:
- `exercises`: una fila por familia principal;
- `exercise_variants`: filas hijas con dificultad y autorización;
- `sessions.variantSelectionsCsv`: variante elegida durante una sesión;
- `training_profile.preferredDifficulty`: nivel solicitado por el jugador.

Se han añadido migraciones sucesivas de Room 3→4 (metadatos de variantes),
4→5 (historial de variantes) y 5→6 (dificultad elegida).
Las migraciones anteriores 1→2 y 2→3 se conservan.

`ExerciseVariantSelector.kt` filtra cada realización por equipo,
restricciones, nivel autorizado y dificultad. Nunca elige variantes no
aprobadas o de nivel desconocido. El motor respeta tanto el **nivel solicitado**
como la **experiencia declarada**; ninguno de los dos límites se sobrepasa.

- Jugador inicial → solo dificultad inicial.
- Jugador con experiencia intermedia, dificultad intermedia seleccionada
  → puede recibir inicial o intermedia, dando prioridad a la intermedia
  si existe una alternativa apropiada.
- Dificultad avanzada → variantes avanzadas solo de consulta, no propuesta
  automática en este prototipo. Las reglas de seguridad pueden reducir la
  propuesta o exigir revisión.
- Dentro del mismo nivel, la IA puede rotar variantes verificadas menos
  utilizadas según sesiones locales **confirmadas**. No añade volumen,
  repeticiones o cargas automáticamente.

Los ejercicios excluidos, el equipo no disponible, las restricciones
pendientes y el historial de molestias siguen bloqueando o limitando el
planificador. El progreso RPG no obliga a elevar la dificultad física.

## Automatización y pruebas

- `scripts/validate_research_assets.py` comprueba que los 2.186 registros
  y 1.823 grupos nominales estén representados offline, que las dificultades
  no se inventen y que todo quede deshabilitado para planificación.
- `scripts/validate_catalog_reviews.py` exige revisión y evidencia de
  dificultad por variante antes de publicar assets autorizados.
- `ExerciseVariantSelectorTest` y `OfflineTrainingIntelligenceTest`
  cubren límite inicial, intermedio, exclusiones, nivel desconocido,
  equipamiento y alternancia de variantes.
- GitHub Actions ejecuta auditorías, validadores, pruebas y compilación
  cuando la infraestructura esté disponible.

No se puede declarar una APK compilada, ni certificar la correcta ejecución
de las migraciones sobre dispositivos físicos, sin pruebas exitosas.

## Trabajo pendiente para alcanzar el resultado definitivo

1. Revisión técnica de familias y variantes a partir de las fuentes.
2. Confirmación de la dificultad y restricciones de cada realización.
3. Aprobación y carga de familias verificadas en `approved_exercises.json`.
4. Resolución de los 29 conflictos de categoría y variantes con sinónimos.
5. Verificación instrumental de Room y del selector en Android.
6. Cumplimiento, si resulta viable sin inventar patrones, del objetivo de
   400 familias independientes en cada una de las cinco categorías.

No se debe interpretar la lista de registros de fuente como un catálogo
profesional autorizado para entrenar.
