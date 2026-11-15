plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.studio.launcher"
    compileSdk = 35
    signingConfigs {
        getByName("debug") {
            // Official builds are signed with a private key supplied by GitHub secrets (never stored in the repo).
            val keyFile = System.getenv("KEYSTORE_FILE")
            if (keyFile != null && file(keyFile).exists()) {
                storeFile = file(keyFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
            // Without it (for example on your own computer) Gradle uses the normal local debug key.
        }
        // The release build is signed with the same private key, so it installs over earlier versions.
        create("release") {
            val keyFile = System.getenv("KEYSTORE_FILE")
            if (keyFile != null && file(keyFile).exists()) {
                storeFile = file(keyFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            // Not debuggable. Code shrinking is left off on purpose, so nothing can break at run time.
            isMinifyEnabled = false
            val keyFile = System.getenv("KEYSTORE_FILE")
            signingConfig = if (keyFile != null && file(keyFile).exists()) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    lint { checkReleaseBuilds = false }
    defaultConfig {
        applicationId = "com.kartproduction.kauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 320
        versionName = "4.10"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.camera:camera-video:1.3.4")
}
