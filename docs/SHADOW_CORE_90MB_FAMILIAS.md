# Shadow Core AI · familias por nivel y presupuesto de 90 MB

**Versión de código: 0.6.0. Estado: implementación fuente; compilación Android pendiente de comprobar.**

## Solicitud implementada

1. Ampliar el proceso de investigación de variantes sin contarlas como ejercicios nuevos.
2. Reunir movimientos relacionados como posibles familias, distinguiendo las
   variantes por **Inicial / Intermedio / Avanzado / Sin determinar**.
3. Dar al usuario un panel completo para consultar Shadow Core AI y visualizar
   la explicación reproducible de las reglas utilizadas.
4. Aceptar hasta **90.000.000 bytes (90 MB decimales)** de recursos offline:
   índices, metadatos, vocabulario y eventuales pesos de modelos.

La IA **no aumenta automáticamente** la dificultad física. Las fichas avanzadas
y los datos sin revisión permanecen exclusivamente consultables.

## 1. Grupos y variantes

Se generaron cinco índices de `family_groups_*.json` usando la información
ya recopilada de cuatro repositorios con metadatos abiertos:

| Familia funcional | Registros originales | Nombres distintos en categoría | Agrupaciones propuestas |
|---|---:|---:|---:|
| Tren superior | 1.009 | 789 | 376 |
| Tren inferior | 479 | 417 | 232 |
| Zona central | 400 | 344 | 236 |
| Cardio | 139 | 128 | 109 |
| Movilidad | 159 | 145 | 136 |
| **Total** | **2.186** | **1.823** | **1.089** |

Entre estas agrupaciones, 28 fusionan **provisionalmente** más de una denominación
relacionada por pistas léxicas. La fusión es una **hipótesis de revisión, NO una
identificación biomecánica certificada**.

Para cada agrupación se conservan:
- Nombre(s) original(es) e identificador de la fuente.
- Equipo documentado y dificultad declarada por el proveedor.
- Cubetas de cuatro niveles; etiquetas sin evidencia permanecen
  `sin_determinar`.
- Estado explícito `research_only_unverified` y
  `autoPlanningEligible=false`.
- Aviso sobre fuentes y agrupación provisional.

Las variantes de investigación **no se convierten en `ExerciseVariantEntity`**.
Solo las familias aprobadas por el flujo
`validate_catalog_reviews.py` pasan a Room.

Los 2.186 registros no equivalen a 2.186 ejercicios independientes y tampoco
las 1.089 agrupaciones equivalen a 1.089 familias validadas. Las cuotas de
400 movimientos verificados por cada una de las cinco categorías siguen
pendientes de acreditar.

## 2. Panel completo de Shadow Core AI

En **IA → Abrir panel de control**, el usuario puede:

- Consultar en español al asistente especializado.
- Leer la última decisión y los motivos de sus filtros de forma explícita.
- Ver la clasificación orientativa de su última consulta.
- Codificar su informe de reglas en JSON y copiarlo voluntariamente.
- Decodificar y revisar informes JSON locales, sin ejecutar acciones.
- Inspeccionar el presupuesto, número de registros y recursos incluidos.
- Cambiar la dificultad solicitada y explorar grupos de variantes por nivel.
- Ver los registros originales y limpiar la propuesta sin borrar sesiones.
- Consultar el alcance y las limitaciones del motor.

El informe detalla seis etapas **deterministas y auditables**:
entrada, lectura local, exclusiones, variantes, datos históricos y decisión.
No contiene deliberaciones ocultas de una red neuronal. Los informes son
resúmenes reproducibles de reglas del programa.

La exportación al portapapeles se realiza solo por acción explícita del usuario,
pues el texto puede incluir parámetros personales declarados.

## 3. Inteligencia offline y codificación/decodificación

- `ShadowCoreAI.kt`: reconocimiento local de intenciones; incluye solicitudes
  de catálogo, explicación de la selección y estado del sistema.
- `ShadowDecisionAudit.kt`: razones de cada decisión sin bloquear el
  derecho del usuario a consultar su información.
- `ShadowAuditCodec.kt`: codificación y lectura de informes JSON versión 1,
  con validación de esquema y límites de tamaño.
