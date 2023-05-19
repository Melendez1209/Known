plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.melendez.known"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.melendez.known"
        minSdk = 29
        targetSdk = 33
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

    implementation("androidx.core:core-ktx:1.11.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui:1.5.0-alpha03")
    implementation("androidx.compose.ui:ui-graphics:1.5.0-alpha03")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0-alpha03")
    implementation("androidx.compose.material3:material3:1.1.0-beta02")
    implementation("androidx.compose.material:material-icons-extended:1.5.0-alpha01")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.0-beta02")
    implementation("androidx.navigation:navigation-compose:2.6.0-alpha08")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.31.2-alpha")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0-alpha03")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0-alpha03")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.0-alpha03")
}