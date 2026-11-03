package com.gymbreaker.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.gymbreaker.app.ai.ShadowLocalLanguageModel
import com.gymbreaker.app.data.TrainingDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Única Activity de lanzamiento: muestra la ilustración de Igris antes de
 * presentar las pantallas funcionales. No reabre el splash al pulsar Atrás.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val poster = SplashArtView(this)
        setContentView(poster)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        lifecycleScope.launch {
            val startedAt = SystemClock.elapsedRealtime()
            poster.updateProgress(1)

            // El porcentaje refleja la preparación autenticada del GGUF
            // (hasta 85 %) y la apertura de datos locales (hasta 95 %).
            // Si falta memoria o la ABI no es compatible, el resto de la
            // aplicación continúa usando sus reglas locales.
            withContext(Dispatchers.IO) {
                ShadowLocalLanguageModel.prepare(applicationContext) { percent ->
                    poster.post {
                        if (!isFinishing && !isDestroyed) {
                            poster.updateProgress((percent * 85 / 100).coerceIn(1, 85))
                        }
                    }
                }
                poster.post { if (!isFinishing && !isDestroyed) poster.updateProgress(86) }
                try {
                    val db = TrainingDatabase.get(applicationContext)
                    db.trainingDao().getProfile()
                    db.trainingDao().countExercises()
                } catch (_: Exception) {
                    // El repositorio muestra errores de datos en su flujo habitual.
                    // La carga gráfica no impide que se abra la aplicación.
                }
            }

            poster.updateProgress(95)
            // El fotograma inicial y la animación deben resultar visibles, sin
            // retrasar artificialmente una carga real larga.
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            if (elapsed < 900) delay(900 - elapsed)
            poster.updateProgress(100)
            delay(240)
            if (!isFinishing && !isDestroyed) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
}
