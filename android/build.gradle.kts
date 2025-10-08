plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    id("maven-publish")
}

android {
    namespace = "sergio.sastre.composable.preview.scanner.android"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.appcompat)
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
            artifactId = "android"
            version = "0.7.1"
        }
    }
}