// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android Gradle plugin
    alias(libs.plugins.android.application) apply false
    // Kotlin Android plugin
    alias(libs.plugins.kotlin.android) apply false
    // Google Services plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}