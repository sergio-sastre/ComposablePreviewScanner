package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.CompiledClassesNotFound
import java.io.File

class AndroidComposablePreviewScannerSourceSetMissingTest {
    @Test
    fun `GIVEN 'screenshotTest' compiled classes do not exist WHEN target Source Set THEN throw CompiledClassesNotFound error`() {
        val classpath = Classpath(SourceSet.SCREENSHOT_TEST)
        val file = File(classpath.rootDir, classpath.packagePath)
        Assume.assumeFalse(file.exists())

        val exception = Assert.assertThrows(CompiledClassesNotFound::class.java) {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(sourceSetClasspath = classpath)
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
        }

        Assert.assertEquals(
            "No compiled classes under ${file.absolutePath}. Generate them by executing the corresponding 'compile' task:\n" +
                    "> ./gradlew :mymodule:compile<Variant>ScreenshotTestKotlin, where Variant is usually Debug or Release\n" +
                    ">\n" +
                    "> Alternatively, you can configure gradle to always run that command before running JVM-screenshot tests like this:\n" +
                    "> tasks.withType<Test> {\n" +
                    ">   dependsOn(\"compile<Variant>ScreenshotTestKotlin\")\n" +
                    ">}",
            exception.message
        )
    }

    @Test
    fun `GIVEN 'androidTest' compiled classes do not exist WHEN target Source Set THEN throw CompiledClassesNotFound error`() {
        val classpath = Classpath(SourceSet.ANDROID_TEST)
        val file = File(classpath.rootDir, classpath.packagePath)
        Assume.assumeFalse(file.exists())

        val exception = Assert.assertThrows(CompiledClassesNotFound::class.java) {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(sourceSetClasspath = classpath)
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
        }

        Assert.assertEquals(
            "No compiled classes under ${file.absolutePath}. Generate them by executing the corresponding 'compile' task:\n" +
                    "> ./gradlew :mymodule:compile<Variant>AndroidTestKotlin, where Variant is usually Debug or Release\n" +
                    ">\n" +
                    "> Alternatively, you can configure gradle to always run that command before running JVM-screenshot tests like this:\n" +
                    "> tasks.withType<Test> {\n" +
                    ">   dependsOn(\"compile<Variant>AndroidTestKotlin\")\n" +
                    ">}",
            exception.message
        )
    }
}