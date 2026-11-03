# Solo Leveling Gym Breaker

Aplicación Android nativa en **Kotlin + XML**, Room/SQLite, Coroutines, MVVM y Shadow Core AI offline. Estado: **prototipo**; el modelo GGUF necesita compilación con descarga verificada y pruebas en dispositivo antes de declararse estable.

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

El motor híbrido combina reglas locales comprobables para rutinas y restricciones con **SmolLM2-135M-Instruct Q2_K, un modelo GGUF neuronal real cuantizado de ~88,2 MB** utilizado únicamente para respuestas conversacionales experimentales. Los pesos proceden de una revisión fijada de [TensorBlock / Hugging Face](https://huggingface.co/tensorblock/SmolLM2-135M-Instruct-GGUF) y se autentican con SHA-256 (`478cca924101ed67bb2751f597be41532b2f4817ee2ab1b0d7cc379c76e79faf`) antes de crear la APK. La licencia del modelo es Apache-2.0 y la biblioteca de inferencia llama.cpp Android es MIT.

**Importante:** el modelo es muy pequeño, está entrenado principalmente en inglés y esta cuantización sacrifica calidad; **no es fiable para prescribir ejercicios ni evaluar salud**. Por diseño, solo atiende conversación general; el catálogo, la dificultad, el equipamiento, los límites y las decisiones de entrenamiento permanecen en el sistema de reglas existente. Funciona sin Internet en dispositivos `arm64-v8a` compatibles y, si falla la inferencia, se conserva el asistente por reglas. La primera consulta prepara una copia local privada del modelo, por lo que el almacenamiento instalado será superior al tamaño de descarga.

La compilación descarga el GGUF durante GitHub Actions con `python3 scripts/fetch_offline_model.py`. No se agregan 88 MB al historial de Git. El límite de 90 MB se refiere a **recursos del asistente**, no al tamaño final de la APK ni a la RAM. **El modelo no aumenta la cantidad de familias de ejercicios aprobadas**.

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

## Splash inicial de Igris · versión 0.7.1

- Al iniciar Android se abre `SplashActivity` antes de `MainActivity`.
- `SplashArtView` muestra el diseño y redibuja el progreso real, los porcentajes y una silueta que corre con zancadas animadas. La barra ya no se queda en el 72 % del boceto.
- El avance hasta 85 % corresponde a preparar el modelo GGUF local autenticado, y el resto a inicializar datos y abrir la app. Si el móvil no soporta inferencia local, se continúa con el motor por reglas.
- Se respeta la desactivación de animaciones del dispositivo. No exige Internet.
- **Pendiente antes de fusionar esta PR:** sustituir `app/src/main/res/drawable-nodpi/loading_poster.png` (miniatura transferida de 125 × 200) por el PNG aprobado original de 992 × 1585. El APK FullHD entregado en la conversación ya lleva el original y está firmado para pruebas. El repositorio no debe presentarse como FullHD hasta reemplazar ese recurso binario.
- La firma de la APK FullHD de prueba se generó localmente y puede diferir de las firmas Debug de GitHub Actions; al instalar sobre otra versión, se debe respaldar información antes de cualquier desinstalación.
