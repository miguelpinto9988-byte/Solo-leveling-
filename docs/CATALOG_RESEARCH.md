# Investigación del catálogo: 2.000 ejercicios principales, sin variantes duplicadas

> **Estado vigente de la APK (0.6.1):** después de analizar los 2.186
> registros de investigación originales, se conservaron **198 denominaciones
> documentales** (402 referencias) en **119 familias propuestas**. Las
> otras **1.625 denominaciones** salieron de los recursos Android. Los datos
> de las tablas históricas de este informe permanecen en `catalog/staging`
> y **ya no representan el catálogo visible de la APK**.
>
> Consulta [la auditoría final y sus motivos de exclusión](DEPURACION_VARIANTES_2026.md).
> La aprobación profesional del catálogo de entrenamiento sigue pendiente.

**Resultado actual: 2.186 entradas de metadatos recopiladas; objetivo de 2.000 movimientos independientes NO demostrado.**

## Taxonomía fija del proyecto

La aplicación usa cinco categorías principales de ejercicios, con cuota objetivo de 400 por grupo:

1. Tren superior
2. Tren inferior
3. Zona central
4. Cardio
5. Movilidad

Los músculos concretos son subcategorías y el descanso es una categoría del calendario,
no un ejercicio contable.

## Datos efectivamente recopilados en GitHub

