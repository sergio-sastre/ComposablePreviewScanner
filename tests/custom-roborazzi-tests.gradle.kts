
// record: ./gradlew roborazziPreviewsSourceSet
// verify: ./gradlew roborazziPreviewsSourceSet -Pverify=true
tasks.register("roborazziPreviewsSourceSet") {
    description = "Records/verifies screenshots with Roborazzi for ComposablePreview tests from compiled classes"
    group = "Verification"

    // For the sourceTests
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
                ":tests:${command}RoborazziDebug",
                "--tests",
                "sergio.sastre.composable.preview.scanner.tests.roborazzi.sourceset.*",
                "-Plibrary=roborazzi",
            )
        }
    }

    // This makes the cleanTestsBuildFolder task run after this task completes
    finalizedBy("cleanTestsBuildFolder")
}

// record: ./gradlew roborazziPreviewsRuntime
// verify: ./gradlew roborazziPreviewsRuntime -Pverify=true
tasks.register("roborazziPreviewsRuntime") {
    description = "Records/verifies screenshots with Roborazzi for ComposablePreview tests at runtime"
    group = "Verification"

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
                ":tests:${command}RoborazziDebug",
                "--tests",
                "sergio.sastre.composable.preview.scanner.tests.roborazzi.runtime.*",
                "-Plibrary=roborazzi",
                "-PmaxParallelForks=2"
            )
        }
    }

    // This makes the cleanTestsBuildFolder task run after this task completes
    finalizedBy("cleanTestsBuildFolder")
}
