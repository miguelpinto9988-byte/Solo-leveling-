# Registro de implementación · 0.3.0

## Integrado al código fuente (sin certificación de ejecución)
- Proyecto Kotlin + interfaces XML, Gradle Kotlin DSL, Android SDK.
- MVVM con ViewModel, StateFlow, Coroutines y Flow.
- Room + SQLite: ejercicios de demostración y sesiones completadas.
- Motor local determinista con exclusiones por categorías y ejercicios, experiencia declarada y bloqueo de revisión.\n- Estimador de esfuerzo offline que requiere cinco sesiones comparables.\n- Perfil Room y metadatos de sesiones con migración 1→2.
- Sistema RPG básico: experiencia virtual, nivel, rango y avance semanal.
- Misiones Monarca narrativas, opcionales; sin incrementos punitivos de carga.
- Shadow Core AI integrado: reconocimiento especializado de solicitudes en español, parámetros de rutina y estadísticas locales. No es un LLM ni una IA generativa de propósito general.
- Navegación: Sistema, Misiones, Entrenar, Sombras, IA y Ajustes.
- Temas claro y oscuro, texto en negrita y cuatro colores de acento para botones y marcos.
- Gradientes suaves para tarjetas y seis acompañantes originales PNG transparentes 256 × 256.
- Alarma diaria aproximada con permiso de notificaciones, restauración al reinicio.
- Pruebas JUnit del planificador, rangos y asistente.
- GitHub Actions configurado para pruebas y compilación APK de depuración.

- Presupuesto automatizado de modelos offline de hasta 60 MiB; no implica tamaño final de APK ni RAM.
- Pruebas unitarias nuevas para la comprensión local y el aprendizaje descriptivo.

## Investigación del catálogo (no integrada en la app)
- Cinco categorías principales en `CatalogTaxonomy.kt`: tren superior, tren inferior,
  zona central, cardio y movilidad.
- 2.186 registros de metadatos de cuatro fuentes abiertas conservados en staging.
- 1.823 pares categoría/nombre distintos, 1.794 nombres distintos globales.
- 44 grupos léxicos de posibles variantes en cola de revisión manual.
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
- Exportación/restauración segura, migraciones de base de datos y pruebas instrumentadas.
- Validación de rendimiento, accesibilidad y compatibilidad física.
- APK firmada para distribución: la app-debug.apk de CI es solo una compilación de desarrollo.

## Seguridad
- Sin permisos de micrófono, cámara, contactos, ubicación ni Internet en esta primera versión.
- Sin secretos ni credenciales.
- Los puntos y rangos son símbolos del juego, no indicadores de salud.
- Las misiones no penalizan con ejercicios adicionales ni aumentos de carga.
- La app no verifica técnica ni diagnostica lesiones.

## Compilación
El flujo CI usa JDK 17, SDK 36, Gradle 8.13 y AGP 8.13.2 con Kotlin 2.2.21. Incluye `verifyShadowCoreModelBudget`.
Abrir **Actions → Android Debug CI** para ver resultados. Solo un resultado exitoso y
un artefacto presente permitirían afirmar que la APK de prueba se ha compilado.

Versión mínima técnica provisional: Android 8.0 (API 26). Android 15+ será evaluado,
según hardware y disponibilidad del asistente generativo, antes del lanzamiento.
