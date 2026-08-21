package sergio.sastre.composable.preview.scanner.tests.paparazzi.runtime

import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.wear.WearTileComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.configuration.WearTileSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.paparazziTestNameSnapshotHandler
import androidx.wear.tiles.tooling.preview.TilePreviewData
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.DeviceConfigBuilder

/**
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziWearTileComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziWearTileComposablePreviewInvokeTests(
    private val preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>,
) {

    companion object {
        private val wearCachedBuildTimePreviews by lazy {
            WearTileComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>> =
            wearCachedBuildTimePreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder
            .build(preview.previewInfo)
            .copy(locale = preview.previewInfo.locale.ifBlank { "en" })
            .copy(fontScale = preview.previewInfo.fontScale),
        theme = "android:Theme.DeviceDefault",
        renderingMode = SessionParams.RenderingMode.NORMAL,
        snapshotHandler = paparazziTestNameSnapshotHandler()
    )

    @Test
    fun snapshot() {
        val wearView = WearTileSnapshotConfigurator(paparazzi.context)
            .previewToView(preview)

        paparazzi.snapshot(
            view = wearView,
            name = WearTilePreviewScreenshotIdBuilder(preview)
                .ignoreClassName()
                .encodeUnsafeCharacters()
                .build()
                .replace("sergio.sastre.composable.preview.scanner.", "")
        )
    }
}
