# Filtro estricto de variantes con evidencia externa — 2 de noviembre de 2026

## Objetivo

Evitar que las coincidencias entre dos repositorios potencialmente derivados de una misma fuente se presenten como validaciones técnicas independientes, manteniendo los niveles de dificultad de origen y la separación entre **consulta documental** y **autorización de entrenamiento**.

## Resultado y trazabilidad

- Investigación histórica: 2.186 registros, 1.823 denominaciones.
- Primer cribado: 198 nombres con metadatos concordantes; 1.625 nombres descartados.
- Segundo filtro: **3 nombres** con ficha ACE pública que identifica el ejercicio y documenta un nivel congruente, **6 referencias originales**, **3 familias propuestas**, todas de nivel `inicio`.
- Retiradas adicionales de los assets: **195 denominaciones** sin referencia técnica externa independiente registrada.
- Total fuera de los assets: **1.820 denominaciones**; todas conservadas en el registro de decisiones o staging para auditoría.
- **0 familias aprobadas** para que la IA genere rutinas. ACE valida el registro documental citado, pero **no certifica** la ejecución de los usuarios ni el agrupamiento canónico de la aplicación.

| Nombre retenido | Categoría | Nivel | Referencia ACE |
| --- | --- | --- | --- |
| Bodyweight Squat | Tren inferior | `inicio` | https://www.acefitness.org/resources/everyone/exercise-library/135/bodyweight-squat/ |
| Crunches (ACE: Crunch) | Zona central | `inicio` | https://www.acefitness.org/resources/everyone/exercise-library/52/crunch/ |
| Front Dumbbell Raise (ACE: Front Raise) | Tren superior | `inicio` | https://www.acefitness.org/resources/everyone/exercise-library/54/front-raise/ |

Verificaciones puntuales: ACE identifica en estas páginas equipo y nivel *Beginner*. Las correspondencias *Crunches/Crunch* y *Front Dumbbell Raise/Front Raise* son equivalencias nominales conservadoras para consulta, no pruebas biomecánicas de variantes alternativas. No se importan descripciones ni imágenes protegidas de ACE.

**Conflicto sin resolver:** ACE clasifica *Front Plank* como *Intermediate* (https://www.acefitness.org/resources/everyone/exercise-library/32/front-plank/), en desacuerdo con algunos catálogos que indican *Beginner*. No se reincorpora esta ficha sin revisión.

## Política de integración en familias

1. Retener en los assets Android solo una denominación incluida en `verification_registry.json` con `externalEvidence` ACE congruente y trazable.
2. Mantener el movimiento original como `name` en cada registro, y `source`/`sourceId` como procedencia separada. Un mismo movimiento con dos fuentes es **una variante nominal**, no dos ejercicios.
3. Crear exclusivamente familias de consulta, sin afirmar relaciones biomecánicas aprobadas. Las variantes están agrupadas bajo la familia y **no se contabilizan como ejercicios independientes**.
4. Conservar los campos `difficultyVerified=false`, `autoPlanningEligible=false` y `verifiedFamilies=0`; la revisión documental no cambia esas señales.
5. Mantener `catalog_approved.json` vacío. Solo una revisión técnica humana documentada en `catalog/review/approved_exercises.json` puede permitir futuras fichas activas; el validador de aprobación permanece cerrado para datos sin firma.
6. Respetar el nivel de dificultad como límite, con prioridad para restricciones, equipamiento y descanso. Los rangos del juego no fuerzan una progresión física.

## Alcance de la eliminación

Se excluyeron **de la aplicación distribuida**, no del historial de GitHub, 195 nombres del primer cribado cuya única evidencia era la coincidencia entre catálogos. No se afirma que esos movimientos sean físicamente imposibles. Se pueden reexaminar al aportar fichas técnicas específicas, atributos de equipo compatibles, dificultad conciliada y revisión profesional de identidad y restricciones.

## Comprobación

Los validadores de assets comprueban que ningún nombre sin `externalEvidence` aparezca en Android, que los nombres de variantes correspondan a su denominación nominal, que todas las referencias originales sean trazables y que el motor no marque ninguna de estas fichas como apta para planificación. El manifiesto offline debe contabilizar 3 denominaciones, 3 familias, 6 referencias y 1.820 nombres fuera de los assets.

La prueba de compilación y en dispositivo es independiente de esta revisión documental; no se considera certificada por el mero hecho de realizar estos cambios.
