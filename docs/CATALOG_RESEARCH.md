# Investigación y auditoría del catálogo: objetivo 2.000 ejercicios principales

**Estado: investigación preliminar, no catálogo final ni validado.**

## Referencia de clasificación

El documento aportado `Base_Teorica_Integral_Sistema_Entrenamiento.pdf`
(secciones 3 y 6) distingue cinco familias funcionales de entrenamiento:
**tren superior, tren inferior, zona central, cardio y movilidad**.
Descanso es una actividad de planificación, no un ejercicio contable del catálogo.
Las subcategorías musculares no son categorías principales adicionales.

**Objetivo solicitado: 2.000 ejercicios biomecánicamente distintos (400 por categoría);
variantes de implemento, agarre, amplitud, lado, inclinación o técnica dentro de un
mismo patrón no incrementan el contador principal.**

## Fuentes examinadas y material efectivamente recopilado

1. `yuhonas/free-exercise-db` — fuente pública declarada como Unlicense /
   dominio público, `dist/exercises.json`: **876 registros originales**.
   URL: https://github.com/yuhonas/free-exercise-db
   **721 registros** se conservan en staging, tras excluir **155 entradas de
   modalidades técnicas, explosivas o de exigencia elevada** del primer lote.
2. `Glowupp-app/open-exercisedb` — repositorio MIT, `exercises.json`:
   **322 registros originales**. Se conservaron **275** con categoría
   asignable y dificultad de origen inferior a 8; 47 se excluyeron por
   clasificación insuficiente o dificultad alta.
   URL: https://github.com/Glowupp-app/open-exercisedb

Se han localizado y revisado también fuentes como OpenExerciseDB, LiftFlow,
wger, ACE y ExerciseDB. Sus cifras no se suman, pues algunas derivan de
otras bases, incluyen variantes o tienen condiciones de reutilización
diferentes. Ningún material multimedia de estas fuentes ha sido incorporado.

## Estadísticas verificadas del staging

| Categoría | Entradas de fuentes (SIN deduplicación semántica) | Objetivo | Déficit mínimo de entradas |
| --- | ---: | ---: | ---: |
| Tren superior | 521 | 400 | 0 |
| Tren inferior | 176 | 400 | 224 |
| Zona central | 146 | 400 | 254 |
| Cardio | 26 | 400 | 374 |
| Movilidad | 127 | 400 | 273 |
| **TOTAL** | **996** | **2.000** | **al menos 1.125 por categoría** |

De las 996 entradas se detectaron **10 nombres duplicados tras normalizar
mayúsculas, acentos y caracteres**, por lo que existen **986 denominaciones
normalizadas diferentes**, antes de identificar nombres equivalentes,
sinónimos, variantes y movimientos duplicados.

**Ejercicios principales distintos y revisados de las fuentes: aún no
determinados.** No se asigna un número definitivo hasta revisión técnica.
La diferencia global bruta es de 1.004 entradas, pero la distribución
igualitaria exige al menos 1.125 entradas adicionales en categorías deficitarias,
aun antes de agrupar variantes. El déficit real por categoría será mayor o igual al calculado antes de
deduplicar y puede ser considerablemente mayor.

## Definición operacional del ejercicio y las variantes

Una ficha principal corresponde a un **patrón funcional de movimiento
identificable**, no a su combinación de equipo, mano, dificultad o carga.
Las variantes se almacenan como registros hijos con su propio equipo y
precauciones. Cambios sustanciales del patrón o de su función pueden
justificar fichas independientes si la revisión técnica lo sustenta.

Ejemplos de decisiones a revisar (no agrupaciones certificadas):
- Distintos implementos del curl de bíceps: posible familia común.
- Variantes de banco o agarre del press de banca: posible familia común.
- Movimientos de carrera y aparatos de marcha: no se distinguen solo por
  velocidad o marca del equipo; revisar patrón funcional.
- Un movimiento de movilidad para un grupo corporal diferente puede
  requerir una ficha propia; no combinarlo solo por compartir la palabra
  "estiramiento".

El campo `variationFamilyHint` indica una **pista de revisión**, nunca la
identidad confirmada del ejercicio. Deben revisarse descripciones
biomecánicas, grupo principal, movimiento articular y contexto de uso.
Si es dudoso, el registro permanece pendiente.

## Estructura para Android

1. `catalog/staging/free_exercise_db_research.json` y
   `catalog/staging/glowupp_research.json` contienen registros fuente
   recuperados para investigación.
2. `scripts/audit_catalog.py` verifica fuentes, categorías, IDs y
   restricciones de importación.
3. `CatalogTaxonomy.kt` define las cinco categorías del producto.
4. **Todos los registros están en estado `research_only_pending_identity_review`.**
   El campo `autoPlanningEligible` es `false`. Ninguno se incorpora
   directamente a la base Room ni puede ser elegido por Shadow Core AI.
5. Las doce fichas de demostración que ya existen en la app son independientes
   de este catálogo investigado.

## Criterios de promoción futura a la app

Para autorizar un ejercicio principal se requiere:
- Licencia y procedencia verificables;
- Identidad de movimiento diferenciada de otras fichas principales;
- Categoría principal única y variantes agrupadas;
- Metadatos de equipamiento y nivel consistentes;
- Revisión técnica y de condiciones de uso;
- Filtros de exclusión y restricciones aplicables;
- Tratamiento prudente de ejercicios avanzados: no se habilitan de forma
  automática sin los controles de elegibilidad necesarios.

## Decisión sobre el objetivo 2.000 / 400 por categoría

Las fuentes analizadas **no prueban que existan 400 movimientos únicos
pertinentes en cada una de las cinco categorías**. Cardio y movilidad
presentan el mayor déficit y muchas bases contabilizan aparatos o variantes
como entradas nuevas. No sería íntegro completar la cuota inventando
ejercicios, duplicando variantes o renombrando un mismo movimiento.

**Recomendación:** conservar la taxonomía de cinco categorías y priorizar
el número real de movimientos verificables por categoría, sin exigir
exactamente 400 cuando el material no lo soporte. Si se mantiene la cuota,
solo se sumarán nuevos ejercicios principales con identidad verificada.

## Reproducción de la auditoría

```bash
python3 scripts/audit_catalog.py
```

El flujo Android CI ejecuta este control antes de las pruebas y compilación.
