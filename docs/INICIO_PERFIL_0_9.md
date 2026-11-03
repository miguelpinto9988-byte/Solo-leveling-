# Inicio 0.9.0: navegación, perfil y fondos

## Recorrido de entrada

1. `SplashActivity` muestra la animación del póster de Igris y prepara el modelo local.
2. Si `HunterSetupStore.completed() == false`, se abre `OnboardingActivity` obligatoriamente; de lo contrario, se abre `MainActivity` en **Inicio**.
3. **La fotografía de la cabecera se muestra y habilita únicamente en Inicio.** Abre la actividad interna `ProfileActivity`, sin navegación inferior. El perfil se edita desde esa actividad y vuelve al inicio conservando el rango y los datos.
4. Barra inferior: **Inicio, Rutinas, Misiones, Progreso, Más**. El menú Más contiene Sombras, IA, Catálogo y Ajustes; no contiene Perfil. Los ajustes globales no ofrecen acceso directo a edición del perfil.
5. Las pantallas internas comparten una sola actividad de navegación. Al pulsar Atrás desde una sección secundaria se regresa a Inicio.

## Tarjetas funcionales de Inicio

- **Tu sistema de cazador:** alias, rango E-S y nivel calculados únicamente sobre XP confirmada. Indicador para el próximo nivel; dificultad independiente del rango.
- **Entrenamiento de hoy:** grupo previsto de acuerdo a los siete días de la configuración; si es descanso, no genera entrenamiento.
- **Tu semana:** siete indicadores por día y el objetivo de 1 a 7 misiones; cuenta de sesiones confirmadas con una etiqueta que distingue sesiones de misiones.
- **Tu progreso:** sesiones registradas y XP real; acceso a una nueva pantalla Progreso.
- **Sombra acompañante:** muestra el avatar decorativo y enlaza a Sombras.
- **Shadow Core:** explica las recomendaciones del asistente y enlaza a la sección IA.

## Recursos y exclusividad de monarcas

Se incorporan **34 fondos decorativos WebP** desde la PR #5 mediante los hashes de blob de GitHub: 25 escenarios normales y 9 de monarcas. Inicio rota escenarios de ciudad, gimnasio, exploración, portales y sombras. **Los fondos `mission_bg_monarch_*` solo se muestran en la tarjeta de misión Monarca semanal**, nunca en Inicio ni en el perfil. Seleccionar un Monarca cambia exclusivamente la ilustración; no cambia la dificultad física ni las restricciones.

## Seguridad y persistencia

- Las restricciones de movimiento y el bloqueo de revisión no se omiten; el modo de descanso no penaliza.
- La fotografía elegida permanece en almacenamiento privado. No se solicita permiso de acceso permanente a fotos ni acceso a Internet.
- El rango y XP nunca se restablecen al editar el perfil.
- Las tres dificultades son `inicio`, `intermedio` y `avanzado`; ninguna autoriza ejercicios del catálogo sin aprobación técnica.
- Los datos reales proceden de Room y del perfil local; el diseño no inventa estadísticas.

## Estado técnico

CI de PR #7 valida Python, Kotlin/JVM, `assembleDebug`, presupuesto de modelo offline, integridad y firma Android. La prueba en un dispositivo real sigue pendiente.

**Calidad gráfica pendiente antes de fusionar**: las imágenes WebP que GitHub importó desde la PR #5 proceden de una miniatura provisional. El APK mejorado generado para el usuario fue recompuesto con los 34 WebP de 720 × 320 del ZIP original y el póster Igris de 992 × 1585; está firmado con una clave local Debug de pruebas y puede tener una firma diferente de otras instalaciones. Para que la compilación sea plenamente reproducible desde GitHub, sustituir también `app/src/main/res/drawable-nodpi/` por los recursos del ZIP `Gym-Breaker-v0.9.0-Recursos-FullHD-para-GitHub.zip` entregado en la conversación. **La PR continúa en borrador hasta completar ese paso y revisar la interfaz en móviles reales.**

El diseño es una **implementación nativa funcional** inspirada en la maqueta, no una imagen fija ni una reproducción píxel por píxel en todas las pantallas.
