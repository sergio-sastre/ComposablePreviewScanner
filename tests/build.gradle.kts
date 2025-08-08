plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
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
    compileSdk = 35

    defaultConfig {
        applicationId = "composable.preview.scanner"
        minSdk = 23
        // Needs to use any targetSdk for glance to correctly render Previews without "widthDp"
        // when using Roborazzi or instrumentation testing libs
        // Paparazzi cannot handle this
        targetSdk = 35
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
                java.srcDir("src/screenshotTest/java") // "src/androidTest/java")
                res.srcDir("src/screenshotTest/res")   // "src/androidTest/res")
            }
        }
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
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
    implementation(project(":common"))
    implementation(project(":custompreviews"))
    implementation(project(":glance"))
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.junit.ktx)

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
    debugImplementation(libs.android.ui.testing.utils)
    androidTestImplementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.android.testify)
}
