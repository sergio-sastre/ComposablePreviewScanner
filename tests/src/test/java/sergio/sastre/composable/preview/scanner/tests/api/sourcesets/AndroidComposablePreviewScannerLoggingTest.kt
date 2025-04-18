package sergio.sastre.composable.preview.scanner.tests.api.sourcesets

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

@RunWith(Parameterized::class)
class AndroidComposablePreviewScannerLoggingTest<T>(
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
                ),
            )
        }
    }

    // Fields to store the original System.out and our capturing stream
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @Before
    fun setUp() {
        // Redirect System.out to our ByteArrayOutputStream
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @After
    fun tearDown() {
        // Restore the original System.out after each test
        System.setOut(standardOut)
    }

    @Test
    fun `WHEN Scanning in a given source set THEN outputs root dir and package path of that source set classpath`() {
        // WHEN
        val file = File(classpath.rootDir, classpath.packagePath)
        assumeTrue(file.exists())

        AndroidComposablePreviewScanner()
            .setTargetSourceSet(
                sourceSetClasspath = classpath,
                packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
            )
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .getPreviews()

        // THEN
        val output = outputStreamCaptor.toString().trim()

        // DOES CONTAIN
        assertTrue(
            "Output does contain source set",
            output.contains("Source set (compiled classes path): ${classpath.rootDir}/${classpath.packagePath}")
        )
    }
}