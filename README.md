# Solo Leveling Gym Breaker

Aplicación Android nativa en **Kotlin + XML**, Room/SQLite, Coroutines, MVVM y Shadow Core AI offline. Estado: **prototipo**; la compilación de esta rama y la ejecución en un dispositivo Android requieren comprobación.

## Catálogo distribuido: filtro de evidencia externa

La investigación histórica de `catalog/staging` reúne **2.186 registros** y **1.823 denominaciones por categoría**. Una primera depuración conservó **198 nombres con concordancia entre catálogos** y descartó **1.625**. Sin embargo, concordancia entre repositorios no es necesariamente corroboración editorial independiente.

En esta revisión, la **APK únicamente muestra tres denominaciones respaldadas también por referencias técnicas públicas de ACE**, seis registros originales y tres familias *propuestas*:

| Categoría | Denominación consultable | Dificultad documental |
| --- | --- | --- |
| Tren superior | Front Dumbbell Raise | Inicial |
| Tren inferior | Bodyweight Squat | Inicial |
| Zona central | Crunches | Inicial |
| Cardio | Sin entradas que superen el filtro | — |
| Movilidad | Sin entradas que superen el filtro | — |

Las otras **195 denominaciones** previamente conservadas se retiraron de los assets Android por no registrar una referencia técnica externa independiente. Sumadas a las 1.625 ya excluidas, son **1.820 denominaciones fuera de la APK**. Se mantienen la investigación y los descartes en GitHub para auditoría; «retirado de la aplicación» no significa que el ejercicio sea imposible, sino que **no hay evidencia suficiente según este filtro**.

Los nombres de variantes en los assets muestran el **nombre del movimiento**, no el nombre del repositorio que aportó el dato. Las variantes están subordinadas a la familia; **no incrementan el recuento de ejercicios principales**.

**Cero familias nuevas están aprobadas para entrenamiento automático.** La coincidencia con una ficha ACE confirma documentación pública del movimiento y de su clasificación; no supone una evaluación de seguridad individual, aprobación profesional del programa ni equivalencia de todas las variantes biomecánicas. Los 12 ejercicios de demostración anteriores permanecen separados del catálogo investigado.

## Dificultad y seguridad

La consulta expone solo el nivel **inicial** documentado de esas tres denominaciones. No se han acreditado entradas intermedias ni avanzadas con este nuevo filtro. El nivel preferido del jugador es un **techo**, no una obligación de aumentar la carga ni la frecuencia; las restricciones y molestias mantienen prioridad y la progresión RPG es simbólica.

`ExerciseVariantSelector.kt` solo admite variantes cuya aprobación individual está registrada, y `ApprovedCatalogLoader.kt` lee exclusivamente `catalog_approved.json`, que sigue vacío. No se convierte material de investigación en rutinas por automatización.

## Shadow Core AI offline

El motor local interpreta solicitudes habituales en español, consulta el progreso, aplica filtros y genera informes JSON sin conexión. **No es un LLM de propósito general.** El presupuesto configurado es de **90 MB decimales de recursos offline**, no de RAM ni del tamaño instalado de la APK.

## Evidencia y control de calidad

- [Filtro estricto y referencias oficiales](docs/FILTRO_ACE_2026.md).
- [Registro completo de la depuración anterior](catalog/review/verification_registry.json).
- [Protocolo de revisión profesional](catalog/review/REVIEW_PROTOCOL.md).
- [Licencias y procedencia](catalog/SOURCE_LICENSES.md).
- [GitHub Actions](.github/workflows/android.yml).

Para auditar los datos:

```bash
python3 scripts/audit_catalog.py
python3 scripts/audit_review_queues.py
python3 scripts/verify_catalog_evidence.py
python3 scripts/validate_research_assets.py
python3 scripts/validate_family_groups.py
python3 scripts/validate_ai_manifest.py
python3 scripts/validate_catalog_reviews.py
python3 -m unittest discover -s scripts/tests -v
```

Estas comprobaciones validan estructura y trazabilidad, **no técnica deportiva ni salud**. La compilación, migraciones Room y pruebas reales en Android se considerarán verificadas solo después de obtener resultados de CI y pruebas de dispositivo.
