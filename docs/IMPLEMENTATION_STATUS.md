# Registro de implementación · 0.6.0

## Integrado al código fuente (sin certificación de ejecución)
- Proyecto Kotlin + interfaces XML, Gradle Kotlin DSL, Android SDK.
- MVVM con ViewModel, StateFlow, Coroutines y Flow.
- Room + SQLite: ejercicios de demostración y sesiones completadas.
- Motor local determinista con exclusiones por categorías y ejercicios, experiencia declarada y bloqueo de revisión.
- Estimador de esfuerzo offline que requiere cinco sesiones comparables.
- Perfil Room y metadatos de sesiones con migración 1→2.
- Sistema RPG básico: experiencia virtual, nivel, rango y avance semanal.
- Misiones Monarca narrativas, opcionales; sin incrementos punitivos de carga.
- Shadow Core AI integrado: reconocimiento especializado de solicitudes en español, parámetros de rutina y estadísticas locales. No es un LLM ni una IA generativa de propósito general.
- Navegación: Sistema, Misiones, Entrenar, Sombras, IA y Ajustes.
- Temas claro y oscuro, texto en negrita y cuatro colores de acento para botones y marcos.
- Gradientes suaves para tarjetas y seis acompañantes originales PNG transparentes 256 × 256.
- Alarma diaria aproximada con permiso de notificaciones, restauración al reinicio.
- Pruebas JUnit del planificador, rangos y asistente.
- GitHub Actions configurado para pruebas y compilación APK de depuración.

- Presupuesto automatizado de recursos y modelos offline de hasta 90 MB; no implica tamaño final de APK ni RAM.
- Pruebas unitarias nuevas para la comprensión local y el aprendizaje descriptivo.

## Investigación del catálogo (solo consulta en la app; no habilitado para rutinas)
- Cinco categorías principales en `CatalogTaxonomy.kt`: tren superior, tren inferior,
  zona central, cardio y movilidad.
- 2.186 registros de metadatos de cuatro fuentes abiertas conservados en staging.
- 1.823 pares categoría/nombre distintos, 1.794 nombres distintos globales.
- 44 grupos léxicos de posibles variantes en cola de revisión manual;
  29 conflictos entre categorías y diez casos documentados de cribado editorial.
- 273 denominaciones de cardio/movilidad priorizadas para revisión
  (128 y 145, respectivamente).
- Nuevo archivo de familias aprobado **vacío**, más validador de fuentes,
  revisión humana y nivel de autorización.
- `ApprovedCatalogLoader` importa desde assets únicamente familias aprobadas;
  variantes subordinadas en Room y migraciones adicionales 3→4, 4→5 y 5→6.
- Cinco archivos de investigación de solo consulta incluidos en la APK, con
  2.186 entradas y 1.823 nombres nominales por categoría.
- Cinco índices de posibles familias y niveles que generan 1.089 grupos
  **no revisados**. De ellos, 28 agrupan nombres diferentes por
  similitud textual y requieren especial atención.
- Pantalla de exploración por familia y dificultad: inicial, intermedio,
  avanzado y sin determinar. Los datos de investigación no se proponen para entrenar.
- Panel completo de Shadow Core AI: consultas, interpretación, proceso
  determinista de seis etapas, informe JSON codificable/decodificable,
  tamaño de recursos y preferencias.
- Consultas en español sobre explicación de decisiones, catálogo y recursos.
- Comprobación del presupuesto de 90 MB con auditoría en GitHub Actions;
  los assets actuales se estimaron en aproximadamente 1,78 MB de
  archivos fuente, sin incluir código DEX ni RAM.
- Dificultades de origen normalizadas únicamente si la fuente usa niveles
  explícitos, y no certificadas como dificultades biomecánicas.
- Jugador elige dificultad y experiencia de forma independiente. Selector offline
  prioriza variantes revisadas compatibles y alterna entre opciones del mismo
  nivel según historial confirmado; no fuerza progresión.
- Registro de variante elegida en historial local de sesiones.
- Auditorías y pruebas del catálogo incorporadas al flujo CI.
- **No son 2.000 ejercicios principales comprobados**: el solapamiento semántico
  y la existencia de muchas variantes impiden certificar la cifra.
- Cardio y movilidad concentran los mayores déficits de nombres (272 y 255
  antes de la revisión de variantes).
- Ningún ejercicio de staging se activa en Shadow Core AI hasta revisión.
- Auditoría automática: `python3 scripts/audit_catalog.py`.
- Información: [CATALOG_RESEARCH.md](CATALOG_RESEARCH.md).

## Pendiente para el producto objetivo
- Los **2000 ejercicios revisados**: por ahora hay 12 ejemplos, sin validación biomecánica.
- Editor completo de rutinas, sesión guiada por series/descansos y estadísticas avanzadas.
- Sistema de acompañantes animados con partículas y aura reactiva.
- Imágenes oficiales/escenarios autorizados: se necesitan los archivos y el alcance de las licencias.
- La evaluación e integración opcional de un modelo neuronal cuantizado queda pendiente; el núcleo propio actual funciona sin pesos de modelo y sin conexión.
- Comprobación con datos reales de las estimaciones estadísticas, la detección de intenciones y su calidad.
- Misiones Monarca y recompensas complejas.
- Alarmas por días, sonidos personalizados y ajustes de precisión según permisos.
- Exportación/restauración segura, pruebas instrumentadas y revisión de migraciones en dispositivos reales.
- Validación de rendimiento, accesibilidad y compatibilidad física.
- APK firmada para distribución: la app-debug.apk de CI es solo una compilación de desarrollo.

## Seguridad
- Sin permisos de micrófono, cámara, contactos, ubicación ni Internet en esta primera versión.
- Sin secretos ni credenciales.
- Los puntos y rangos son símbolos del juego, no indicadores de salud.
- Las misiones no penalizan con ejercicios adicionales ni aumentos de carga.
- La app no verifica técnica ni diagnostica lesiones.

## Compilación
El flujo CI usa JDK 17, SDK 36, Gradle 8.13 y AGP 8.13.2 con Kotlin 2.2.21. Incluye `verifyShadowCoreModelBudget` y `verifyOfflineAiResourceBudget`, además de
`validate_family_groups.py`.
Abrir **Actions → Android Debug CI** para ver resultados. Solo un resultado exitoso y
un artefacto presente permitirían afirmar que la APK de prueba se ha compilado.

Versión mínima técnica provisional: Android 8.0 (API 26). Android 15+ será evaluado,
según hardware y disponibilidad del asistente generativo, antes del lanzamiento.
