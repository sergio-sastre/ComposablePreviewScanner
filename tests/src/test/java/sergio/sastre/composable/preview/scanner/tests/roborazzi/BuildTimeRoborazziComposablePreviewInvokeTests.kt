package sergio.sastre.composable.preview.scanner.tests.roborazzi

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.device.domain.RobolectricDeviceQualifierBuilder
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'BuildTimeRoborazziComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class BuildTimeRoborazziComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val cachedBuildTimePreviews: List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedBuildTimePreviews
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {
        RobolectricDeviceQualifierBuilder.build(preview.previewInfo.device)?.run {
            RuntimeEnvironment.setQualifiers(this)
        }

        val name = AndroidPreviewScreenshotIdBuilder(preview)
            .doNotIgnoreMethodParametersType()
            .build()
        captureRoboImage(filePath = "${name}.png") {
            preview()
        }
    }
}