# Solo Leveling Gym Breaker

Aplicación Android nativa desarrollada con **Kotlin + XML**, Room, Coroutines
y arquitectura orientada a estado con ViewModel/StateFlow.

## Estado: prototipo 0.6.0

**Shadow Core AI (offline)** está integrado en el código fuente:

- Interpreta solicitudes habituales de rutinas en español, sin Internet.
- Extrae tiempo disponible, equipamiento y grupos musculares.
- Propone rutinas mediante un planificador que aplica restricciones obligatorias.
- Analiza descriptivamente esfuerzos de sesiones confirmadas.
- Genera mensajes de progreso y misiones RPG sin penalizaciones físicas.
- Guarda las sesiones y preferencias en Room/SQLite.

No es un modelo de lenguaje generativo. No contiene modelos descargados ni acceso a
servidores. La selección de un modelo neuronal adicional está pendiente de evaluación.

**Presupuesto de recursos offline del núcleo de IA: hasta 90 MB decimales.**
El proyecto verifica los archivos de investigación, índices, metadatos y
eventuales modelos empaquetados en assets. Este objetivo **no representa**
consumo de RAM ni tamaño medido del código compilado o la APK. No se añaden
pesos ficticios para llenar el límite.

La interfaz cuenta ahora con un **panel de control** de Shadow Core AI:
explicación de reglas, interpretación de consultas, historial resumido,
codificación/decodificación JSON de informes y medición de recursos locales.
El asistente puede responder consultas sobre variantes, catálogo y proceso
de decisión, pero no es un LLM generativo general.

## Catálogo: familias y variantes

El proyecto conserva cinco categorías principales y un objetivo pendiente de
2.000 ejercicios biomecánicamente diferentes (400 por categoría).

- 2.186 registros de investigación procedentes de cuatro fuentes abiertas,
  todavía sin certificación como 2.000 ejercicios independientes.
- 44 grupos léxicos para revisión de variantes; 29 conflictos de categoría;
  cola especializada de 273 nombres de cardio y movilidad.
- Diez casos documentados de cribado editorial preliminar.
- Variantes almacenadas en Room bajo su ejercicio principal (migración 2→3).
- Los 2.186 registros se empaquetan como **catálogo offline de consulta**
  en 1.823 nombres normalizados por categoría. Una segunda capa los organiza
  en **1.089 agrupaciones provisionales** de movimientos y niveles
  inicial / intermedio / avanzado / sin determinar.
- De los 1.089 grupos, 28 reúnen varios nombres por pistas léxicas y requieren
  revisión humana. **No son familias biomecánicas certificadas.**
- La importación de fichas nuevas requiere procedencia, revisión técnica
  documentada y una aprobación expresa. **Hay 0 familias nuevas aprobadas**;
  la IA sigue utilizando únicamente las 12 fichas de demostración.
- El jugador configura experiencia y dificultad deseada por separado.
  Shadow Core AI selecciona un máximo de una variante autorizada por familia,
  con nivel y equipamiento compatibles, y registra la variante realizada.

Consulta [investigación y controles](docs/CATALOG_RESEARCH.md) y
[fuentes de cardio y movilidad](catalog/research/CARDIO_MOVILIDAD.md),
y [dificultad y variantes en Android](docs/VARIANTES_Y_DIFICULTAD.md).

## Compilación

El flujo [Android Debug CI](.github/workflows/android.yml) ejecuta las pruebas
unitarias, la auditoría de ejercicios y variantes, el bloqueo de fichas no aprobadas,
la comprobación del presupuesto de modelos y `assembleDebug`. Consultar el
resultado real de GitHub Actions antes de afirmar que existe una APK funcional.

## Documentación

- [Shadow Core AI: grupos, acceso completo, auditoría y presupuesto de 90 MB](docs/SHADOW_CORE_90MB_FAMILIAS.md)
- [Shadow Core AI: arquitectura, límites y próximas pruebas](docs/SHADOW_CORE_AI.md)
- [Estado de implementación y tareas pendientes](docs/IMPLEMENTATION_STATUS.md)

## Limitaciones actuales

El catálogo contiene solamente doce fichas de demostración. Faltan el catálogo
revisado de 2.000 ejercicios, recursos visuales oficiales autorizados, animaciones
definitivas, editor de rutinas, evaluación de inferencia y pruebas en dispositivos.

Las decisiones sobre entrenamiento son propuestas revisables, no diagnósticos;
el progreso RPG no determina la capacidad física del usuario.
