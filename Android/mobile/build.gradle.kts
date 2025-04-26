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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    val activity = "1.11.0-rc01"
    val composeBom = "2025.04.01"
    val ui = "1.9.0-alpha01"
    val material = "1.4.0-alpha13"
    val accompanist = "0.36.0"
    val lifecycle = "2.9.0-rc01"
    val room = "2.7.1"

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.android.material:material:1.13.0-alpha13")
    implementation("androidx.navigation:navigation-compose:2.9.0-rc01")

    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth-ktx:23.2.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.4")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.1")
    implementation("com.google.android.gms:play-services-ads:24.2.0")

    implementation("androidx.activity:activity-ktx:${activity}")
    implementation("androidx.activity:activity-compose:${activity}")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle")

    implementation("androidx.compose.ui:ui:$ui")
    implementation("androidx.compose.animation:animation:$ui")
    implementation("androidx.compose.ui:ui-graphics:$ui")
    implementation("androidx.compose.ui:ui-tooling-preview:$ui")
    implementation("androidx.compose.runtime:runtime-livedata:$ui")

    implementation("androidx.room:room-runtime:$room")
    ksp("androidx.room:room-compiler:$room")
    implementation("androidx.room:room-ktx:$room")

    implementation("androidx.compose.material3:material3:$material")
    implementation("androidx.compose.material3:material3-window-size-class:$material")

    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanist")
    implementation("com.google.accompanist:accompanist-pager-indicators:${accompanist}")

    implementation(platform("androidx.compose:compose-bom:$composeBom"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$ui")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBom"))
    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha03")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha03")

    debugImplementation("androidx.compose.ui:ui-tooling:$ui")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$ui")

    testImplementation("junit:junit:4.13.2")
}