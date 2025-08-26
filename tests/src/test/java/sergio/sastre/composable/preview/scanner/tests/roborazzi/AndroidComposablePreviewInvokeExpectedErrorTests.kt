package sergio.sastre.composable.preview.scanner.tests.roborazzi

import com.github.takahirom.roborazzi.DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.exception.PreviewParameterIsNotFirstArgumentException

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'AndroidComposablePreviewInvokeExpectedErrorTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class AndroidComposablePreviewInvokeExpectedErrorTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val cachedBuildTimePreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("defaultparams.before.previewparameters.throwserror")
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedBuildTimePreviews
    }

    fun screenshotName(preview: ComposablePreview<AndroidPreviewInfo>): String =
        "$DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH/${
            AndroidPreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
        }.png"

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test(expected = PreviewParameterIsNotFirstArgumentException::class)
    fun snapshot() {
        val name = AndroidPreviewScreenshotIdBuilder(preview)
            .doNotIgnoreMethodParametersType()
            .build()
        captureRoboImage(filePath = "${name}.png") {
            preview()
        }
    }
}