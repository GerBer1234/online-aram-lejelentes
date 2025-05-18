plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.emeter"

    // Használt SDK verzió beállítása
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.emeter"

        // Minimum és target SDK verziók
        minSdk = 24
        targetSdk = 35

        // Verzióinformációk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase szolgáltatásokhoz
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Helymeghatározáshoz
    implementation(libs.play.services.location)
}

// Firebase szolgáltatások inicializálása
apply(plugin = "com.google.gms.google-services")
