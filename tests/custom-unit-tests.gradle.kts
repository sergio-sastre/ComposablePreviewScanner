// Run all unit tests:
// ./gradlew :tests:testApi ; ./gradlew :tests:testSourceSets
tasks.register<Test>("testApi") {
    description = "Runs tests for ComposablePrevewScanner API"
    group = "Verification"
    maxHeapSize = "4g"
    maxParallelForks = 2

    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.tests.api.main.*")
    }

    testLogging {
        events("passed", "skipped", "failed", "standardOut")
    }
}

tasks.register<Test>("testSourceSets") {
    description = "Runs tests for ComposablePrevewScanner source sets"
    group = "Verification"
    maxHeapSize = "4g"
    maxParallelForks = 1

    // source sets classes need to exist before executing these tests, otherwise they're skipped
    tasks.findByName("compileReleaseKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileDebugScreenshotTestKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileReleaseScreenshotTestKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileDebugAndroidTestKotlin")?.let { dependsOn(it) }

    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.tests.api.sourcesets.*")
    }

    testLogging {
        events("passed", "skipped", "failed", "standardOut")
    }

    // This ensures compiled classes for SourceSets Tests are deleted
    finalizedBy("cleanTestsBuildFolder")
}

tasks.register<Delete>("cleanTestsBuildFolder") {
    description = "Deletes the build directory of the tests module"
    group = "Cleanup"

    // only delete the build dirs where the compiled classes are located
    delete(
        project.layout.buildDirectory.dir("tmp/kotlin-classes"),
        project.layout.buildDirectory.dir("intermediates/kotlinc"),
        project.layout.buildDirectory.dir("intermediates/built_in_kotlinc")
    )
}