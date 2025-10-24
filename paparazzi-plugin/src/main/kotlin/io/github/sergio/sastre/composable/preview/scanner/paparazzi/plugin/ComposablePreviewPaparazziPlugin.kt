package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposablePreviewPaparazziPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create and configure the extension
        val extension = project.extensions.create(
            "composablePreviewPaparazzi",
            ComposablePreviewPaparazziExtension::class.java,
            project.objects
        )

        // Set default values
        extension.enable.convention(false)
        extension.packages.convention(emptyList())
        extension.includePrivatePreviews.convention(false)
        extension.testClassName.convention("GeneratedComposablePreviewPaparazziTests")
        extension.testPackageName.convention("generated.paparazzi.tests")
        // Do not set a convention for numOfShards here; we will derive it from Gradle's Test.maxParallelForks later.

        // Configure the task after project evaluation
        project.afterEvaluate {
            // Default numOfShards to Gradle Test.maxParallelForks (users can still override via extension)
            val tests = project.tasks.withType(org.gradle.api.tasks.testing.Test::class.java)
            val maxForks = tests.findByName("test")?.maxParallelForks
                ?: tests.maxOfOrNull { it.maxParallelForks } ?: 1
            extension.numOfShards.convention(maxForks)

            if (extension.enable.get()) {
                setupGenerateComposablePreviewPaparazziTestsTask(project, extension)
            }
        }
    }
}