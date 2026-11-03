# Perfil inicial · Solo Leveling Gym Breaker 0.8.0

## Flujo aprobado y regla de primer inicio

`SplashActivity` comprueba `HunterSetupStore.completed()` después de la barra de carga. Si no hay un registro completado, abre la **actividad independiente `OnboardingActivity`**, no la pantalla principal. El perfil no puede omitirse desde la navegación habitual. Una vez guardados Room y los ajustes privados, `profile_created` queda marcado; los siguientes arranques van a `MainActivity`.

La interfaz se ha implementado con **controles Android verdaderos**, no como un JPG estático. Conserva estética oscura, sombras, castillo, marco luminoso y botones violetas de la maqueta aprobada; las proporciones del boceto no son idénticas píxel por píxel en todos los teléfonos.

### Cinco pasos y confirmación

1. **Información básica:** alias obligatorio (2–35 caracteres); género, edad, estatura y peso opcionales. No se calculan objetivos corporales.
2. **Apariencia y tema:** escoger una foto del teléfono o cualquiera de seis avatares; marco violeta, cian o dorado; elegir tema claro/oscuro y acento. El arte de fondo se mantiene automático. La foto se reescala y se copia a un JPEG privado sin permisos permanentes a la galería.
3. **Tu semana:** dos pestañas, *Misiones* y *Rutinas diarias*. La pestaña Misiones despliega de **1 a 7** objetivos narrativos semanales; nunca 0 ni 8. Rutinas permite configurar los siete días, descanso o grupo (cuerpo completo, tren superior, inferior, central, cardio o movilidad), además del tiempo y descansos entre bloques.
4. **Objetivos y dificultad:** elige objetivo principal, escribe un subobjetivo y solicita una interpretación local orientativa de Shadow Core. La propuesta se **confirma explícitamente** antes de cambiar el objetivo. Hay exactamente tres opciones para la dificultad física: `inicio`, `intermedio` y `avanzado`.
5. **Restricciones opcionales:** limitaciones declaradas y equipamiento; las molestias y limitaciones físicas dejan `requiresReview=true` en Room.
6. **Confirmación:** resumen de avatar, objetivos, dificultad, tiempo, semana y rango inicial E. Al aceptar se abre la aplicación.

### Reglas clave

- **Rango**: E al comenzar, después D, C, B, A y S mediante XP de sesiones confirmadas (`HunterProgression.rank`). El rango no equivale a la dificultad física.
- **Dificultad**: Básico, Intermedio o Avanzado, solo preferencias para seleccionar variantes ya disponibles y autorizadas. Elegir Avanzado no habilita variantes no aprobadas.
- **Misiones 1–7**: meta de la semana, no exigencia diaria ni justificación para aumentar cargas. Sin castigos por descanso o sesiones no realizadas.
- **IA y subobjetivo**: la interpretación actual es por reglas locales auditables; no se hace pasar un resumen generativo experimental por evaluación médica ni se reprograman rutinas sin confirmación.
- **Privacidad**: el retrato queda en `filesDir`; resto de los campos en preferencias privadas y la dificultad, tiempo y revisión de seguridad en Room. No se solicita Internet.
- **Edición posterior**: la pestaña Perfil/Personalización permite reabrir los mismos paneles sin reiniciar la experiencia ni borrar XP.

### Verificaciones y límites

`python3 -m unittest discover -s scripts/tests` verifica que la aplicación enlaza el launcher al onboarding, exige nombre, conserva exactamente tres niveles, elige de 1 a 7 misiones y mantiene las restricciones. La compilación Android requiere su validación habitual `assembleDebug` y una prueba de contraste, teclado, navegación atrás y selector de imágenes en teléfono.

El splash original de esta rama heredada de la PR #3 todavía usa una miniatura del póster en GitHub; el arte FullHD mostrado previamente debe reemplazar ese recurso binario antes de publicar. Las bibliotecas de fondos Monarca siguen en la PR #5 y no se incorporaron al perfil: se reservan para sus misiones específicas.
