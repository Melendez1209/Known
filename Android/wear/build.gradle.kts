plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.melendez.known"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.melendez.known"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    val composeBom = "2024.06.00"
    val compose = "1.4.0-beta03"
    val ui = "1.7.0-beta04"
    val material = "1.4.0-alpha05"
    val horologist = "0.6.22"

    implementation("com.google.android.gms:play-services-wearable:18.2.0")
    implementation(platform("androidx.compose:compose-bom:$composeBom"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.core:core-splashscreen:1.2.0-alpha01")
    implementation("androidx.compose.ui:ui:$ui")
    implementation("androidx.compose.ui:ui-tooling-preview:$ui")
    implementation("androidx.compose.material:material-icons-extended:$ui")
    implementation("androidx.wear.compose:compose-material:$compose")
    implementation("androidx.wear.compose:compose-foundation:$compose")
    implementation("androidx.wear.tiles:tiles:$material")
    implementation("androidx.wear.tiles:tiles-material:$material")
    implementation("com.google.android.horologist:horologist-compose-tools:$horologist")
    implementation("com.google.android.horologist:horologist-tiles:$horologist")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0-alpha03")

    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBom"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}