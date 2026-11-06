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

            // Allow filtering by task shortcut or command-line property
            val requestedTasks = gradle.startParameter.taskNames
            val taskFilter = when {
                requestedTasks.any { it.endsWith("benchmarkMainSourceSet") } -> "MainSourceSetBenchmark"
                requestedTasks.any { it.endsWith("benchmarkBuildTimeWithoutPrivatePreviews") } -> "BuildTimeWithoutPrivatePreviewsBenchmark"
                requestedTasks.any { it.endsWith("benchmarkBuildTime") } -> "BuildTimeBenchmark"
                else -> null
            }

            val filterPatterns = buildList {
                if (taskFilter != null) {
                    add(taskFilter)
                }
                val includeProp = (project.findProperty("include")
                    ?: project.findProperty("benchmark")
                    ?: project.findProperty("filter")) as? String
                if (!includeProp.isNullOrBlank()) {
                    addAll(includeProp.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() })
                }
            }

            filterPatterns.distinct().forEach { pattern ->
                include(pattern)
            }

            val excludeProp = (project.findProperty("exclude")
                ?: project.findProperty("excludeBenchmark")) as? String
            if (!excludeProp.isNullOrBlank()) {
                excludeProp.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }.forEach { pattern ->
                    exclude(pattern)
                }
            }

            // Optional iteration overrides for quick local testing (e.g. -Piterations=1 -Pwarmups=1)
            (project.findProperty("iterations") as? String)?.toIntOrNull()?.let { iterations = it }
            (project.findProperty("warmups") as? String)?.toIntOrNull()?.let { warmups = it }
            (project.findProperty("iterationTime") as? String)?.toLongOrNull()?.let {
                iterationTime = it
                iterationTimeUnit = (project.findProperty("iterationTimeUnit") as? String) ?: "s"
            }
        }
    }
    targets {
        register("main")
    }
}

// Shortcut tasks to run specific benchmarks directly
tasks.register("benchmarkMainSourceSet") {
    group = "benchmark"
    description = "Runs only the MainSourceSet benchmark"
    dependsOn("benchmark")
}

tasks.register("benchmarkBuildTime") {
    group = "benchmark"
    description = "Runs only the BuildTime benchmark (includes private previews)"
    dependsOn("benchmark")
}

tasks.register("benchmarkBuildTimeWithoutPrivatePreviews") {
    group = "benchmark"
    description = "Runs only the BuildTime benchmark without private previews"
    dependsOn("benchmark")
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
    runtimeOnly(files(rootProject.file("custompreviews/build/tmp/kotlin-classes/debug")))
}

apply(from = "benchmark-reporter.gradle.kts")
