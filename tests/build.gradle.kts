plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.screenshot)
}

// Apply conditionally via CLI to avoid plugin clashes
if (project.hasProperty("library")) {
    when (project.property("library")) {
        "roborazzi" -> apply(plugin = "io.github.takahirom.roborazzi")
        "paparazzi" -> apply(plugin = "app.cash.paparazzi")
    }
}

android {
    namespace = "sergio.sastre.composable.preview.scanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "composable.preview.scanner"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        android.buildFeatures.buildConfig = true
        buildConfigField("String", "BUILD_DIR_PATH", "\"${layout.buildDirectory.get().asFile.absolutePath}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    testOptions.unitTests {
        isIncludeAndroidResources = true
        all {
            it.jvmArgs("-Xmx2g")
        }
    }
}

dependencies {
    implementation(project(":android"))
    implementation(project(":jvm"))
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    screenshotTestImplementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
    debugImplementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8") {
        because("We would get NoClassFound exception when running Paparazzi and Roborazzi tests since it is used for ScreenshotImplementation")
    }

    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.classgraph)

    testImplementation(libs.junit)
    testImplementation(libs.test.parameter.injector)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi)
    testImplementation(libs.robolectric)
    testImplementation(libs.paparazzi)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}