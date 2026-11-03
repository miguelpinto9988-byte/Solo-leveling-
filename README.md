# Solo Leveling Gym Breaker

Aplicación Android nativa en **Kotlin + XML**, Room/SQLite, Coroutines, MVVM y
Shadow Core AI offline. Estado de código: **prototipo 0.6.1**, todavía sin APK
validada mediante pruebas de compilación y ejecución en dispositivo.

## Shadow Core AI offline

El motor interpreta solicitudes habituales en español, permite consultar el
progreso, propone rutinas desde fichas habilitadas y explica sus filtros. Los
informes se codifican y decodifican mediante JSON local, sin conexión a Internet.
La IA no es un modelo generativo de propósito general.

El límite configurado para **archivos de recursos offline de IA es 90 MB
decimales**. No representa el tamaño instalado de la APK ni RAM. Gradle verifica
el presupuesto y GitHub Actions está configurado para pruebas y compilación.

## Catálogo depurado: documentación frente a aprobación

Durante la investigación se localizaron **2.186 registros de fuentes abiertas**,
equivalentes a **1.823 denominaciones por categoría**. Esta recopilación se
conserva en `catalog/staging` exclusivamente como archivo de investigación.

Después de comprobar fuentes, categorías, niveles y patrones confusos, la
**APK solo incluye 198 denominaciones documentales**, con **402 referencias de
origen**, organizadas en **119 familias provisionales** y cinco categorías:

| Categoría | Denominaciones retenidas | Familias propuestas |
| --- | ---: | ---: |
| Tren superior | 125 | 67 |
| Tren inferior | 35 | 23 |
| Zona central | 24 | 16 |
| Cardio | 3 | 2 |
| Movilidad | 11 | 11 |
| **Total** | **198** | **119** |

Las **1.625 denominaciones excluidas** no se muestran en la APK; su motivo y
procedencia siguen disponibles en `catalog/review/verification_registry.json`.
Se han descartado entradas sin corroboración, niveles contradictorios,
conflictos de categoría y patrones de ejecución ambiguos. ACE aporta
referencias externas para algunos movimientos; no se reproduce su contenido.

**Importante:** corroboración documental **NO equivale a aprobación profesional
para generar rutinas**. Las relaciones entre nombres y familias siguen siendo
provisionales; no se han firmado fichas nuevas por un profesional cualificado.
El archivo `catalog_approved.json` contiene cero familias nuevas aprobadas.
Shadow Core AI mantiene los 12 ejemplos previos como demostración y no utiliza
automáticamente las 198 denominaciones investigadas.

Las variantes se agrupan por niveles **inicial** e **intermedio** con datos
concordantes. No se conservaron variantes avanzadas con evidencia suficiente
en esta depuración. Las variantes no incrementan el número de ejercicios
principales. La dificultad elegida por el jugador y las restricciones
siguen siendo límites obligatorios del planificador.

## Documentación y auditorías

- [Informe de depuración, criterios y fuentes](docs/DEPURACION_VARIANTES_2026.md)
- [Registro de aceptación y descarte](catalog/review/verification_registry.json)
- [Diseño offline y presupuesto 90 MB](docs/SHADOW_CORE_90MB_FAMILIAS.md)
- [Estado de implementación](docs/IMPLEMENTATION_STATUS.md)
- [GitHub Actions](.github/workflows/android.yml)

Las pruebas y auditorías configuradas se ejecutan en GitHub Actions. No debe
afirmarse que existe una APK funcional sin comprobar una compilación exitosa
y pruebas en un dispositivo Android. Los recursos gráficos oficiales y el
catálogo final de 2.000 movimientos independientes continúan pendientes.

El sistema no diagnostica lesiones y las misiones RPG no incluyen
penalizaciones físicas ni progresión forzada.
