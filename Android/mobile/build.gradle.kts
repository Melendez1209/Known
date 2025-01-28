plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.1.10-RC2-1.0.29"
}
android {
    namespace = "com.melendez.known"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.melendez.known"
        minSdk = 29
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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

    val composeBom = "2025.01.00"
    val ui = "1.8.0-alpha08"
    val material = "1.4.0-alpha06"
    val accompanist = "0.36.0"
    val lifecycle = "2.9.0-alpha08"
    val room = "2.7.0-alpha12"

    implementation("com.tencent:mmkv:2.0.2")

    implementation("androidx.core:core-ktx:1.16.0-alpha01")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("me.zhanghai.compose.preference:library:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("com.google.android.material:material:1.13.0-alpha10")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")

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

    implementation("com.google.accompanist:accompanist-navigation-animation:$accompanist")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanist")

    implementation(platform("androidx.compose:compose-bom:$composeBom"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$ui")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBom"))
    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha03")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha03")

    debugImplementation("androidx.compose.ui:ui-tooling:$ui")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$ui")

    testImplementation("junit:junit:4.13.2")
}