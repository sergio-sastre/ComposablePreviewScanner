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
    namespace = "sergio.sastre.composable.preview.scanner.wear"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
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
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.wear.compose:compose-material:1.4.0")
    implementation("androidx.wear.compose:compose-ui-tooling:1.4.0")
    api("androidx.wear.tiles:tiles-tooling-preview:1.6.2")
    debugImplementation("androidx.wear.tiles:tiles-tooling:1.6.2")
    implementation("androidx.wear.tiles:tiles-renderer:1.6.2")
    implementation("androidx.wear:wear-tooling-preview:1.0.0")
    implementation("androidx.wear.protolayout:protolayout:1.2.1")
    implementation("androidx.wear.protolayout:protolayout-material:1.2.1")
    implementation("androidx.wear.protolayout:protolayout-expression-pipeline:1.2.1")
    
    implementation(libs.classgraph)
    implementation(libs.androidx.compose.runtime)
    implementation("androidx.compose.ui:ui:1.7.0")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "wear"
            version = "0.10.0"
        }
    }
}
