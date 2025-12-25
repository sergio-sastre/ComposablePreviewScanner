package sergio.sastre.composable.preview.scanner.tests.roborazzi.runtime

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.background
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.locale
import com.github.takahirom.roborazzi.size
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'RoborazziCommonComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RoborazziCommonComposablePreviewInvokeTests(
    private val preview: ComposablePreview<CommonPreviewInfo>,
) {
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val cachedBuildTimePreviews: List<ComposablePreview<CommonPreviewInfo>> by lazy {
            CommonComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<CommonPreviewInfo>> = cachedBuildTimePreviews
    }

    fun screenshotName(preview: ComposablePreview<CommonPreviewInfo>): String =
        "src/test/screenshots/common/${
            CommonPreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
        }.png"

    @OptIn(ExperimentalRoborazziApi::class)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {
        captureRoboImage(
            filePath = screenshotName(preview),
            roborazziComposeOptions = RoborazziComposeOptions {
                size(
                    widthDp = preview.previewInfo.widthDp,
                    heightDp = preview.previewInfo.heightDp
                )
                background(
                    showBackground = preview.previewInfo.showBackground,
                    backgroundColor = preview.previewInfo.backgroundColor
                )
                locale(preview.previewInfo.locale)
            },
        ) {
            preview()
        }
    }
}