# Solo Leveling Gym Breaker

Aplicación Android nativa desarrollada con **Kotlin + XML**, Room, Coroutines
y arquitectura orientada a estado con ViewModel/StateFlow.

## Estado: prototipo 0.4.0

**Shadow Core AI (offline)** está integrado en el código fuente:

- Interpreta solicitudes habituales de rutinas en español, sin Internet.
- Extrae tiempo disponible, equipamiento y grupos musculares.
- Propone rutinas mediante un planificador que aplica restricciones obligatorias.
- Analiza descriptivamente esfuerzos de sesiones confirmadas.
- Genera mensajes de progreso y misiones RPG sin penalizaciones físicas.
- Guarda las sesiones y preferencias en Room/SQLite.

No es un modelo de lenguaje generativo. No contiene modelos descargados ni acceso a
servidores. La selección de un modelo neuronal adicional está pendiente de evaluación.

**Presupuesto de modelos offline: como máximo 60 MiB.** El proyecto verifica los
archivos de modelos empaquetados; no se debe interpretar como tamaño medido de la
APK, memoria RAM o base de datos. Los componentes actuales son compactos y
no se rellenarán artificialmente hasta 60 MB.

## Catálogo: familias y variantes

El proyecto conserva cinco categorías principales y un objetivo pendiente de
2.000 ejercicios biomecánicamente diferentes (400 por categoría).

- 2.186 registros de investigación procedentes de cuatro fuentes abiertas,
  todavía sin certificación como 2.000 ejercicios independientes.
- 44 grupos léxicos para revisión de variantes; 29 conflictos de categoría;
  cola especializada de 273 nombres de cardio y movilidad.
- Diez casos documentados de cribado editorial preliminar.
- Variantes almacenadas en Room bajo su ejercicio principal (migración 2→3).
- La importación de fichas nuevas requiere procedencia, verificación técnica
  documentada y una aprobación expresa. **Hay 0 familias nuevas aprobadas**,
  por lo que la app conserva sus 12 ejercicios de demostración.

Consulta [investigación y controles](docs/CATALOG_RESEARCH.md) y
[fuentes de cardio y movilidad](catalog/research/CARDIO_MOVILIDAD.md).

## Compilación

El flujo [Android Debug CI](.github/workflows/android.yml) ejecuta las pruebas
unitarias, la auditoría de ejercicios y variantes, el bloqueo de fichas no aprobadas,
la comprobación del presupuesto de modelos y `assembleDebug`. Consultar el
resultado real de GitHub Actions antes de afirmar que existe una APK funcional.

## Documentación

- [Shadow Core AI: arquitectura, límites y próximas pruebas](docs/SHADOW_CORE_AI.md)
- [Estado de implementación y tareas pendientes](docs/IMPLEMENTATION_STATUS.md)

## Limitaciones actuales

El catálogo contiene solamente doce fichas de demostración. Faltan el catálogo
revisado de 2.000 ejercicios, recursos visuales oficiales autorizados, animaciones
definitivas, editor de rutinas, evaluación de inferencia y pruebas en dispositivos.

Las decisiones sobre entrenamiento son propuestas revisables, no diagnósticos;
el progreso RPG no determina la capacidad física del usuario.
