plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.melendez.known"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.melendez.known"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            buildConfigField("String", "apiKey", """"AIzaSyCPValw3nAxCl6eI60pCf88jdNM9PVtEF4"""")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
        }
        debug {
            buildConfigField("String", "apiKey", """"AIzaSyCPValw3nAxCl6eI60pCf88jdNM9PVtEF4"""")
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

    val composeBom = "2023.10.01"
    val ui = "1.6.0-beta02"
    val material = "1.2.0-alpha12"
    val accompanist = "0.33.2-alpha"
    val lifecycle = "2.7.0-rc02"
    val room = "2.6.1"

    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.core:core-ktx:1.13.0-alpha02")
    implementation("androidx.activity:activity-compose:1.9.0-alpha01")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.1.1")
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.compose.ui:ui:$ui")
    implementation("androidx.compose.ui:ui-graphics:$ui")
    implementation("androidx.compose.ui:ui-tooling-preview:$ui")
    implementation("androidx.compose.runtime:runtime-livedata:$ui")
    implementation("androidx.compose.material:material-icons-extended:$ui")
    implementation("androidx.compose.material3:material3:$material")
    implementation("androidx.compose.material3:material3-window-size-class:$material")
    implementation("com.google.accompanist:accompanist-navigation-animation:$accompanist")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanist")
    implementation(platform("androidx.compose:compose-bom:$composeBom"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$ui")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBom"))
    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha01")

    debugImplementation("androidx.compose.ui:ui-tooling:$ui")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$ui")

    testImplementation("junit:junit:4.13.2")
}