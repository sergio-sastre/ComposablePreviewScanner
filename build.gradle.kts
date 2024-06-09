// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //id("sergio.sastre.composable.preview.plugin.ScanResultStore") version "1.0.0"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}