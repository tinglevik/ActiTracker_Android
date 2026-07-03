plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.actitracker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.actitracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {

    // -----------------------------
    // Compose BOM
    // -----------------------------
    implementation(platform(libs.compose.bom))
    implementation(libs.core.ktx)
    implementation(libs.foundation.layout)
    androidTestImplementation(platform(libs.compose.bom))

    // -----------------------------
    // Core Android
    // -----------------------------
    implementation(libs.appcompat)
    implementation(libs.material)

    // -----------------------------
    // Compose
    // -----------------------------
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // -----------------------------
    // Lifecycle
    // -----------------------------
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // -----------------------------
    // Navigation
    // -----------------------------
    implementation(libs.navigation.compose)

    // -----------------------------
    // Room
    // -----------------------------
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // -----------------------------
    // DataStore (for settings and timer state)
    // -----------------------------
    implementation(libs.datastore.preferences)

    // -----------------------------
    // Charts
    // -----------------------------
    implementation(libs.mpandroidchart)

    // -----------------------------
    // Icons
    // -----------------------------
    implementation(libs.material.icons.extended)

    // -----------------------------
    // Serialization
    // -----------------------------
    implementation(libs.gson)

    // -----------------------------
    // Additional UI
    // -----------------------------
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // -----------------------------
    // Debug
    // -----------------------------
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // -----------------------------
    // Tests
    // -----------------------------
    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

}