# Investigación del catálogo: 2.000 ejercicios principales, sin variantes duplicadas

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
