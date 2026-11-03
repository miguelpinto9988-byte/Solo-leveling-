# Selector de ocho avatares PNG sin marcos — Gym Breaker 0.9.2

## Integración Android y archivos exactos

Se reemplaza el selector antiguo de **seis sombras provisionales** por los **ocho retratos elegidos por el usuario**, sin marcos incrustados y con alfa real, cada uno en su propio archivo RGBA de 1254×1254. Las sombras y los monarcas **no** son avatares.

Las siguientes rutas deben existir dentro del repositorio para compilar el **APK final con imágenes integradas**:

| Etiqueta visible | Archivo obligatorio |
|---|---|
| Sung Jinwoo | `app/src/main/assets/profile_avatars/avatar_01_sung_jinwoo.png` |
| Woo Jinchul | `app/src/main/assets/profile_avatars/avatar_02_woo_jinchul.png` |
| Sung Jinah | `app/src/main/assets/profile_avatars/avatar_03_sung_jinah.png` |
| Go Gunhee | `app/src/main/assets/profile_avatars/avatar_04_go_gunhee.png` |
| Choi Jong-in | `app/src/main/assets/profile_avatars/avatar_05_choi_jongin.png` |
| Baek Yoonho | `app/src/main/assets/profile_avatars/avatar_06_baek_yoonho.png` |
| Liu Zhigang | `app/src/main/assets/profile_avatars/avatar_07_liu_zhigang.png` |
| Cha Hae-in | `app/src/main/assets/profile_avatars/avatar_18_cha_haein.png` |

## Uso

- `OnboardingActivity` presenta ocho retratos en dos columnas con selección real.
- `HunterSetupStore` guarda un solo avatar elegido de esta lista; el valor predeterminado es Sung Jinwoo. Las selecciones antiguas de sombras se migran al valor predeterminado, sin alterar XP ni objetivos.
- `ProfileArtwork` decodifica cada PNG desde `assets/profile_avatars/` usando `BitmapFactory.Options.inSampleSize` y una caché limitada en memoria, con relación de aspecto preservada.
- El selector permite conservar una **foto propia JPG/PNG** sin enviarla fuera del dispositivo.
- El marco inicial es **Sin marco**; se conservan Violeta, Cian y Dorado como overlays *opcionales* independientes del retrato.
- El perfil sigue siendo **pantalla independiente**, accesible únicamente mediante la foto superior de Inicio; la barra inferior no incluye Perfil.
- El paso 3 conserva las misiones semanales 1–7, calendario semanal y descansos; el paso 4, las únicas tres dificultades Básico, Intermedio y Avanzado. El rango E–S depende de XP real.

## Estado de recursos y seguridad de entrega

**ATENCIÓN**: Los ocho PNG fuente de 1254×1254 ya se entregaron como ZIP en esta conversación y están cargados en Canva; **aún no se han incorporado como binarios a GitHub**. La integración de código es real, pero la APK producida por CI **no incluye las imágenes hasta que esos ocho archivos se incorporen a las rutas arriba indicadas**. Para evitar confundir un archivo ausente con la imagen correcta, el código muestra un icono temporal cuando falta un PNG y anuncia su ausencia en accesibilidad.

Un paquete `GymBreaker_8_Avatares_Para_Android.zip` preparado en la conversación lleva los ocho originales bajo las rutas exactas y un manifiesto SHA-256. Una vez agregados a la rama de la PR, volver a ejecutar `assembleDebug` y comprobar por ZIP/Android que están los ocho recursos. No sustituir por miniaturas ni exportaciones ampliadas.

La vista `Previsualizacion_Perfil_8_Avatares_Transparente_0_9_2.html` usa los **ocho originales de 1254px** embebidos, no placeholders ni capturas: reproduce las cinco pantallas del formulario más bienvenida y confirmación, en HTML/CSS/JavaScript. Es una vista interactiva del diseño y NO una captura de un dispositivo Android real.

## Pruebas de aceptación

1. Ocho retratos PNG 1254×1254, canales alfa variables y cero marcos en los propios archivos.
2. Al elegir el octavo personaje, persiste Cha Hae-in y se refleja en la confirmación y cabecera de Inicio.
3. La foto propia tiene prioridad cuando se selecciona; el marco sigue siendo opcional.
4. No se pueden seleccionar Igris, Beru, Tank, Iron, Tusk ni Kaisel como avatares de perfil.
5. Sin pérdida de XP, restricciones, semana o dificultad al editar un perfil existente.
6. Teclado y scroll funcionales en móviles pequeños; prueba visual real en Android pendiente.
