package sergio.sastre.composable.preview.scanner.tests.paparazzi.runtime

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.wear.WearTileComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.configuration.WearTileSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.paparazziTestNameSnapshotHandler
import androidx.wear.tiles.tooling.preview.TilePreviewData

/**
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziWearTileComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziWearTileComposablePreviewInvokeTests(
    private val preview: WearComposablePreview<WearTilePreviewInfo, TilePreviewData>,
) {

    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val wearCachedBuildTimePreviews by lazy {
            WearTileComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<WearComposablePreview<WearTilePreviewInfo, TilePreviewData>> = wearCachedBuildTimePreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfig.WEAR_OS_SMALL_ROUND,
        theme = "android:Theme.DeviceDefault",
        renderingMode = SessionParams.RenderingMode.NORMAL,
        snapshotHandler = paparazziTestNameSnapshotHandler()
    )

    @Test
    fun snapshot() {
        val wearView = WearTileSnapshotConfigurator(paparazzi.context)
            .composableToView { preview() }

        paparazzi.snapshot(
            view = wearView,
            name = WearTilePreviewScreenshotIdBuilder(preview)
                .ignoreClassName()
                .encodeUnsafeCharacters()
                .build()
        )
    }
}
