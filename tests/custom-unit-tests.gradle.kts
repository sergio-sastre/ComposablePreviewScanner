
// Run all unit tests: ./gradlew :tests:testLogic ; ./gradlew :tests:testSourceSets
tasks.register<Test>("testLogic"){
    description = "Runs tests for ComposablePrevewScanner logic"
    group = "Verification"
    maxHeapSize = "4g"

    // Specify the test class directories and classpath
    // Inherit configuration from the testDebugUnitTest task
    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.tests.logic.main*")
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<Test>("testSourceSets"){
    description = "Runs tests for ComposablePrevewScanner source sets"
    group = "Verification"
    maxHeapSize = "4g"

    tasks.findByName("compileDebugScreenshotTestKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileReleaseScreenshotTestKotlin")?.let { dependsOn(it) }

    // Specify the test class directories and classpath
    // Inherit configuration from the testDebugUnitTest task
    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.tests.logic.sourcesets*")
    }

    testLogging {
        events("passed", "skipped", "failed")
    }

    // This makes the cleanTestsBuildFolder task run after this task completes
    finalizedBy("cleanTestsBuildFolder")
}

tasks.register<Delete>("cleanTestsBuildFolder") {
    description = "Deletes the build directory of the tests module"
    group = "Cleanup"
    delete(layout.buildDirectory)
}