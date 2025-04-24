
// record: ./gradlew paparazziPreviews
// verify: ./gradlew paparazziPreviews -Pverify=true
tasks.register("paparazziPreviews") {
    description = "Records/verifies screenshots with Paparazzi for ComposablePreview tests"
    group = "Verification"

    tasks.findByName("compileDebugScreenshotTestKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileDebugAndroidTestKotlin")?.let { dependsOn(it) }

    doLast {
        val verifyProperty = project.findProperty("verify")?.toString()
        val isVerify = verifyProperty?.equals("true", ignoreCase = true) ?: false

        val command = if (isVerify) "verify" else "record"
        // Use the Gradle exec API to run a gradle command programmatically
        project.exec {
            workingDir(project.rootDir)

            // Build the command with proper executable for the OS
            val isWindows = System.getProperty("os.name").lowercase().contains("windows")
            val gradleCommand = if (isWindows) "gradlew.bat" else "./gradlew"

            commandLine(
                gradleCommand,
                ":tests:${command}PaparazziDebug",
                "--tests",
                "sergio.sastre.composable.preview.scanner.tests.paparazzi.*",
                "-Plibrary=paparazzi"
            )
        }
    }

    // This makes the cleanTestsBuildFolder task run after this task completes
    finalizedBy("cleanTestsBuildFolder")
}