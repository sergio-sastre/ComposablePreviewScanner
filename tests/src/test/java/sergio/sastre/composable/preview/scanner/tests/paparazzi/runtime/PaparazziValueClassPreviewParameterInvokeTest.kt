package sergio.sastre.composable.preview.scanner.tests.paparazzi.runtime

import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.DeviceConfigBuilder
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.applyUiMode
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.paparazziTestNameSnapshotHandler

/**
 * Ensures a @Preview whose @PreviewParameter provider yields a value class (e.g. Dp) can be both
 * scanned and invoked/rendered. This exercises the invocation path (ComposablePreview.invoke()),
 * which previously crashed in kotlin-reflect's ValueClassAwareCaller for value-class signatures.
 *
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziValueClassPreviewParameterInvokeTest' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziValueClassPreviewParameterInvokeTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("valueclass.previewparameters.android")
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder
            .build(preview.previewInfo.device)
            .applyUiMode(preview.previewInfo.uiMode),
        renderingMode = SessionParams.RenderingMode.SHRINK,
        snapshotHandler = paparazziTestNameSnapshotHandler()
    )

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
            .doNotIgnoreMethodParametersType()
            .encodeUnsafeCharacters()
            .build()
            .replace("sergio.sastre.composable.preview.scanner.", "")

        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }
}
