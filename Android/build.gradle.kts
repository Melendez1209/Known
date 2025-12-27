// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.1.0-alpha01" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
}
buildscript {
    repositories {
        google()  // Google's Maven repository
    }
    dependencies {
        classpath(libs.google.services)
    }
}
