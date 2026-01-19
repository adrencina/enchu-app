import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.adrencina.enchu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adrencina.enchu"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            }
            keyAlias = keystoreProperties["keyAlias"]?.toString()
            keyPassword = keystoreProperties["keyPassword"]?.toString()
            storeFile = keystoreProperties["storeFile"]?.toString()?.let { file(it) }
            storePassword = keystoreProperties["storePassword"]?.toString()
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            ndk.debugSymbolLevel = "full"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            pickFirsts.addAll(listOf(
                "lib/arm64-v8a/libsqlcipher.so",
                "lib/armeabi-v7a/libsqlcipher.so",
                "lib/x86/libsqlcipher.so",
                "lib/x86_64/libsqlcipher.so"
            ))
            useLegacyPackaging = true
        }
    }
}

dependencies {

    // Core de Android y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Firebase BOM (Bill of Materials)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.appcheck)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.google.services.auth)


    // Navegación en Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt (Inyección de Dependencias)
    implementation(libs.hilt.android)
    implementation(libs.google.firebase.appcheck.playintegrity)
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler) // <-- ESTA ES LA LÍNEA NUEVA
    implementation(libs.androidx.hilt.navigation.compose)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    // Startup
    implementation(libs.androidx.startup.runtime)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // Room (Base de datos local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt(libs.room.compiler)

    // Cifrado de Base de Datos y Keystore
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.security.crypto)

    // Coil (Carga de imágenes)
    implementation(libs.coil.compose)
    implementation(libs.androidx.exifinterface)

    // Dependencias de Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // DataStore
    implementation(libs.datastore.preferences)

    // In-App Updates
    implementation(libs.app.update.ktx)
}

kapt {
    correctErrorTypes = true
}