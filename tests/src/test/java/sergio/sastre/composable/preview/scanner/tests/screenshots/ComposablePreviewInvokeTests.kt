package sergio.sastre.composable.preview.scanner.tests.screenshots

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source.
 *
 * ./gradlew :tests:recordRoborazziDebug --test '*PreviewInvokeTests'
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class ComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .scanPackageTrees(
                    // TODO previewsinsideclass tests are not supported yet
                    "sergio.sastre.composable.preview.scanner.additionalannotation",
                    "sergio.sastre.composable.preview.scanner.api30plus"
                    //"sergio.sastre.composable.preview.scanner.previewsinsideclass"
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {
        captureRoboImage(
            filePath = "${AndroidPreviewScreenshotIdBuilder(preview).build()}.png",
        ) {
            preview()
        }
    }
}