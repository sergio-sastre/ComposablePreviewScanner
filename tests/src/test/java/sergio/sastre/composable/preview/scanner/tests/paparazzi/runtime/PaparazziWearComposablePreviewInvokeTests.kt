package sergio.sastre.composable.preview.scanner.tests.paparazzi.runtime

import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.wear.WearComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearPreviewInfo
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.DeviceConfigBuilder
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.applyUiMode
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.paparazziTestNameSnapshotHandler

/**
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziWearComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziWearComposablePreviewInvokeTests(
    private val preview: WearComposablePreview<WearPreviewInfo, Unit>,
) {

    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val cachedPreviews by lazy {
            WearComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<WearComposablePreview<WearPreviewInfo, Unit>> = cachedPreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder
            .build(preview.previewInfo.device)
            .applyUiMode(preview.previewInfo.uiMode),
        theme = "android:Theme.DeviceDefault",
        renderingMode = SessionParams.RenderingMode.NORMAL,
        snapshotHandler = paparazziTestNameSnapshotHandler()
    )

    @Test
    fun snapshot() {
        val screenshotId = WearPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .encodeUnsafeCharacters()
            .build()

        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }
}
