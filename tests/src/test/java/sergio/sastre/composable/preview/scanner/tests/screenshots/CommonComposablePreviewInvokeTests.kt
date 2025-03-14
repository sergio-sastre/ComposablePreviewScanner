package sergio.sastre.composable.preview.scanner.tests.screenshots

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.jvm.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.common.CommonPreviewInfo

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'CommonComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class CommonComposablePreviewInvokeTests(
    private val preview: ComposablePreview<CommonPreviewInfo>,
) {

    companion object {
        private val cachedBuildTimePreviews: List<ComposablePreview<CommonPreviewInfo>> =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<CommonPreviewInfo>> = cachedBuildTimePreviews
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {

        // TODO -> name finishes with underscore "_" and should not.
        //  Maybe create screenshotIdBuilder??
        val name = preview.toString()
        captureRoboImage(filePath = "${name}.png") {
            preview()
        }
    }
}