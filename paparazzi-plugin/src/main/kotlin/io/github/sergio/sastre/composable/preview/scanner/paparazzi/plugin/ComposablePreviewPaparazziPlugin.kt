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
        extension.numOfShards.convention(1)

        // Configure the task after project evaluation
        project.afterEvaluate {
            if (extension.enable.get()) {
                setupGenerateComposablePreviewPaparazziTestsTask(project, extension)
            }
        }
    }
}