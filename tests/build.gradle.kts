plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.testify)
}

// Apply conditionally via CLI to avoid plugin clashes
if (project.hasProperty("library")) {
    when (project.property("library")) {
        "roborazzi" -> apply(plugin = "io.github.takahirom.roborazzi")
        "paparazzi" -> apply(plugin = "app.cash.paparazzi")
    }
}

apply(from = "custom-unit-tests.gradle.kts")
apply(from = "custom-paparazzi-tests.gradle.kts")
apply(from = "custom-roborazzi-tests.gradle.kts")

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

    val includeScreenshotTests = project.hasProperty("includeSourceSetScreenshotTest")
    if (includeScreenshotTests) {
        sourceSets {
            getByName("androidTest") {
                java.srcDir("src/screenshotTest/java")//, "src/androidTest/java")
                res.srcDir("src/screenshotTest/res")//"src/androidTest/res")
            }
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
        all { test ->
            test.jvmArgs("-Xmx4g")
            test.testLogging { showStandardStreams = true }
        }
    }
}

testify {
    moduleName = ":tests"
    applicationPackageId = "composable.preview.scanner"
}

dependencies {
    implementation(project(":android"))
    implementation(project(":jvm"))
    implementation(project(":custompreviews"))
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    screenshotTestImplementation(libs.kotlinx.collections.immutable)
    debugImplementation(libs.kotlinx.collections.immutable) {
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

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.android.ui.testing.utils)
    androidTestImplementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.android.testify)
}