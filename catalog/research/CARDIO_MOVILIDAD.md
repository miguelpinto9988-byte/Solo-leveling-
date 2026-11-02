# Investigación dirigida: Cardio y Movilidad

**Objetivo:** encontrar ejercicios principales realmente distintos para las cuotas
de 400 en cada categoría, sin convertir variantes, equipamiento o intensidades
en ejercicios independientes.

## Estado reproducible

- **Cardio:** 139 registros de origen; **128 denominaciones normalizadas**.
- **Movilidad:** 159 registros de origen; **145 denominaciones normalizadas**.
- Cola priorizada de **273 pares nombre/categoría**, procedentes de cuatro
  fuentes abiertas previamente auditadas.
- **24 denominaciones** aparecen en varias fuentes dentro de su categoría.
- Los mínimos nominales que siguen faltando son **272 en cardio** y **255 en movilidad**.
- Los déficits reales serán mayores al resolver movimientos equivalentes.
- No se han autorizado nuevas fichas; `autoPlanningEligible` permanece falso.

Cola ejecutable de revisión:
`catalog/review/cardio_mobility_priority.json`.

## Investigación adicional de fuentes

| Fuente | Información útil | Licencia / límite | Decisión |
| --- | --- | --- | --- |
| [wger](https://wger.de/) | Movimientos, músculos, equipos y vínculos de variantes (`variation_group`) | Contenido por ejercicio con atribución y, según registro, CC BY-SA; respetar autores y share-alike | **Referencia de identidad** hasta disponer de exportaciones con atribución verificable |
| [RepDB free tier](https://github.com/RepDB/exercise-dataset) | Ejercicios de fuerza, movilidad y cardio con información en español | Uso dentro de aplicaciones con enlace de atribución; **prohibido redistribuir como dataset** y usar imágenes para derivación generativa | **No copiar el dataset a este repositorio público** |
| [2024 Compendium of Physical Activities](https://pacompendium.com/) | Nombres de actividades y clasificación energética | Catálogo de **actividades**, frecuentemente intensidades o contextos, no familias de ejercicios. Versión de adultos, no aplicable automáticamente a adolescentes | **Solo referencia metodológica**, no generar registros ni pautas |
| [Yoga API](https://github.com/alexcumplido/yoga-api) | Nombres de posturas y categorías para ampliar investigación de movilidad | Código/API MIT, pero imágenes y algunos conjuntos de poses provienen de terceros con licencias diferentes | **No importar recursos ni nombres en bloque sin verificar procedencia** |
| [smyrdev exercises-dataset](https://github.com/smyrdev/exercises-dataset) | Jerarquía de movimiento y 37 perfiles de volumen para 1.115 de 1.324 registros | MIT para datos; mucho solapamiento con datos existentes. Solo 34 registros de cardio y 69 de estiramientos | **Referencia para detectar variantes**, no sumar cifras |

Fuentes externas de carácter educativo no se consideran automáticamente
una autorización para distribución comercial ni una certificación médica.

## Reglas de clasificación e identidad

1. **Cardio:** identificar el patrón predominante (locomoción, pedaleo,
   desplazamiento, coordinación, actividad aeróbica). Distintas superficies,
   velocidad, equipo o modalidad de intervalos **no** son ejercicios nuevos
   por sí solos. Una actividad deportiva completa puede no ser una ficha
   de ejercicio apta para rutinas automáticas.
2. **Movilidad:** diferenciar el movimiento articular, postura y función.
   Un estiramiento de una región anatómica distinta puede ser un movimiento
   distinto, pero no toda lateralidad, ángulo o ayuda de equipo genera una
   familia nueva. Priorizar descripciones y procedencia comprobables.
3. **Zona central:** verificar si la entrada es estabilidad, rotación,
   flexión o control del tronco. Solo se considera independiente si
   el patrón de movimiento es distinto y está documentado.
4. Las funciones simultáneas (por ejemplo, coordinación y cardio) se
   etiquetan como objetivos secundarios; cada ejercicio tiene UNA
   categoría principal sin repetirse en el contador.
5. La clasificación no diagnostica riesgos físicos. Los ejercicios
   técnicamente exigentes no se habilitan de forma automática.

## Siguiente trabajo editorial y técnico

- Resolver los **29 nombres con conflicto de categoría** documentados en
  `catalog/review/category_conflicts.json`.
- Revisar las **44 agrupaciones sugeridas** con un profesional cualificado
  y documentar decisiones en el protocolo de revisión.
- Contrastar las 273 denominaciones de cardio y movilidad con sus
  referencias originales, variantes y patrones biomecánicos.
- Si las fuentes no acreditan 400 patrones distintos por categoría,
  informar la cifra real. **No inventar ni replicar registros.**
- Importar únicamente fichas que superen `validate_catalog_reviews.py`,
  con documentación y versión de revisión humana.

**Advertencia:** un número elevado de nombres no implica eficacia, conveniencia
ni seguridad de entrenamiento. Los contenidos deben verificarse antes de usarse
en el motor offline de Shadow Core AI.
