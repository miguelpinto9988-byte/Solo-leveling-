# Shadow Core AI · arquitectura offline (revisada para 0.6.1)

## Objetivo de tamaño

**Máximo: 90 MB decimales (90.000.000 bytes) para los recursos offline de IA y catálogo, incluidos los modelos que eventualmente se empaqueten.** Este es un
límite superior, no un tamaño que debamos llenar artificialmente. El motor actual
está escrito principalmente en Kotlin y emplea datos locales, por lo que sus
archivos de modelo pueden ocupar **0 MiB**. La clase de modelo opcional (si se añade
después) debe pasar la tarea `verifyShadowCoreModelBudget`.

El límite de modelos no incluye el tamaño de la APK, Kotlin/ART, Room, imágenes,
base de datos ni memoria RAM. Hasta medir el paquete instalado y PSS en un móvil,
**no se puede afirmar que la IA completa ocupe 90 MB o menos**. El límite es un
objetivo de ingeniería, sujeto a verificación en dispositivo.

## Qué se ha construido

- **NLU especializado en español**: normaliza tildes y mayúsculas, infiere la
  intención por similitud léxica con ejemplos locales y extrae parámetros de
  tiempo, equipo y grupos musculares. Opera sin Internet.
- **Planificador condicionado**: valida disponibilidad, equipo, experiencia,
  grupos excluidos y ejercicios excluidos; nunca omite restricciones declaradas.
  Una intención interpretada por el asistente pasa siempre por este planificador.
- **Control de seguridad**: las consultas que expresan dolor, lesiones,
  molestias o incapacidad no se interpretan como solicitudes de rutina.
- **Aprendizaje incremental estadístico**: consulta las sesiones confirmadas y
  evalúa tendencias descriptivas de esfuerzo con un mínimo de cinco muestras.
  No altera automáticamente las cargas ni diagnostica condiciones médicas.
- **Progreso RPG**: explica experiencia virtual, niveles y misiones a partir del
  historial local; penalizaciones únicamente narrativas y virtuales.
- **Privacidad**: sin permiso Android INTERNET; no descarga modelos ni usa nube.

## Qué NO es

- No es un LLM, no tiene generación libre ilimitada ni comprensión universal.
- No está entrenado con un gran corpus médico/deportivo.
- No estima biomecánica, lesiones, necesidades fisiológicas ni sustitución de
  un profesional. Sus respuestas son orientativas y las rutinas, revisables.
- No se deben denominar “2000 ejercicios validados” las 12 fichas demo actuales.

## Componentes

1. `ai/ShadowCoreAI.kt`: procesamiento offline de solicitudes.
2. `ai/ShadowLearning.kt`: tendencias descriptivas.
3. `domain/OfflineTrainingIntelligence.kt`: reglas, filtros y decisiones.
4. `TrainingViewModel.kt`: orquestación del estado local y respuestas.
5. `data/TrainingDatabase.kt`: Room con perfiles y sesiones.

## Pruebas

`ShadowCoreAITest` y `ShadowLearningTest` cubren intención, parámetros,
tildes, fatiga, precauciones y filtrado. Las pruebas de regresión del planificador
existentes siguen incluidas. El flujo Android CI ejecuta las pruebas de JVM y
la compilación debug además del presupuesto de modelos.

**Limitación de validación:** la implementación del código fuente no certifica
la compilación. Revisar la ejecución exitosa de GitHub Actions y probar la APK
en un teléfono para verificar inferencia y consumo.

La versión 0.6 añade acceso a la explicación de reglas, codificación y
descodificación JSON, métricas de recursos y 1.089 agrupaciones de familias
propuestas con niveles. Consulta [SHADOW_CORE_90MB_FAMILIAS.md](SHADOW_CORE_90MB_FAMILIAS.md)
para la arquitectura vigente; la IA no incluye un LLM generativo.

## Próximos pasos sugeridos

1. Probar intenciones con expresiones reales en español y medir errores.
2. Crear y revisar fichas completas de ejercicios y relaciones de exclusión.
3. Medir memoria RAM, latencia y tamaño APK en varios dispositivos.
4. Extender el aprendizaje estadístico con calibración y evaluación de datos.
5. Añadir, por separado y solo si está justificado, un pequeño modelo
   cuantizado real sujeto al presupuesto global de 90 MB y una licencia compatible.
6. Implementar una pantalla de revisión de planes antes de registrar sesiones.
