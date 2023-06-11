plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { isMinifyEnabled = true }
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("io.coil-kt:coil-compose:2.4.0")
    val composeBom = "2023.03.00"
    val ui = "1.5.0-beta01"
    val material = "1.1.0-beta02"
    val accompanist = "0.31.3-beta"

    implementation("androidx.core:core-ktx:1.11.0-beta01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
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

    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$ui")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBom"))

    debugImplementation("androidx.compose.ui:ui-tooling:$ui")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$ui")

    testImplementation("junit:junit:4.13.2")
}