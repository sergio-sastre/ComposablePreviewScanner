plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinx.benchmark)
}

kotlin {
    jvmToolchain(17)
}

benchmark {
    configurations {
        getByName("main") {
            reportFormat = "json"
        }
    }
    targets {
        register("main")
    }
}

dependencies {
    implementation(project(":android"))
    implementation(project(":core"))
    implementation(libs.kotlinx.benchmark.runtime)
    implementation(libs.classgraph)
    implementation(libs.kotlinx.collections.immutable)

    // Compose dependencies required for MethodFinder reflection on @Composable functions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-unit")
    implementation(libs.androidx.ui.tooling.preview)

    // Android framework JVM stubs (required for reflection on Android classes like Path/Context)
    compileOnly("org.robolectric:android-all:14-robolectric-10818077")
    runtimeOnly("org.robolectric:android-all:14-robolectric-10818077")

    // Ensure scanned classes from :tests and :custompreviews are available on the classpath
    runtimeOnly(files(rootProject.file("tests/build/tmp/kotlin-classes/debug")))
    runtimeOnly(files(rootProject.file("tests/build/tmp/kotlin-classes/debugAndroidTest")))
    runtimeOnly(files(rootProject.file("tests/build/kotlin/compileDebugAndroidTestKotlin")))
    runtimeOnly(files(rootProject.file("custompreviews/build/tmp/kotlin-classes/debug")))
}

apply(from = "benchmark-reporter.gradle.kts")