- `ResearchFamilyGroupLoader.kt`: descodificación JSON de agrupaciones en
  las cinco categorías; rechaza datos marcados como aprobados sin autorización.
- `ShadowResourceMetrics.kt`: lectura de manifiesto local, medición de bytes
  de los recursos y presentación de cifras.
- `ExerciseVariantSelector.kt`: selecciona únicamente variantes validadas
  según nivel solicitado, experiencia, restricciones, equipo e historial.

Los cálculos de catálogo y las consultas estructuradas se ejecutan sin permiso
Android `INTERNET`. No se ha incluido un LLM genérico.

## 4. Presupuesto de 90 MB

- Presupuesto: **90 MB decimales = 90.000.000 bytes**.
- Recursos incluidos actualmente (medidos en los archivos fuente desde
  GitHub, versión 0.6): aproximadamente **1,78 MB** de archivos de catálogo,
  índices, avisos y manifiesto; no representa RAM ni tamaño de la APK.
- No se añaden bytes de relleno para alcanzar el techo.
- Gradle incluye `verifyShadowCoreModelBudget` y
  `verifyOfflineAiResourceBudget`; ambas tareas rechazan recursos que
  excedan el límite.
- GitHub Actions informa además el tamaño del APK `app-debug.apk` cuando
  la compilación sea exitosa. El código DEX, las bibliotecas, el almacenamiento
  dinámico y la RAM requieren medición propia.

Modelos generativos LiteRT-LM ampliamente disponibles actualmente están
por encima de 90 MB; Google documenta tamaños de aproximadamente 289 MB
para FunctionGemma y 586 MB para Qwen3-0.6B. Por eso este motor **no
promete generación libre al nivel de un LLM** y utiliza una arquitectura
especializada de reglas, búsqueda e índices.

Documentación oficial: https://developers.google.com/edge/litert-lm/overview

## 5. Fuentes nuevas investigadas sin redistribuir datos

Además de los repositorios ya incorporados, se estudiaron:

- **Kinetic Exercises Database** (MIT):
  https://github.com/kinetic-place/exercises-db
  Ofrece 899 entradas con niveles y traducciones. Reconoce derivación
  sustancial de `wrkout/exercises.json`, por lo que no se añade su total
  como si fueran 899 ejercicios nuevos o variantes originales.
- **Yoga API** (código MIT):
  https://github.com/alexcumplido/yoga-api
  Define posturas y niveles, pero reutiliza información y recursos visuales
  de terceros con licencias distintas; no se incorporaron imágenes ni
  un lote de posturas sin rastrear la licencia por entrada.
- **OpenExerciseDB**, derivada de wger:
  https://github.com/rfivesix/OpenExerciseDB
  Útil para contraste y referencias, pero con procedencia/atribución
  específica por registro y fuerte solapamiento con otras bases.
- **RepDB**:
  https://github.com/RepDB/exercise-dataset
  Condiciones de atribución y restricciones sobre redistribuir el contenido
  como dataset independiente. No se copiaron sus fichas.

Estas fuentes refuerzan la investigación de sinónimos y dificultad, pero
**no justifican contar ni importar nuevas familias sin revisión de identidad**.

## 6. Control de calidad

Se añadió `scripts/validate_family_groups.py` al flujo CI para asegurar:
- Conservación íntegra de los 2.186 registros y su procedencia.
- Cubetas completas de dificultad y números coherentes.
- Ausencia de aprobación o eligibilidad automática para datos investigados.
- No duplicar identificadores de origen.
- Total de 1.823 nombres y 1.089 propuestas con estados inequívocos.

Se añadieron pruebas Kotlin de consulta de catálogo, explicación de decisiones
y selección bajo restricciones. Los flujos de GitHub Actions deben finalizar
correctamente antes de declarar una APK verificada.

### Próximas mejoras propuestas (no implementadas automáticamente)

1. Revisión por un profesional cualificado de las 28 agrupaciones sugeridas
   y sus variantes. Beneficio: identificar familias verdaderas y evitar
   agrupaciones falsas.
2. Resolver conflictos entre categorías y aprobar fichas con dificultad
   documentada. Beneficio: aumentar los ejercicios elegibles para el motor.
3. Pruebas instrumentadas de Room, consumo de RAM y latencia en Android.
   Beneficio: validar estabilidad, migraciones y rendimiento real.
