# Biblioteca visual · personajes y sombras

**Estado: índice de metadatos en GitHub; importación de binarios pendiente.**

Esta biblioteca organiza las **29 referencias entregadas por el usuario** (28 fotografías numeradas y una referencia anterior de Igris), distinguiéndolas de los **8 primeros conceptos ilustrados** y de las fuentes externas consultadas. El paquete de originales, fichas detalladas, bocetos y galería HTML se entregó como `Biblioteca_personajes_Solo_Leveling_Gym_Breaker_v1.zip` dentro de la conversación.

## Inventario y diseño

- `manifest_referencias.tsv`: huellas SHA-256 exactas y rutas planeadas para los 29 originales.
- `modelos_y_variantes.json`: características visuales que deben mantenerse, vistas, fondos y poses para futuras recreaciones.
- `fuentes_externas.json`: investigación visual en sitios oficiales y sitios de consulta. **No se incluyen archivos de esos sitios.**
- `catalogo.json`: ficha individual ampliada **dentro del ZIP entregado al usuario**, que debe subirse al repositorio con los binarios.
- `originales_usuario/{cazadores,sombras,otros}/`: **pendiente de cargar los JPEG del ZIP** en esas rutas.
- `bocetos_generados/`: los 8 diseños individuales y el roster de la primera ronda están en el ZIP; son muestras no aprobadas.

## Política de reutilización

No hay evidencia documental de la licencia específica en esta conversación. El usuario declara que cuenta con autorización para sus imágenes, pero **no se ha comprobado si cubre adaptación, entrenamiento de modelos, redistribución y uso comercial**. No asumir que una galería oficial, una captura de anime o una imagen hallada en Internet es de libre uso; las URLs externas se conservan únicamente como referencias de estudio. Toda variante derivada debe revisarse y aprobarse antes de mostrarse dentro de la APK.

La biblioteca es **material fuente de diseño**, no forma parte de `app/src/main/assets`: de otro modo aumentaría la APK y mezclaría recursos sin aprobación con la aplicación distribuida.

## Instalación de los originales

1. Extraer el ZIP en la raíz del repositorio, respetando sus rutas `personajes/v1/`.
2. Subir los 29 JPEG originales, `catalogo.json`, `fichas_personajes.json`, `plantillas_recreacion.json`, `galeria.html` y `verificar_integridad.py`.
3. Ejecutar `python verificar_integridad.py` después de importar. El archivo ZIP ya pasó esta comprobación con 29 de 29 hashes válidos.
4. Mantener esta PR en borrador hasta comprobar que los JPEG están realmente presentes en GitHub y que hay documentación sobre los derechos de uso para publicación.

## Estado de implementación

No se han generado modelos 3D (mallas, rigs o FBX); se creó una primera tanda de concept art 2D, susceptible de nuevas poses y fondos. Varias fotografías de personas y criaturas se mantienen **sin nombre confirmado** para evitar asignaciones incorrectas de identidad.
