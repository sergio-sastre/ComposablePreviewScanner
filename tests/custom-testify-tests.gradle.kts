import javax.inject.Inject
import org.gradle.process.ExecOperations

interface TestifyInjectedExecOps {
    @get:Inject
    val execOperations: ExecOperations
}

val testifyInjectedExecOps = project.objects.newInstance<TestifyInjectedExecOps>()

tasks.register<Test>("saveScanResultInAssets") {
    description = "Dumps scan results into androidTest assets for instrumentation screenshot tests"
    group = "Verification"
    maxHeapSize = "4g"

    // Always re-run and track outputs so Gradle VFS sees the generated JSONs immediately
    outputs.files(
        project.file("src/androidTest/assets/scan_result_main_android.json"),
        project.file("src/androidTest/assets/scan_result_main_glance.json"),
        project.file("src/androidTest/assets/scan_result_main_wear.json"),
        project.file("src/androidTest/assets/scan_result_screenshot_test.json"),
        project.file("src/androidTest/assets/scan_result_android_test.json"),
        project.file("src/androidTest/assets/custom_previews.json")
    )
    outputs.upToDateWhen { false }

    // Source set classes must exist before dumping scan results
    tasks.findByName("compileDebugKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileDebugScreenshotTestKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileDebugAndroidTestKotlin")?.let { dependsOn(it) }

    val testDebugTask = tasks.getByName("testDebugUnitTest") as Test
    testClassesDirs = testDebugTask.testClassesDirs
    classpath = testDebugTask.classpath

    filter {
        includeTestsMatching("sergio.sastre.composable.preview.scanner.run_before_instrumentation_tests.SaveScanResultInAssets")
    }

    testLogging {
        events("passed", "skipped", "failed", "standardOut")
    }
}

val screenshotsPkg = "sergio.sastre.composable.preview.scanner.screenshots"

fun runTestifyGmd(
    gmdTask: String,
    testClass: String,
    defaultShards: Int? = null,
) {
    val verifyProperty = project.findProperty("verify")?.toString()
    val isVerify = verifyProperty?.equals("true", ignoreCase = true) ?: false
    val rawNumShards = if (defaultShards != null) {
        project.findProperty("numShards")?.toString()?.toIntOrNull() ?: defaultShards
    } else {
        null
    }
    val numShards = rawNumShards?.let { minOf(it, 2) }

    val gpu = project.findProperty("android.testoptions.manageddevices.emulator.gpu")?.toString()

    val commonArgs = buildList {
        add("-PincludeSourceSetScreenshotTest")
        add("-PuseTestStorage")
        add("-Pverify=$isVerify")
        if (gpu != null) {
            add("-Pandroid.testoptions.manageddevices.emulator.gpu=$gpu")
        }
        if (numShards != null && numShards > 1) {
            add("-Pandroid.experimental.androidTest.numManagedDeviceShards=$numShards")
        }
    }

    testifyInjectedExecOps.execOperations.exec {
        workingDir(project.rootDir)
        commandLine(
            listOf("./gradlew", gmdTask) +
                commonArgs +
                "-Pandroid.testInstrumentationRunnerArguments.class=$testClass"
        )
    }

    if (!isVerify) {
        val possibleRoots = listOf(
            project.layout.buildDirectory.dir("outputs/managed_device_android_test_additional_output").get().asFile,
            project.layout.buildDirectory.dir("intermediates/managed_device_android_test_additional_output").get().asFile
        )

        val targetScreenshotsDir = project.file("src/androidTest/assets/screenshots")
        possibleRoots.forEach { root ->
            if (root.exists()) {
                root.walkTopDown()
                    .filter { it.isDirectory && it.name == "screenshots" && it.parentFile?.name == "images" }
                    .forEach { screenshotsDir ->
                        screenshotsDir.copyRecursively(targetScreenshotsDir, overwrite = true)
                    }
            }
        }
    }
}

