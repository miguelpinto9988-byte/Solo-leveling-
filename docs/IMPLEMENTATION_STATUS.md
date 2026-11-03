# Estado de implementación · Solo Leveling Gym Breaker 0.6.1

**Estado general: código fuente actualizado; compilación, CI y prueba en móvil
pendientes de confirmación.**

## Android y experiencia

- Kotlin + interfaces XML, Gradle Kotlin DSL, Android SDK, Room/SQLite,
  ViewModel, Coroutines y Flow.
- Páginas: Sistema, Misiones, Entrenar, Sombras, IA, Ajustes y Catálogo.
- Temas claro y oscuro, con color de acento independiente para bordes,
  marcos y botones.
- Misiones RPG y XP virtual sin penalizaciones físicas.
- Perfil de experiencia, equipo, dificultad elegida y exclusiones.
- Registro de sesiones, percepción de esfuerzo y variantes utilizadas.
- Alertas locales sujetas a permisos y limitaciones del sistema.

## Shadow Core AI offline

- Comprensión especializada de solicitudes frecuentes en español.
- Propuestas revisables y filtros obligatorios de equipo, restricciones
  y nivel, con bloqueo ante molestias o exceso de esfuerzo declarado.
- Historial local y estimación descriptiva con mínimos de muestra.
- Panel de controles, explicación de decisiones y
  codificación/decodificación JSON de informes.
- Presupuesto máximo de **90 MB decimales de recursos offline**;
  no representa RAM ni APK instalada.
- **No contiene un LLM generativo de propósito general.**

## Catálogo depurado (versión 0.6.1)

- Investigación histórica: **2.186 registros** de cuatro repositorios,
  con **1.823 denominaciones** por categoría en `catalog/staging`.
- Depuración de fuente, nivel, clasificación, ambigüedad y contraste ACE:
  **198 denominaciones documentales conservadas** en la APK, respaldadas
  por **402 referencias originales**.
- Organización: **119 familias propuestas**, de las cuales **20**
  agrupan varias denominaciones.
- Niveles conservados: **145 iniciales**, **53 intermedios**,
  **0 avanzados** suficientemente documentados en esta selección.
- Excluidas **1.625 denominaciones** por documentación insuficiente
  o contradicciones. Motivos en
  `catalog/review/verification_registry.json`.
- Las fuentes originales se conservan para auditoría **fuera de la APK**.
- **0 familias nuevas aprobadas para uso automático**. Las 12 fichas
  de demostración son independientes de esta revisión.
- Los ejercicios de investigación no son equivalentes a las
  2.000 familias biomecánicas finales solicitadas.

## Control de calidad configurado

GitHub Actions ejecuta las auditorías de investigación,
`verify_catalog_evidence.py`, `validate_research_assets.py`,
`validate_family_groups.py`, `validate_ai_manifest.py`,
la puerta de aprobación de familias, los tests Python/JUnit,
la verificación de recursos y `assembleDebug`.

**No se ha certificado el resultado del flujo ni la compilación
de la APK:** comprobar GitHub Actions y ejecutar pruebas en Android
antes de distribuir.

## Pendiente

1. Revisión humana de identidad, técnica y restricciones de las
   20 agrupaciones de varios nombres y del resto de fichas.
2. Incorporar solo las fichas realmente aprobadas al catálogo
   activo de Shadow Core AI.
3. Revisar con nueva evidencia los descartes, sin inventar variantes
   ni sumar fuentes duplicadas.
4. Pruebas instrumentadas de migraciones Room, memoria RAM,
   latencia, accesibilidad y compatibilidad Android.
5. Editor completo de rutinas, acompañantes animados e imágenes
   oficiales con autorización de uso.
6. APK firmada y validada para distribución.

**Información:** [depuración documental](DEPURACION_VARIANTES_2026.md),
[catálogo de fuentes](CATALOG_RESEARCH.md),
[IA y límite de recursos](SHADOW_CORE_90MB_FAMILIAS.md).

Los puntos y rangos son elementos de juego. El motor no sustituye
una valoración médica ni un entrenador cualificado.
