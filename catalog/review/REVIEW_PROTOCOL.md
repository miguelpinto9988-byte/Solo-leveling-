# Protocolo de revisión: una ficha principal, múltiples variantes

Los registros de `catalog/staging` son pistas de investigación, **no ejercicios autorizados**.

### Decisión en cuatro etapas

1. **Identidad canónica**: identificar si dos registros describen el mismo patrón funcional. Diferencias de agarre, implemento, ángulo, lateralidad o velocidad suelen ser variantes, pero no se fusionan sin examinar el movimiento.
2. **Clasificación**: elegir exactamente una de cinco categorías principales (Tren superior, Tren inferior, Zona central, Cardio, Movilidad). Conservar otras zonas como metas secundarias, no como duplicados.
3. **Control técnico**: comprobar procedencia/licencia, nivel, equipamiento, restricciones aplicables y que las descripciones no presenten instrucciones inadecuadas. Evitar aprobaciones automáticas de movimientos de alta exigencia, explosivos o que requieran supervisión especializada.
4. **Firma revisora**: documentar responsable humano cualificado, fecha, fuentes y razonamiento. La IA puede proponer grupos, pero no aprobarlos por sí sola.

Un registro que cambie material o agarre **no incrementa** `canonicalExercises`; se incorpora en `variants`.

`catalog/review/approved_exercises.json` es la única entrada para generar `app/src/main/assets/catalog_approved.json`; ambos comienzan vacíos hasta contar con revisión documentada. Las doce fichas existentes de la app son ejemplos independientes y no representan las 2.000 verificadas.

Ejemplos de familias *candidatas a revisar*, no aprobadas:

- Curl de bíceps → variantes barra, mancuernas, banda.
- Sentadilla → variantes barra, peso corporal, postura.
- Plancha → variantes con apoyo y diferentes posiciones.
- Caminata → cinta y exterior pueden ser modalidades del mismo patrón.
- Movilidad de hombro → círculos y deslizamientos NO deben fusionarse automáticamente por compartir articulación.

Cuando hay duda sobre si dos movimientos son biomecánicamente equivalentes, la decisión debe permanecer **pendiente de revisión**.

### Campos mínimos para aprobar

`id`, `localId`, `name`, `category`, `subcategory`, `patternKey`, `equipment`, `level`, `reviewStatus="approved"`, `planningEligibility="approved"`, `reviewedBy`, `reviewerRole="qualified_exercise_reviewer"`, `reviewDate`, `identityRationale`, `riskScreeningNotes`, `restrictionTags`, `sources` y `variants`.

Cada referencia en `sources` y en las variantes debe existir en `catalog/staging`. No se permite asignar una misma referencia a dos familias ni crear dos familias con el mismo `patternKey`. Los registros aprobados se exportan solo tras pasar validación.


## Metadatos obligatorios de cada variante — v0.5.0

Además de `id`, `name`, `equipment` y `sources`, cada variante debe declarar
cuatro atributos independientes:

- `difficulty`: `inicio`, `intermedio`, `avanzado` o `sin_determinar`;
- `difficultyEvidence`: justificación verificable de la clasificación de dificultad;
- `planningEligibility`: `catalog_only` o `approved`;
- `restrictionTags`: lista de restricciones pendientes o revisadas.

La dificultad declarada por un dataset **no es, por sí sola, revisión técnica**.
Para ser `approved`, la realización debe contar con una familia principal
aprobada, evidencia suficiente, ninguna restricción pendiente y dificultad
inicial/intermedia compatible. Una variante avanzada o de nivel desconocido
permanece `catalog_only`.

La interfaz permite elegir la **dificultad deseada** de forma independiente
de la experiencia; Shadow Core AI respeta ambos límites. Una elección mayor
nunca habilita automáticamente contenidos no revisados.

Las fichas públicas de `app/src/main/assets/research_*.json` son solo consulta;
ningún registro de investigación puede emplearse para fabricar aprobaciones.
