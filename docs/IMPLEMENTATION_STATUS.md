# Registro de implementación · 0.2.0

## Integrado al código fuente (sin certificación de ejecución)
- Proyecto Kotlin + interfaces XML, Gradle Kotlin DSL, Android SDK.
- MVVM con ViewModel, StateFlow, Coroutines y Flow.
- Room + SQLite: ejercicios de demostración y sesiones completadas.
- Motor local determinista con exclusiones por categorías y ejercicios, experiencia declarada y bloqueo de revisión.\n- Estimador de esfuerzo offline que requiere cinco sesiones comparables.\n- Perfil Room y metadatos de sesiones con migración 1→2.
- Sistema RPG básico: experiencia virtual, nivel, rango y avance semanal.
- Misiones Monarca narrativas, opcionales; sin incrementos punitivos de carga.
- Asistente contextual por reglas y adaptador Gemini Nano ML Kit Prompt (modelo generativo condicional, NO verificado en dispositivo).
- Navegación: Sistema, Misiones, Entrenar, Sombras, IA y Ajustes.
- Temas claro y oscuro, texto en negrita y cuatro colores de acento para botones y marcos.
- Gradientes suaves para tarjetas y seis acompañantes originales PNG transparentes 256 × 256.
- Alarma diaria aproximada con permiso de notificaciones, restauración al reinicio.
- Pruebas JUnit del planificador, rangos y asistente.
- GitHub Actions configurado para pruebas y compilación APK de depuración.

## Pendiente para el producto objetivo
- Los **2000 ejercicios revisados**: por ahora hay 12 ejemplos, sin validación biomecánica.
- Editor completo de rutinas, sesión guiada por series/descansos y estadísticas avanzadas.
- Sistema de acompañantes animados con partículas y aura reactiva.
- Imágenes oficiales/escenarios autorizados: se necesitan los archivos y el alcance de las licencias.
- Gemini Nano requiere hardware compatible y modelo disponible. LiteRT-LM queda pendiente de un toolchain Kotlin >=2.3 y la selección/verificación del modelo.
- Comprobación con datos reales de las estimaciones estadísticas y de su calidad.
- Misiones Monarca y recompensas complejas.
- Alarmas por días, sonidos personalizados y ajustes de precisión según permisos.
- Exportación/restauración segura, migraciones de base de datos y pruebas instrumentadas.
- Validación de rendimiento, accesibilidad y compatibilidad física.
- APK firmada para distribución: la app-debug.apk de CI es solo una compilación de desarrollo.

## Seguridad
- Sin permisos de micrófono, cámara, contactos, ubicación ni Internet en esta primera versión.
- Sin secretos ni credenciales.
- Los puntos y rangos son símbolos del juego, no indicadores de salud.
- Las misiones no penalizan con ejercicios adicionales ni aumentos de carga.
- La app no verifica técnica ni diagnostica lesiones.

## Compilación
El flujo CI usa JDK 17, SDK 36, Gradle 8.13 y AGP 8.13.2 con Kotlin 2.2.21.
Abrir **Actions → Android Debug CI** para ver resultados. Solo un resultado exitoso y
un artefacto presente permitirían afirmar que la APK de prueba se ha compilado.

Versión mínima técnica provisional: Android 8.0 (API 26). Android 15+ será evaluado,
según hardware y disponibilidad del asistente generativo, antes del lanzamiento.
