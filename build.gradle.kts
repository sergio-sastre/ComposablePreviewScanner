// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) version "8.5.2" apply false
    alias(libs.plugins.android.library)  version "8.5.2" apply false
    alias(libs.plugins.jetbrains.kotlin.android) version "1.9.24" apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) version "1.9.24" apply false
    alias(libs.plugins.jetbrains.compose) version "1.6.11" apply false
    alias(libs.plugins.roborazzi) version "1.22.0" apply false
    alias(libs.plugins.paparazzi) version "1.3.4" apply false
    alias(libs.plugins.testify) version "3.2.0" apply false
}
