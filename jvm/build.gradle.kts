plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "sergio.sastre.composable.preview.scanner.jvm"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        // Enables Jetpack Compose for this module
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
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
    implementation(libs.androidx.compose.runtime)
    implementation (libs.classgraph)
}

//https://www.talentica.com/blogs/publish-your-android-library-on-jitpack-for-better-reachability/
publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "jvm"
            version = "0.1.2"
        }
    }
}