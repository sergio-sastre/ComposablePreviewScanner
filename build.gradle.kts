// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.compose) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.testify) apply false
    id("com.vanniktech.maven.publish") version "0.29.0" apply false
    id("com.gradleup.nmcp") version "0.0.8" apply false
}