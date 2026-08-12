package sergio.sastre.composable.preview.scanner.tests.roborazzi.runtime

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.background
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.locale
import com.github.takahirom.roborazzi.size
import com.github.takahirom.roborazzi.uiMode
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
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Ensures a @Preview whose @PreviewParameter provider yields a value class (e.g. Dp) can be both
 * scanned and invoked/rendered by Roborazzi. This exercises the invocation path
 * (ComposablePreview.invoke()), which previously crashed in kotlin-reflect's ValueClassAwareCaller
 * for value-class signatures. Mirrors PaparazziValueClassPreviewParameterInvokeTest so the fix is
 * covered under both rendering libraries, which download resources differently.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'RoborazziValueClassPreviewParameterInvokeTest' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RoborazziValueClassPreviewParameterInvokeTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    companion object {
        private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("valueclass.previewparameters.android")
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
    }

    fun screenshotName(preview: ComposablePreview<AndroidPreviewInfo>): String =
        "src/test/screenshots/valueclass/${
            AndroidPreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
        }.png"

    @OptIn(ExperimentalRoborazziApi::class)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {
        RobolectricDeviceQualifierBuilder.build(preview.previewInfo.device)?.run {
            RuntimeEnvironment.setQualifiers(this)
        }

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
                uiMode(preview.previewInfo.uiMode)
            },
        ) {
            preview()
        }
    }
}
