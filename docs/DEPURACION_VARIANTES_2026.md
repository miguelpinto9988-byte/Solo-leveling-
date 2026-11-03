# Depuración documental de variantes y familias · 2 de noviembre de 2026

**Proyecto:** Solo Leveling Gym Breaker · prototipo Android 0.6.1.

## Objetivo y alcance

Se revisaron **todos los 2.186 registros de investigación** recopilados
previamente en cuatro repositorios y agrupados en **1.823 denominaciones por
categoría**. La revisión automatizada verificó la coincidencia de nombre,
categoría, fuente, grado de dificultad explícito y criterios conservadores de
exclusión técnica. **No es una aprobación clínica ni una evaluación personal
de ejecución de los movimientos.**

La biblioteca de [American Council on Exercise (ACE)](https://www.acefitness.org/resources/everyone/exercise-library/)
permite contrastar ejercicios concretos, así como sus categorías, equipamiento
y niveles. Las guías de la [NSCA sobre entrenamiento de jóvenes](https://www.nsca.com/globalassets/about/position-statements/position_stand_youth_resistance_training---2009.pdf)
refuerzan la necesidad de técnicas adecuadas y supervisión competente:
el asistente no puede sustituir esa revisión.

## Resultado de la depuración

| Categoría | Denominaciones documentadas en APK | Familias propuestas |
| --- | ---: | ---: |
| Tren superior | 125 | 67 |
| Tren inferior | 35 | 23 |
| Zona central | 24 | 16 |
| Cardio | 3 | 2 |
| Movilidad | 11 | 11 |
| **TOTAL** | **198** | **119** |

Los 198 nombres conservados se respaldan con **402 registros originales
rastreables** y se clasifican en **145 variantes nominales iniciales**, **53
intermedias** y **ninguna avanzada con documentación suficiente según los
criterios aplicados**.

De las 119 agrupaciones de movimiento, **20 reúnen más de una denominación**.
Esas agrupaciones todavía son *hipótesis de identidad de movimiento*; el
mismo patrón léxico no basta para afirmar equivalencia biomecánica.

## Qué se retiró

**1.625 denominaciones** no figuran en los recursos de la APK depurada.
Sus motivos se registran en
`catalog/review/verification_registry.json`:

| Motivo de exclusión | Denominaciones |
| --- | ---: |
| Una sola fuente sin respaldo externo documentado | 1.189 |
| Patrón técnico de mayor exigencia o ambiguo | 297 |
| Dificultad original inconsistente o insuficiente | 68 |
| Conflicto entre categorías principales | 58 |
| Nivel discrepante frente a referencia ACE | 2 |
| Ejecución o nombre todavía demasiado ambiguos en segundo cribado | 11 |
| **TOTAL** | **1.625** |

Estas entradas **se eliminaron de los diez archivos de consulta del
directorio `app/src/main/assets`**, pero se conserva la investigación original
en `catalog/staging` y el detalle del descarte en el registro de auditoría.
Ese histórico no se carga en Android y no cuenta como contenido de la APK.

## Referencias técnicas comprobadas

- [ACE — Bodyweight Squat](https://www.acefitness.org/resources/everyone/exercise-library/135/bodyweight-squat/):
  categoría de tren inferior, peso corporal y nivel **Beginner**. Permite
  contrastar la entrada documental *Bodyweight Squat* aunque una segunda
  fuente anterior solo ofrecía una escala numérica.
- [ACE — Crunch](https://www.acefitness.org/resources/everyone/exercise-library/52/crunch/):
  clasifica el movimiento como **Beginner**, con peso corporal. Sirve como
  referencia adicional del nombre documental *Crunches*.
- [ACE — Front Raise](https://www.acefitness.org/resources/everyone/exercise-library/54/front-raise/):
  ejercicio con mancuernas, nivel **Beginner**. Se coteja con *Front
  Dumbbell Raise* sin copiar imágenes ni instrucciones protegidas.
- [ACE — Front Plank](https://www.acefitness.org/resources/everyone/exercise-library/32/front-plank/):
  ACE lo considera **Intermediate**, mientras algunos registros originales
  etiquetaban `Plank`/ `Front Plank` como **Beginner**. Estas fichas se
  descartan de la selección hasta resolver identidad y nivel.

La coincidencia de dos repositorios no demuestra por sí sola independencia:
podrían compartir una fuente upstream. Por eso se registra como
`source_corrob_catalog_only`, **no como verificación profesional**.

## Cómo se integran las variantes por dificultad

El usuario visualiza una **familia propuesta** que contiene variaciones con
nombres diferentes, cada una con nivel **inicial** o **intermedio** y sus
referencias a la(s) fuente(s). Cuando dos fuentes usan el mismo nombre y
nivel, se muestra **una sola variante nominal** con dos referencias, no
dos ejercicios separados.

Si un registro no tiene evidencia clara de nivel, o existe discrepancia, se
retira en vez de asignarle una dificultad inventada. Ninguna variante
avanzada superó el filtro conservador de este lote.

La IA de planificación solo usa el archivo de familias aprobadas
`catalog_approved.json`, que continúa con **cero nuevas aprobaciones
técnicas**. Los 198 nombres depurados se muestran en **Catálogo** como
referencia consultable, no como recetas de entrenamiento.

## Seguridad y trazabilidad

El sistema mantiene limitaciones por equipamiento, experiencia, nivel
seleccionado, exclusiones y molestias. No propone automáticamente variantes
avanzadas ni subidas obligatorias de carga. Los puntos RPG son simbólicos.

La codificación y decodificación de información de Shadow Core AI siguen
funcionando **offline**, con un presupuesto de recursos de **90 MB**
independiente de las mediciones de RAM y tamaño de APK.

### Auditorías

```bash
python3 scripts/audit_catalog.py
python3 scripts/verify_catalog_evidence.py
python3 scripts/validate_research_assets.py
python3 scripts/validate_family_groups.py
python3 scripts/validate_ai_manifest.py
python3 scripts/validate_catalog_reviews.py
python3 -m unittest discover -s scripts/tests -v
```

GitHub Actions también ejecuta pruebas JVM y `assembleDebug`. La compilación
y el comportamiento en dispositivos **no se consideran certificados** hasta
comprobar una ejecución exitosa de CI y pruebas en un móvil.

## Mejoras pendientes

1. **Validación de ejecución por un profesional de ejercicio físico:**
   comprobar técnica y diferencias entre las 20 familias con varios nombres
   antes de convertirlas en relaciones canónicas definitivas.
2. **Revisar las fichas descartadas con nueva evidencia:**
   solo reintroducir una entrada cuando existan referencias sólidas,
   atribución y nivel conciliado; nunca por cumplir una cuota.
3. **Pruebas Android y medición de rendimiento:**
   comprobar la migración Room, RAM, tamaño instalado, latencia de consulta
   y accesibilidad.
