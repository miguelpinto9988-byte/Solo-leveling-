plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.gymbreaker.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.gymbreaker.app"
        minSdk = 26 // provisional hasta confirmar dispositivos
        targetSdk = 36
        versionCode = 7
        versionName = "0.6.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }
    testOptions { unitTests.isReturnDefaultValues = true }
}
dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
}

/**
 * Comprueba los modelos empaquetados en assets/shadow_core contra el límite.
 * Este límite se refiere a archivos de modelo; no equivale a RAM o APK total.
 */
tasks.register("verifyShadowCoreModelBudget") {
    group = "verification"
    description = "Rejects offline model assets larger than 90 MB"
    doLast {
        val models = file("src/main/assets/shadow_core")
        val bytes = if (models.exists())
            models.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        else 0L
        val maxBytes = 90_000_000L
        check(bytes <= maxBytes) {
            "Shadow Core offline model assets exceed 90 MB: ${bytes} bytes"
        }
        println("Shadow Core model assets: ${bytes} of ${maxBytes} bytes allowed.")
    }
}

/**
 * Presupuesto total de los recursos de Shadow Core AI y su catálogo offline.
 * No es un límite de RAM ni una medición del código compilado en classes.dex.
 * La medición de la APK se realiza por separado en CI.
 */
tasks.register("verifyOfflineAiResourceBudget") {
    group = "verification"
    description = "Counts model weights, research indexes, approved data and audit metadata"
    doLast {
        val assets = file("src/main/assets")
        val included = if (assets.exists()) {
            assets.walkTopDown().filter { it.isFile }.toList()
        } else emptyList()
        val bytes = included.sumOf { it.length() }
        val limit = 90_000_000L
        check(bytes <= limit) {
            "Offline AI resources exceed the 90 MB storage ceiling: $bytes bytes"
        }
        val largest = included.sortedByDescending { it.length() }.take(5)
        println("Shadow Core AI assets: $bytes / $limit bytes (${included.size} files)")
        largest.forEach { println("  ${it.name}: ${it.length()} bytes") }
        println("Scope: APK assets only. DEX, system libraries and RAM require device tests.")
    }
}
