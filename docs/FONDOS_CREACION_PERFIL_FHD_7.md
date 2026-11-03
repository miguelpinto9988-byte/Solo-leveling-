# Escenarios Full HD para la creación de perfil

El asistente nativo `OnboardingActivity` utiliza **siete PNG distintos de 1080 × 1920**. Los formularios, botones, fotos y textos permanecen en vistas Android por encima de la imagen y de un velo de contraste, no se incrustan como imagen fija.

| Paso | Pantalla | Fondo |
|---:|---|---|
| 0 | Bienvenida | `bg_01_ciudad_moderna_fhd.png` |
| 1 | Información básica | `bg_03_sede_de_la_asociacion_fhd.png` |
| 2 | Apariencia del perfil y ocho avatares | `bg_07_bosque_de_rangos_fhd.png` |
| 3 | Tu semana y misión 1–7 | `bg_02_gimnasio_del_cazador_fhd.png` |
| 4 | Objetivos y dificultad | `bg_09_cueva_de_cristales_fhd.png` |
| 5 | Restricciones | `bg_08_ruinas_antiguas_fhd.png` |
| 6 | Perfil creado | `bg_06_base_de_cazadores_fhd.png` |

## Instalación de los recursos

Los PNG se prepararon individualmente, con sus rutas y hashes de verificación, en el paquete de la conversación `GymBreaker_Fondos_Creacion_Perfil_FHD_7.zip`.

Copiar exactamente los siete archivos a:

```
app/src/main/assets/onboarding_backgrounds/
```

El código `ProfileSceneArtwork.kt` los carga por nombre desde esa carpeta, valida el tamaño nativo y utiliza `BitmapFactory` con muestreo para cuidar memoria. En caso de ausencia del archivo, conserva los fondos FHD anteriores del asistente en lugar de detenerse.

**Estado actual de GitHub:** esta PR contiene la integración Kotlin, mapa de etapas y pruebas de regresión. Los siete PNG binarios se entregan en el ZIP de la conversación, pero **todavía se deben incorporar a la rama** para que la compilación de CI y la APK de GitHub los incluyan realmente. No presentar la compilación de CI como una APK con estos fondos si faltan los archivos.

## Regla de uso

- No se incorporan fondos Monarca en la creación del perfil.
- Cada imagen se muestra con `CENTER_CROP`, manteniendo proporciones.
- Capa oscurecida: protege la legibilidad, especialmente sobre imágenes luminosas.
- No altera la experiencia, rango, dificultad, restricciones, selección de los ocho avatares ni funciones de Shadow Core.

## Revisión de diseño

Los archivos entregados como PNG FHD son **escenas geométricas originales**, no reproducciones fotográficas de las miniaturas anime de la biblioteca inicial. Su tamaño es Full HD real, pero la resolución de píxeles por sí sola no equivale a igualdad de detalle artístico. Revisar el aspecto con la previsualización HTML antes de darlos por definitivos.

Faltan como aceptación de release: subir los PNG a GitHub, comprobar su presencia dentro del APK, verificar selector de fotos y contraste en Android real, y validar arte final con el usuario.
