plugins {
    kotlin("jvm") version "2.0.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("composablePreviewPaparazziPlugin") {
            id = "io.github.sergio-sastre.composable-preview-scanner.paparazzi-plugin"
            implementationClass =
                "io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin.ComposablePreviewPaparazziPlugin"
            displayName = "Composable Preview Paparazzi Generator"
            description =
                "A Gradle plugin that generates and executes Paparazzi test files for screenshot testing Composable Previews"
        }
    }
}

dependencies {
    implementation(gradleApi())

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}