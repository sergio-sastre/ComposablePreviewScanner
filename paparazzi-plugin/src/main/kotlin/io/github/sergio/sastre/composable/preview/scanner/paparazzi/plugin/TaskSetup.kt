package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.testing.Test

fun setupGenerateComposablePreviewPaparazziTestsTask(
    project: Project,
    extension: ComposablePreviewPaparazziExtension
) {
    // Validate configuration
    if (extension.packages.get().isEmpty()) {
        throw IllegalArgumentException(
            "Please set 'packages' in the composablePreviewPaparazzi extension. " +
                    "Example: composablePreviewPaparazzi { packages = listOf(\"com.example.previews\") }"
        )
    }

    // Register the task
    val generateTestsTask = project.tasks.register(
        "generateComposablePreviewPaparazziTests",
        GenerateComposablePreviewPaparazziTestsTask::class.java
    ) { task ->
        task.group = "verification"
        task.description = "Generates Paparazzi test files for screenshot testing Composable Previews"

        // Generate in src/test/kotlin instead of build directory
        task.outputDir.set(project.projectDir.resolve("src/test/kotlin"))
        task.scanPackageTrees.set(extension.packages)
        task.includePrivatePreviews.set(extension.includePrivatePreviews)
        task.testClassName.set(extension.testClassName)
        task.testPackageName.set(extension.testPackageName)
    }

    // The tests are now generated directly in src/test/kotlin, so no need to add source directories
    project.afterEvaluate {
        project.logger.info("Generated tests will be placed in src/test/kotlin")

        // Make all compile and test tasks depend on the generation task
        project.tasks.withType(AbstractCompile::class.java).configureEach { compileTask ->
            if (compileTask.name.contains("Test", ignoreCase = true)) {
                compileTask.dependsOn(generateTestsTask)
            }
        }

        project.tasks.withType(Test::class.java).configureEach { testTask ->
            testTask.dependsOn(generateTestsTask)
        }

        // Also handle Kotlin compile tasks specifically
        project.tasks.configureEach { task ->
            if (task.name.contains("compileTestKotlin") || task.name.contains("compileDebugUnitTestKotlin")) {
                task.dependsOn(generateTestsTask)
            }
        }
    }
}