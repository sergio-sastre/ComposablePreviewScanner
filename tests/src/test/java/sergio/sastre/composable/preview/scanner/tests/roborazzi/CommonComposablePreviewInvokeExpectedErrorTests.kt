package sergio.sastre.composable.preview.scanner.tests.roborazzi

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.exception.PreviewParameterIsNotFirstArgumentException
import sergio.sastre.composable.preview.scanner.jvm.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.jvm.common.screenshotid.CommonPreviewScreenshotIdBuilder

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'CommonComposablePreviewInvokeExpectedErrorTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class CommonComposablePreviewInvokeExpectedErrorTests(
    private val preview: ComposablePreview<CommonPreviewInfo>,
) {
    
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val cachedBuildTimePreviews: List<ComposablePreview<CommonPreviewInfo>> by lazy {
            CommonComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("defaultparams.before.previewparameters.throwserror")
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<CommonPreviewInfo>> = cachedBuildTimePreviews
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test(expected = PreviewParameterIsNotFirstArgumentException::class)
    fun snapshot() {

        val name = CommonPreviewScreenshotIdBuilder(preview)
            .doNotIgnoreMethodParametersType()
            .build()
        captureRoboImage(filePath = "${name}.png") {
            preview()
        }
    }
}