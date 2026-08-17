plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.metalava)
    id("maven-publish")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "sergio.sastre.composable.preview.scanner.glance"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // Removed compileOptions and kotlinOptions as they are handled by jvmToolchain

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api(project(":core"))
    api(libs.androidx.glance.appwidget)
    implementation(libs.classgraph)
    implementation(libs.kotlin.reflect)
}

//https://www.talentica.com/blogs/publish-your-android-library-on-jitpack-for-better-reachability/
publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "glance"
            version = "0.9.3"
        }
    }
}