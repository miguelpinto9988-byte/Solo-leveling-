# Familias, variantes y dificultad · Shadow Core AI 0.6.1

## Definición

Un **ejercicio principal** corresponde a una familia canónica de movimiento.
Una **variante** es una forma de ejecutar ese movimiento con un equipamiento,
posición o técnica que requiere documentación específica. Las variantes
**nunca incrementan el total de ejercicios principales**.

## Catálogo vigente en la APK

Se estudiaron 2.186 registros originales, organizados en 1.823 nombres.
Tras aplicar controles de fuente, dificultad y clasificación, solo
permanecen **198 denominaciones con documentación concordante**, respaldadas
por **402 registros originales** y agrupadas en **119 familias propuestas**.

- Tren superior: 125 nombres en 67 familias propuestas.
- Tren inferior: 35 nombres en 23 familias propuestas.
- Zona central: 24 nombres en 16 familias propuestas.
- Cardio: 3 nombres en 2 familias propuestas.
- Movilidad: 11 nombres en 11 familias propuestas.

**20 agrupaciones reúnen varias denominaciones** bajo un patrón probable.
Se requiere una revisión técnica antes de declarar que pertenecen
biomecánicamente a la misma familia.

Las otras **1.625 denominaciones** están retiradas de los recursos de la
aplicación. Se conserva su auditoría en
`catalog/review/verification_registry.json` y los registros originales
en `catalog/staging`, fuera de la APK.

## Niveles

Los niveles conservados son:
- **Inicial:** 145 denominaciones.
- **Intermedio:** 53 denominaciones.
- **Avanzado:** ninguna con documentación suficiente según estos criterios.
- **Sin determinar:** ninguna mostrada; las entradas inconclusas se retiraron.

Una variante se cuenta una sola vez incluso si varias fuentes la
describen. La vista muestra su nombre y las referencias que respaldan
el nivel; no presenta cada cita como un ejercicio independiente.

## Estados separados

`source_corrob_catalog_only` indica corroboración de metadatos entre
catálogos, **no aprobación de seguridad ni de ejecución**.
`approved` requeriría documentación de revisión técnica independiente,
atribución y compatibilidad con las restricciones.

En la versión actual el archivo `catalog_approved.json` contiene cero
nuevas familias aprobadas. Las 12 fichas de demostración permanecen
separadas de la investigación y no se han presentado como validadas.

## Selección por Shadow Core AI

`ExerciseVariantSelector.kt` considera nivel escogido, experiencia,
equipamiento, exclusiones y restricciones. Solo utiliza variantes
revisadas y autorizadas. El usuario puede consultar las 198
denominaciones depuradas, pero **el motor no puede convertirlas
automáticamente en propuestas de rutina**.

La personalización no impone subidas de dificultad por puntos, rachas
o misiones RPG. Los registros de historial de variantes y sus
migraciones Room permanecen disponibles.

## Herramientas de auditoría

`scripts/verify_catalog_evidence.py` controla el origen de las entradas.
`scripts/validate_research_assets.py` impide que entren variantes
retiradas en los recursos Android. `scripts/validate_family_groups.py`
verifica la relación familia-variante. `scripts/validate_ai_manifest.py`
comprueba el presupuesto global de 90 MB.

En [DEPURACION_VARIANTES_2026.md](DEPURACION_VARIANTES_2026.md) constan
los motivos de descarte y las referencias a fichas ACE consultadas.

**La versión compilada y los consumos reales de RAM y almacenamiento
siguen pendientes de verificar en Android.**
