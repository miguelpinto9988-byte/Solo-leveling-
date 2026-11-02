# Arquitectura de IA offline — Solo Leveling Gym Breaker

Fecha de revisión: 2026-11-02. Documento de implementación; no acredita pruebas de APK en dispositivos reales.

## Motores evaluados

| Tecnología | Integración Android | Uso previsto | Estado |
|---|---|---|---|
| Gemini Nano mediante ML Kit GenAI Prompt (AICore) | Kotlin; servicio del sistema | Respuestas narrativas y consultas | Adaptador Kotlin integrado, **sin pruebas en teléfono compatible** |
| Google LiteRT-LM + modelos abiertos (por ejemplo Gemma) | Kotlin; CPU/GPU/NPU según plataforma | Alternativa generativa offline cuando haya modelo y recursos | Investigado, **no integrado** |
| llama.cpp (GGUF) | Android NDK y enlace Kotlin | Alternativa para modelos cuantizados | Investigado, **no integrado** |
| ExecuTorch | Android AAR, CPU/GPU/NPU | Modelos personalizados | Investigado, **no integrado** |
| ONNX Runtime Mobile | Android Runtime | Predictores ML pequeños | Investigado, **no integrado** |

Documentación de referencia:
- https://developers.google.com/ml-kit/genai/prompt/android/get-started
- https://developers.google.com/ml-kit/genai
- https://developers.google.com/edge/litert-lm/android
- https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- https://docs.pytorch.org/executorch/stable/using-executorch-android.html
- https://onnxruntime.ai/docs/tutorials/mobile/

## Implementado en el código

1. **OfflineTrainingIntelligence**: filtrado obligatorio por modalidad/equipamiento, categoría excluida, ejercicios bloqueados y experiencia declarada.
2. **Control conservador**: no generar nuevas propuestas si hay restricciones pendientes de revisión o dos registros recientes de esfuerzo elevado. Ninguna misión fallida incrementa la carga.
3. **Estimación estadística**: calcular el esfuerzo percibido medio SOLO con un mínimo de cinco sesiones suficientemente comparables. No se interpreta como pronóstico clínico.
4. **Room**: esquema v2 que conserva perfiles de entrenamiento, exclusiones, ejercicios registrados y estado de revisión; migración explícita 1→2 que no borra el historial.
5. **NanoOfflineAssistant**: consulta únicamente cuando `FeatureStatus.AVAILABLE`, sin autorizar una descarga por sí misma ni enviar consultas a servicios remotos. Si está ausente o falla, vuelve al asistente por reglas.
6. **Interfaz de revisión**: permitir excluir categorías y declarar una restricción que detenga la generación automática; revisión explícita para resolver el bloqueo.

## Por qué no controla un LLM las cargas físicas

La generación de texto puede producir respuestas erróneas. El modelo solo explica y conversa. Una operación estructural (guardar rutina, editar ejercicios, modificar fechas o series) debe pasar siempre por un planificador validado y, cuando corresponda, confirmación del usuario. Sin una fuente revisada de 2.000 ejercicios y la disponibilidad real del hardware, no es responsable afirmar que existe un entrenador autónomo completo.

## Requisitos de hardware y privacidad

- Android 15 es una referencia del usuario, NO garantía de Gemini Nano. Se comprueba `FeatureStatus` en tiempo de ejecución.
- AICore puede necesitar instalar el modelo cuando el teléfono está conectado. La app no inicia esta descarga automáticamente; actualmente carece de una pantalla de instalación.
- La API de ML Kit limita el uso en segundo plano y tiene cuotas de inferencia. Ninguna alarma, historial ni rutina básica depende de una llamada generativa.
- No hay permisos de Internet solicitados directamente por esta app en su prototipo.
- Los datos registrados se conservan localmente en Room; las funcionalidades de exportación y cifrado de copias están pendientes.

**Compatibilidad relevante:** LiteRT-LM 0.18+ utiliza metadatos de Kotlin 2.4 y requiere un compilador Kotlin 2.3 o superior, mientras el proyecto utiliza Kotlin 2.2.21. Es obligatorio actualizar y verificar el toolchain en una etapa separada antes de integrar ese SDK. Fuentes: https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/api/kotlin/getting_started.md\n\n## Antes de elegir LiteRT-LM o descargar un modelo

Verificar en teléfonos concretos la RAM disponible, el tamaño cuantizado, latencia, generación en español, consumo, licencias de modelo y recursos, límites de memoria y funcionamiento sin red. No se incorporará un archivo de modelo enorme al repositorio sin confirmar estas restricciones y los derechos de redistribución.

## Pruebas

Hay pruebas unitarias de filtrado y estimación. Se completó una verificación aislada de la lógica Kotlin en un compilador local (6 comprobaciones). **No se han ejecutado aún pruebas Android integrales ni inferencia real de Gemini Nano**. Verificar GitHub Actions en la pestaña Actions del repositorio.
