# Solo Leveling Gym Breaker

Aplicación Android nativa desarrollada con **Kotlin + XML**, Room, Coroutines
y arquitectura orientada a estado con ViewModel/StateFlow.

## Estado: prototipo 0.3.0

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

## Compilación

El flujo [Android Debug CI](.github/workflows/android.yml) ejecuta las pruebas
unitarias, la comprobación del presupuesto y `assembleDebug`. Consultar el
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
