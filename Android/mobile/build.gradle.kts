plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.melendez.known"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.melendez.known"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            buildConfigField("String", "apiKey", """"AIzaSyCI8YN38esP9is0vEAw3NkmwySSBYb1GqM"""")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
        }
        debug {
            buildConfigField("String", "apiKey", """"AIzaSyCI8YN38esP9is0vEAw3NkmwySSBYb1GqM"""")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.volley)
    implementation(libs.generativeai)
    implementation(libs.material.icons.extended)
    implementation(libs.material)
    implementation(libs.navigation.compose)

    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.ui.auth)
    implementation(libs.play.services.ads)

    implementation(libs.activity.ktx)
    implementation(libs.activity.compose)

    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.process)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.animation)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.runtime.livedata)
    implementation(libs.material3)
    implementation(libs.material3.window.size.class1)

    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    testImplementation(libs.junit.jupiter.api)
}