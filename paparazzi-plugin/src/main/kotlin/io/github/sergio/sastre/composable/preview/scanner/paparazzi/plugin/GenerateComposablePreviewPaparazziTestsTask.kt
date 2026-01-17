package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateComposablePreviewPaparazziTestsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val scanPackageTrees: ListProperty<String>

    @get:Input
    abstract val includePrivatePreviews: Property<Boolean>

    @get:Input
    abstract val testClassName: Property<String>

    @get:Input
    abstract val testPackageName: Property<String>

    @TaskAction
    fun generateTests() {
        val testDir = outputDir.get().asFile
        testDir.mkdirs()

        val packagesExpr = scanPackageTrees.get().joinToString(", ") { "\"$it\"" }
        val includePrivatePreviewsExpr = includePrivatePreviews.get()
        val className = testClassName.get()
        val packageName = testPackageName.get()

        val directory = File(testDir, packageName.replace(".", "/"))
        directory.mkdirs()

        File(directory, "$className.kt").writeText(
            PaparazziTestGenerator().generateTestFileContent(
                packageName,
                className,
                packagesExpr,
                includePrivatePreviewsExpr
            )
        )

        logger.info("Generated Paparazzi test file: ${directory.absolutePath}/$className.kt")
    }
}