// record: ./gradlew testifyAndroidPreviews [-PnumShards=2]
// verify: ./gradlew testifyAndroidPreviews -Pverify=true [-PnumShards=2]
tasks.register("testifyAndroidPreviews") {
    description = "Records/verifies Android Composable screenshots with Android-Testify on Phone GMD (pixel3api34, 2 shards)"
    group = "Verification"

    dependsOn("saveScanResultInAssets")

    doLast {
        runTestifyGmd(
            gmdTask = ":tests:pixel3api34DebugAndroidTest",
            testClass = "$screenshotsPkg.TestifyComposablePreviewsTest",
            defaultShards = 2
        )
    }

    finalizedBy("cleanTestsBuildFolder")
}

// record: ./gradlew testifyGlancePreviews
// verify: ./gradlew testifyGlancePreviews -Pverify=true
tasks.register("testifyGlancePreviews") {
    description = "Records/verifies Glance Composable screenshots with Android-Testify on Phone GMD (pixel3api34)"
    group = "Verification"

    dependsOn("saveScanResultInAssets")

    doLast {
        runTestifyGmd(
            gmdTask = ":tests:pixel3api34DebugAndroidTest",
            testClass = "$screenshotsPkg.TestifyGlancePreviewsTest",
            defaultShards = null
        )
    }

    finalizedBy("cleanTestsBuildFolder")
}

// record: ./gradlew testifyWearTilePreviews
// verify: ./gradlew testifyWearTilePreviews -Pverify=true
tasks.register("testifyWearTilePreviews") {
    description = "Records/verifies Wear Tile screenshots with Android-Testify on Wear OS GMD (wearoslargeapi34)"
    group = "Verification"

    dependsOn("saveScanResultInAssets")

    doLast {
        runTestifyGmd(
            gmdTask = ":tests:wearoslargeapi34DebugAndroidTest",
            testClass = "$screenshotsPkg.TestifyWearTilePreviewsTest",
            defaultShards = null
        )
    }

    finalizedBy("cleanTestsBuildFolder")
}

tasks.register("testifyWearPreviews") {
    description = "Alias for testifyWearTilePreviews"
    group = "Verification"
    dependsOn("testifyWearTilePreviews")
}

// record: ./gradlew testifyPreviews
// verify: ./gradlew testifyPreviews -Pverify=true
tasks.register("testifyPreviews") {
    description = "Records/verifies all screenshots (Android, Glance, Wear) with Android-Testify"
    group = "Verification"

    dependsOn("saveScanResultInAssets")

    doLast {
        runTestifyGmd(
            gmdTask = ":tests:pixel3api34DebugAndroidTest",
            testClass = "$screenshotsPkg.TestifyComposablePreviewsTest",
            defaultShards = 2
        )
        runTestifyGmd(
            gmdTask = ":tests:pixel3api34DebugAndroidTest",
            testClass = "$screenshotsPkg.TestifyGlancePreviewsTest",
            defaultShards = null
        )
        runTestifyGmd(
            gmdTask = ":tests:wearoslargeapi34DebugAndroidTest",
            testClass = "$screenshotsPkg.TestifyWearTilePreviewsTest",
            defaultShards = null
        )
    }

    finalizedBy("cleanTestsBuildFolder")
}

val scanningPkg = "sergio.sastre.composable.preview.scanner.scanning"

tasks.register("scanningInstrumentationTest") {
    description = "Runs instrumentation tests under the scanning package on Phone GMD (pixel3api34) without screenshotTest sourceSet"
    group = "Verification"

    dependsOn("saveScanResultInAssets")

    doLast {
        val numShards = project.findProperty("numShards")?.toString()?.toIntOrNull()
        val shardArgs = if (numShards != null && numShards > 1) {
            listOf("-Pandroid.experimental.androidTest.numManagedDeviceShards=$numShards")
        } else {
            emptyList()
        }

        testifyInjectedExecOps.execOperations.exec {
            workingDir(project.rootDir)
            commandLine(
                listOf(
                    "./gradlew",
                    ":tests:pixel3api34DebugAndroidTest",
                    "-Pandroid.testInstrumentationRunnerArguments.package=$scanningPkg"
                ) + shardArgs
            )
        }
    }

    finalizedBy("cleanTestsBuildFolder")
}
