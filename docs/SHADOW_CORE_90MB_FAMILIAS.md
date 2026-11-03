# Shadow Core AI offline y familias de ejercicios — versión 0.6.1

**Estado:** implementación en código fuente; compilación Android y pruebas físicas
pendientes de comprobar.

## Contenido actual de la APK

Se estudiaron originalmente **2.186 entradas de metadatos** con
**1.823 denominaciones nominales por categoría**. Esa investigación permanece
en `catalog/staging`, fuera de los recursos distribuidos en la aplicación.

Tras depurar las fuentes y dificultades, los recursos que carga Android
contienen únicamente **198 denominaciones documentadas**, respaldadas por
**402 referencias originales**, ordenadas en **119 familias propuestas**.
Otras **1.625 denominaciones** están excluidas de la APK y registradas en
`catalog/review/verification_registry.json`.

| Categoría principal | Denominaciones conservadas | Familias propuestas |
| --- | ---: | ---: |
| Tren superior | 125 | 67 |
| Tren inferior | 35 | 23 |
| Zona central | 24 | 16 |
| Cardio | 3 | 2 |
| Movilidad | 11 | 11 |
| **TOTAL** | **198** | **119** |

De ellas, **145** tienen nivel documental inicial y **53** nivel intermedio.
Ninguna variante avanzada quedó acreditada con los filtros de esta depuración.
**20 familias propuestas** reúnen varias denominaciones; esas relaciones todavía
requieren revisión técnica, no deben presentarse como biomecánicamente aprobadas.

El informe de criterios, descartes y referencias ACE está en
[DEPURACION_VARIANTES_2026.md](DEPURACION_VARIANTES_2026.md).

## Acceso a Shadow Core AI

La pestaña IA dispone de un **panel de control** que permite consultar el
asistente local en español, revisar seis etapas de la última decisión,
consultar restricciones y variantes elegidas, inspeccionar el historial
estadístico, codificar y decodificar informes JSON, medir los recursos
empaquetados y cambiar la dificultad solicitada.

Los informes muestran **reglas explícitas y reproducibles**, no pensamientos
ocultos de un modelo generativo. Copiar al portapapeles requiere acción
del usuario. Los datos permanecen locales salvo que el usuario los comparta.

Componentes principales:
- `ShadowCoreAI.kt`: interpretación de intención y parámetros en español.
- `ShadowDecisionAudit.kt` y `ShadowAuditCodec.kt`: proceso auditable
  y codificación/decodificación JSON.
- `ResearchCatalogLoader.kt` y `ResearchFamilyGroupLoader.kt`: lectura
  de índices documentales por categoría, familia, dificultad y fuente.
- `ExerciseVariantSelector.kt`: selección únicamente de variantes
  autorizadas, con filtros por nivel, restricciones y equipo.
- `ApprovedCatalogLoader.kt`: cargador separado que rechaza entradas
  sin aprobación técnica.

**Ninguna de las 198 denominaciones investigadas está aprobada para
planificación automática.** El catálogo aprobado permanece vacío. Las doce
fichas de demostración históricas son independientes.

## Dificultad y adaptación

Cada denominación conserva su nivel confirmado entre las fuentes:
`inicio` o `intermedio`. Si las fuentes discrepaban, se descartó
la entrada en lugar de inventar una dificultad.

El jugador selecciona experiencia y dificultad preferida de forma
independiente. El planificador no puede ignorar exclusiones,
incompatibilidades de equipamiento ni restricciones pendientes. Las
misiones RPG no penalizan el descanso ni fuerzan un aumento físico.

La familia es un **patrón propuesto**. Las variantes documentadas
comparten una ficha y no incrementan el contador de ejercicios
principales. Se conservan las referencias de cada variante.

## Presupuesto de hasta 90 MB

El presupuesto de recursos offline es **90 MB decimales =
90.000.000 bytes**. Incluye archivos locales, índices de catálogo,
metadatos y eventuales modelos que se añadan con autorización.

El núcleo actual es especializado en reglas y estadísticas, **no
contiene un modelo generativo de propósito general**. No se añaden
archivos ficticios para utilizar todo el presupuesto.

Las tareas `verifyShadowCoreModelBudget` y
`verifyOfflineAiResourceBudget` supervisan el tamaño de archivos
empaquetados. Los validadores `verify_catalog_evidence.py`,
`validate_research_assets.py`, `validate_family_groups.py` y
`validate_ai_manifest.py` verifican identidad documental, exclusiones,
índices y contadores. GitHub Actions ejecuta el conjunto configurado
antes de compilar.

**El tamaño real de APK, RAM y latencia no se ha certificado aún.**

## Evidencia y límites

La [biblioteca ACE](https://www.acefitness.org/resources/everyone/exercise-library/)
publica movimientos con equipamiento y dificultad. Se contrastaron
referencias concretas para Bodyweight Squat, Crunch y Front Raise.
ACE marca Front Plank como intermedio, mientras datos históricos lo
marcaban inicial; esa contradicción motivó su exclusión de la APK.

Los repositorios pueden compartir material upstream, por lo que
dos nombres concordantes **no son automáticamente dos fuentes científicas
independientes** ni una autorización profesional de uso.

## Mejoras posteriores (sujetas a revisión)

1. Revisar profesionalmente las 20 familias propuestas con varias
   denominaciones antes de declararlas canónicas.
2. Recuperar entradas excluidas solo cuando nuevas fuentes concilien
   identidad, nivel, equipamiento y condiciones de uso.
3. Verificar CI, migraciones Room, consumo de memoria y latencia en
   dispositivos Android reales.
