package sergio.sastre.composable.preview.scanner.core.scanner.exceptions

import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import java.io.File

class CompiledClassesNotFound(classpath: Classpath) : Exception(
    buildErrorMessage(classpath)
)

private fun buildErrorMessage(classpath: Classpath): String {
    val path = File(classpath.rootDir, classpath.packagePath).absolutePath
    val lowercasePackagePath = classpath.packagePath.lowercase()
    val sourceSet = when {
        lowercasePackagePath.contains("androidtest") -> "AndroidTest"
        lowercasePackagePath.contains("screenshottest") -> "ScreenshotTest"
        else -> "<SourceSet>"
    }
    val agpWarning = when {
        lowercasePackagePath.contains("screenshottest") -> "\nWARNING: If using AGP 8.6.x or prior, configure ComposablePreviewScanner " +
                "with .setTargetSourceSet(LegacySourceSetClasspath.SCREENSHOT_TEST)"
        else -> ""
    }
    return "No compiled classes under $path. Generate them by executing the corresponding 'compile' task:\n" +
            "> ./gradlew :mymodule:compile<Variant>${sourceSet}Kotlin, where Variant is usually Debug or Release\n" +
            ">\n" +
            "> Alternatively, you can configure gradle to always run that command before running JVM-screenshot tests like this:\n" +
            "> tasks.withType<Test> {\n" +
            ">   dependsOn(\"compile<Variant>${sourceSet}Kotlin\")\n" +
            ">}" +
            agpWarning

}