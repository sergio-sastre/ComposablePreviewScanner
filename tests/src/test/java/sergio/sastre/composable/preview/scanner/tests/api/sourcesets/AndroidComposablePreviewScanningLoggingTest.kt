package sergio.sastre.composable.preview.scanner.tests.api.sourcesets

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.utils.SystemOutputTestRule
import java.io.File

@RunWith(Parameterized::class)
class AndroidComposablePreviewScanningLoggingTest(
    val classpath: Classpath
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Array<Any> {
            return arrayOf(
                Classpath(
                    sourceSet = MAIN,
                    variantName = "release"
                ),
                Classpath(
                    sourceSet = ANDROID_TEST,
                ),
                Classpath(
                    sourceSet = SCREENSHOT_TEST
                )
            )
        }
    }

    @get:Rule
    val systemOutputTestRule = SystemOutputTestRule()

    @RequiresShowStandardStreams
    @Test
    fun `WHEN Scanning in a given source set THEN outputs root dir and package path of that source set classpath`() {
        // WHEN
        val file = File(classpath.rootDir, classpath.packagePath)
        assumeTrue(file.exists())

        AndroidComposablePreviewScanner()
            .enableScanningLogs()
            .setTargetSourceSet(
                sourceSetClasspath = classpath,
                packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
            )
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .getPreviews()

        // THEN
        assertTrue(
            "Output does contain source set",
            systemOutputTestRule.systemOutput.contains("Source set (compiled classes path): ${classpath.rootDir}/${classpath.packagePath}")
        )
    }
}