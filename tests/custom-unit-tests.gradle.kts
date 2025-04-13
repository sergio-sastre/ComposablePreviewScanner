
// Run all unit tests:
// ./gradlew :tests:testApi ; ./gradlew :tests:testSourceSets
tasks.register<Test>("testApi"){
    description = "Runs tests for ComposablePrevewScanner API"
    group = "Verification"
    maxHeapSize = "4g"

    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.tests.api.main.*")
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<Test>("testSourceSets"){
    description = "Runs tests for ComposablePrevewScanner source sets"
    group = "Verification"
    maxHeapSize = "4g"

    // source sets classes need to exist before executing these tests, otherwise they're skipped
    tasks.findByName("compileDebugScreenshotTestKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileReleaseScreenshotTestKotlin")?.let { dependsOn(it) }

    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.tests.api.sourcesets.*")
    }

    testLogging {
        events("passed", "skipped", "failed")
    }

    // This ensures compiled classes for SourceSets Tests are deleted
    finalizedBy("cleanTestsBuildFolder")
}

tasks.register<Delete>("cleanTestsBuildFolder") {
    description = "Deletes the build directory of the tests module"
    group = "Cleanup"
    delete(layout.buildDirectory)
}