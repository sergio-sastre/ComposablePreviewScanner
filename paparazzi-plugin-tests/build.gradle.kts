plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)

    // These 2 are needed for screenshot testing
    alias(libs.plugins.paparazzi)
    id("io.github.sergio-sastre.composable-preview-scanner.paparazzi")
}

android {
    namespace = "sergio.sastre.composable.preview.scanner.paparazzi.plugin"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

// Execute ./gradlew :paparazzi-plugin-tests:recordPaparazziDebug
composablePreviewPaparazzi {
    enable = true
    packages = listOf("sergio.sastre.composable.preview.scanner.paparazzi.plugin")
    includePrivatePreviews = true
    testClassName = "GeneratedPaparazziTests"
    testPackageName = "preview.generated"
}

dependencies {
    // Composable preview scanner
    testImplementation(project(":android"))
    testImplementation(project(":jvm"))
    testImplementation(libs.test.parameter.injector)

    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
}