| Origen | Entradas originales | Entradas en investigación | Licencia | Notas |
| --- | ---: | ---: | --- | --- |
| [Free Exercise DB](https://github.com/yuhonas/free-exercise-db) | 876 | 721 | The Unlicense / dominio público | Excluidas 155 entradas de modalidades técnicas o de alta exigencia |
| [Glowupp open-exercisedb](https://github.com/Glowupp-app/open-exercisedb) | 322 | 275 | MIT | Excluidas 47 entradas por nivel o clasificación insuficiente |
| [rthepen workout-database](https://github.com/rthepen/workout-database) | 1.015 | 883 | MIT | 132 fuera del lote por nivel, exigencia o clasificación; 31 archivos por equipo |
| [buzops9 exercisedb](https://github.com/buzops9/exercisedb) | 317 | 307 | MIT | 10 entradas fuera del lote por nivel o exigencia |
| **TOTAL** | **2.530** | **2.186** | Mixtas | Fuentes independientes, con solapamiento de nombres y variantes |

En los archivos de staging se conservaron únicamente identificadores y metadatos necesarios para la
investigación. No se copiaron vídeos, ilustraciones ni instrucciones de terceros.

## Cuotas pendientes y solapamientos

Los conteos siguientes son **nombres**, no ejercicios biomecánicamente diferentes.

| Categoría | Registros sin deduplicar | Nombres distintos dentro de la categoría | Meta | Déficit mínimo de nombres |
| --- | ---: | ---: | ---: | ---: |
| Tren superior | 1.009 | 789 | 400 | 0 |
| Tren inferior | 479 | 417 | 400 | 0 |
| Zona central | 400 | 344 | 400 | 56 |
| Cardio | 139 | 128 | 400 | 272 |
| Movilidad | 159 | 145 | 400 | 255 |
| **TOTAL** | **2.186** | **1.823 pares categoría/nombre** | **2.000** | **583 por cuota** |

Además:
- **1.794 nombres normalizados globales**, frente a los 1.823 pares categoría/nombre;
- **363 duplicaciones textuales** dentro de una categoría;
- **29 nombres** se clasificaron en dos categorías distintas según la fuente;
- En caso de conflicto de clasificación, un ejercicio principal debe tener UNA categoría
  principal consistente, con objetivos secundarios en campos aparte;
- La deduplicación por patrón de movimiento (equipamiento, agarre, ángulo, postura)
  **no está concluida**, así que 1.794 tampoco es un contador final de movimientos únicos.

El déficit mínimo real probablemente es **mayor** que los 583 nombres nominales:
muchos de los nombres todavía diferentes se agruparán como variantes.

### Auditoría de familias candidatas

`catalog/review/variant_family_suggestions.json` propone **44 grupos léxicos** que requieren
revisión individual antes de agrupar. No autoriza ninguna fusión automática:
los movimientos y sus restricciones pueden diferir incluso con nombres similares.

Por ejemplo, más de cien nombres de curls pueden corresponder a numerosas configuraciones
de equipamiento y agarre, no necesariamente a ejercicios principales diferentes.
Esta distinción es imprescindible para respetar el requisito del usuario.

## Otras fuentes evaluadas, NO añadidas a staging

- [smyrdev/exercises-dataset](https://github.com/smyrdev/exercises-dataset):
  publica 1.324 registros; su jerarquía distingue 37 perfiles de movimiento para
  1.115 entradas de entrenamiento de resistencia. Ilustra la abundancia de variantes.
  Hay 34 registros de cardio y 69 de estiramiento. No se incorporó el conjunto,
  entre otras razones por su considerable solapamiento con fuentes ya recopiladas.
- [RepDB/exercise-dataset](https://github.com/RepDB/exercise-dataset):
  publica 701 entradas en su edición gratuita con atribución obligatoria,
  pero prohíbe redistribuir el contenido como un dataset de terceros.
  **No se ha copiado a este repositorio.**
- [OpenExerciseDB](https://github.com/rfivesix/OpenExerciseDB) y
  [wger](https://wger.de/): datos con licencias Creative Commons por entrada;
  requieren control de autores, términos y relaciones de variantes.
- [ACE Exercise Library](https://www.acefitness.org/resources/everyone/exercise-library/):
  adecuada como fuente de referencia técnica; su contenido no se ha redistribuido.

No se sumarán cifras publicitadas por repositorios que reutilizan sustancialmente las
mismas fuentes, ni se contarán modalidades y máquinas como movimientos independientes
si el patrón sigue siendo equivalente.

## Estructura de trabajo para revisión

- `catalog/staging/*.json`: datos de procedencia por fuente;
- `catalog/review/variant_family_suggestions.json`: agrupaciones a revisar;
- `scripts/audit_catalog.py`: conteo reproducible, categorías, procedencia y controles;
- `app/.../CatalogTaxonomy.kt`: cinco categorías usadas por la app;
- `catalog/SOURCE_LICENSES.md`: avisos obligatorios de autoría y licencias.

Todos los registros de investigación conservan:
`status = "research_only_pending_identity_review"` y
`autoPlanningEligible = false`.

Por tanto, **Shadow Core AI sigue utilizando únicamente las fichas de demostración previamente
disponibles**: el material recuperado no se inserta en Room ni se usa en planes automáticos.

## Criterios para aceptar un ejercicio principal

1. Procedencia y licencia examinadas.
2. Nombre canónico y descripción funcional precisos.
3. Patrón biomecánico diferenciado, no solo variación de agarre/equipo.
4. Una categoría principal; músculos secundarios y variantes como relaciones.
5. Compatibilidad por experiencia/equipo/restricciones revisada.
6. Ejercicios de riesgo o exigencia técnica no se habilitan automáticamente.
7. Confirmación técnica de que no duplica otras fichas.

## Conclusión y recomendación

Con los datos revisados, **no se puede certificar la existencia de 2.000 ejercicios principales
diferentes distribuidos exactamente 400/400/400/400/400**. Particularmente cardio y movilidad
no ofrecen ese número en estas fuentes sin contar variantes.

No se inventarán nombres, se clonarán ejercicios ni se alterarán categorías para completar cuotas.
Recomiendo conservar las cinco categorías pero permitir distribución desigual de movimientos
realmente distintos y representar las variantes por separado. Cualquier cambio del objetivo
numérico requiere aprobación del usuario.

Auditoría:

```bash
python3 scripts/audit_catalog.py
```


## Implementación de las tres recomendaciones — versión 0.4.0

### 1. Revisión de identidad y variantes (implementada como proceso controlado)

- Se recopilaron **44 grupos léxicos provisionales** para revisión y **29 conflictos
  de categoría** documentados en `catalog/review/category_conflicts.json`.
- Se registraron **10 casos editoriales concretos** en
  `catalog/review/manual_screening_examples.json`, con referencia al ID
  original y advertencias de falsos positivos o diferencias funcionales.
- Ningún grupo provisional está homologado como familia biomecánica definitiva.
  La firma de un profesional cualificado sigue siendo necesaria.
- `CatalogReviewGate` exige identificadores únicos, patrón canónico único
  entre las cinco categorías y variantes subordinadas.
- `exercise_variants` es una tabla Room vinculada por clave foránea
  `exerciseId` al ejercicio principal. La migración 2→3 conserva la
  estructura de las bases existentes. Las variantes nunca se cuentan como
  ejercicios adicionales.

### 2. Investigación focalizada en cardio y movilidad (realizada)

- `catalog/review/cardio_mobility_priority.json`: cola de **273 nombres
  documentales** entre cardio (128) y movilidad (145), con procedencia y
  posibles coincidencias entre fuentes.
- `catalog/research/CARDIO_MOVILIDAD.md`: fuentes adicionales, diferencias
  entre actividades y ejercicios, restricciones de licencias y estrategia
  para no inventar entradas.
- Se mantuvo el número real de registros investigados: **2.186**.
  Las referencias nuevas no se añadieron como registros cuando sus licencias
  o identidades no estaban listas para importación.

### 3. Validación obligatoria antes de la IA (implementada)

- `catalog/review/approved_exercises.json` es la lista editorial autorizada,
  actualmente **vacía (0 familias con aprobación documentada)**.
- `scripts/validate_catalog_reviews.py` comprueba procedencia, revisor,
  razones de identidad, clasificaciones, atributos, patrones repetidos,
  incompatibilidades y variantes; `--write` genera el asset sólo si pasa.
- `app/src/main/assets/catalog_approved.json` es el único origen de
  nuevas familias de la app. No contiene registros pendientes.
- `ApprovedCatalogLoader.kt` valida el asset durante la carga y descarta
  completamente cualquier lote inválido; nunca lee `catalog/staging`.
- `Shadow Core AI` continúa usando sólo las fichas previamente disponibles
  y, en futuras versiones, fichas firmadas que sean explícitamente elegibles.
  Las fichas técnicas avanzadas o con etiquetas de precaución quedan
  consultables, no seleccionables automáticamente.
- `scripts/audit_review_queues.py` y las pruebas del gate se incorporaron a
  GitHub Actions. La compilación Android no está certificada hasta comprobar
  que el flujo se ha ejecutado con éxito.

### Cómo añadir una ficha en el futuro

1. Resolver su identidad con las variantes y la categoría principal.
2. Registrar la revisión técnica y la trazabilidad en
   `catalog/review/approved_exercises.json`.
3. Ejecutar `python3 scripts/validate_catalog_reviews.py --write`.
4. Ejecutar `python3 scripts/audit_catalog.py`,
   `python3 scripts/audit_review_queues.py`,
   `python3 -m unittest discover -s scripts/tests -v` y la
   compilación/test de Android antes de distribuir.
5. Registrar cuál fue la revisión y verificar funcionamiento en un móvil real.

**La cifra de 2.000/400 por categoría sigue como objetivo pendiente**.
El catálogo activo no se ha inflado con variantes ni registros sin revisar.


## Extensión 0.5.0: variantes dentro de la app y selección por dificultad

- Los cinco archivos `app/src/main/assets/research_*.json` contienen los
  2.186 registros de investigación agrupados por **nombre normalizado
  dentro de cada categoría**. Por eso hay 1.823 grupos nominales, **no**
  1.823 familias biomecánicas certificadas.
- La interfaz de Android permite buscar, filtrar por nivel declarado y consultar
  nombres relacionados como **posibles variantes aún no verificadas**.
- Los metadatos de cada fuente conservan su dificultad textual original;
  cuando no hay nivel explícito, se muestra `sin_determinar`.
- La selección del jugador (inicial, intermedio, avanzado) se almacena separada
  de su experiencia y está limitada por controles de elegibilidad.
- El planificador usa únicamente familias aprobadas y variantes con dificultad
  revisada. Las variantes no aumentan el contador principal.
- Se almacena la variante efectivamente elegida en cada sesión, para alternar
  entre realizaciones **del mismo nivel** cuando hay historial suficiente.
- Las variantes avanzadas y las no revisadas quedan fuera de planificación.
- Documentación: [VARIANTES_Y_DIFICULTAD.md](VARIANTES_Y_DIFICULTAD.md).

**No se han aprobado nuevas familias mediante inferencia o agrupación automática.**


## Nueva capa de agrupaciones propuestas por nivel — 0.6.0

Los datos de investigación se organizaron en cinco archivos de lectura local
`family_groups_*.json` mediante las pistas de parentesco que ya se
documentaban en el catálogo. Se conservan **2.186 registros de procedencia**,
**1.823 nombres por categoría** y se generan **1.089 agrupaciones
provisionales**. De ellas, **28** contienen más de una denominación
relacionada de manera léxica y requieren revisión específica. No se trata
de 1.089 ejercicios biomecánicos verificados.

Cada agrupación incluye variantes por nivel inicial, intermedio,
avanzado y sin determinar, equipo informado, identificador de fuente,
estado de verificación y señal de exclusión para planificación.
Ningún registro de investigación puede saltar la puerta de aprobación.

Fuentes adicionales investigadas: Kinetic Exercises Database (MIT,
derivada sustancialmente de catálogos abiertos previos), Yoga API
(código MIT pero con recursos y datos de terceros), OpenExerciseDB
(fork del proyecto wger con atribuciones por entrada) y RepDB
(condiciones específicas de redistribución y atribución). Se evitaron
importaciones automáticas por solapamiento o derechos sin aclarar.
Los detalles constan en [SHADOW_CORE_90MB_FAMILIAS.md](SHADOW_CORE_90MB_FAMILIAS.md).

La tarea `scripts/validate_family_groups.py` verifica los grupos,
las fuentes, la asignación de niveles sin validar y la prohibición
de habilitar automáticamente las variantes. El objetivo original de
2.000 movimientos principales diferentes permanece pendiente.
