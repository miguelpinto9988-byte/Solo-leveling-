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
        versionCode = 5
        versionName = "0.5.0"
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
    description = "Rejects offline model assets larger than 60 MiB"
    doLast {
        val models = file("src/main/assets/shadow_core")
        val bytes = if (models.exists())
            models.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        else 0L
        val maxBytes = 60L * 1024L * 1024L
        check(bytes <= maxBytes) {
            "Shadow Core offline model assets exceed 60 MiB: ${bytes} bytes"
        }
        println("Shadow Core model assets: ${bytes} of ${maxBytes} bytes allowed.")
    }
}
