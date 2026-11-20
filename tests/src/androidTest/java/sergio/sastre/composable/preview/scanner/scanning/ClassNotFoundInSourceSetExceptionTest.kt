package sergio.sastre.composable.preview.scanner.scanning

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ClassNotFoundInSourceSetException

/**
 * This test needs to run without -PincludeSourceSetScreenshotTest
 */
class ClassNotFoundInSourceSetExceptionTest {

    @Test(expected = ClassNotFoundInSourceSetException::class)
    fun WHEN_scanFile_for_screenshotTest_without_sourceSet_included_THEN_throw_ClassNotFoundInSourceSetException() {
        val assets = getInstrumentation().context.assets
        AndroidComposablePreviewScanner()
            .setTargetSourceSet(Classpath(SourceSet.SCREENSHOT_TEST))
            .scanFile(
                targetInputStream = assets.open("scan_result_screenshot_test.json"),
                customPreviewsInfoInputStream = assets.open("custom_previews.json")
            )
            .getPreviews()
    }
}
