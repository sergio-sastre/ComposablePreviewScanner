package sergio.sastre.composable.preview.scanner.tests.paparazzi.runtime

import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.configuration.WearTileSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.paparazziTestNameSnapshotHandler
import androidx.wear.tiles.tooling.preview.TilePreviewData
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.getAnnotation
import sergio.sastre.composable.preview.scanner.tests.paparazzi.utils.DeviceConfigBuilder
import sergio.sastre.composable.preview.scanner.wear.PaparazziConfig
import sergio.sastre.composable.preview.scanner.wear.configuration.TileRoundShapeClipping.Enabled

/**
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziWearTilePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziWearTilePreviewInvokeTests(
    private val preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>,
) {

    companion object {
        private val wearCachedBuildTimePreviews by lazy {
            WearTilePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
                .includeAnnotationInfoForAllOf(PaparazziConfig::class.java)
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>> =
            wearCachedBuildTimePreviews
    }

    val defaultMaxPercentDifference = 0.0
    val maxPercentDifference: Double
        get() = preview.getAnnotation<PaparazziConfig>()?.let { config ->
            when (config.onlyApplyToDevice == preview.previewInfo.device) {
                true -> config.maxPercentDifference
                false -> defaultMaxPercentDifference
            }
        } ?: defaultMaxPercentDifference

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder
            .buildWear(preview.previewInfo.device)
            .copy(locale = preview.previewInfo.locale.ifBlank { "en" })
            .copy(fontScale = preview.previewInfo.fontScale),
        theme = "android:Theme.DeviceDefault",
        renderingMode = SessionParams.RenderingMode.NORMAL,
        snapshotHandler = paparazziTestNameSnapshotHandler(maxPercentDifference),
    )

    @Test
    fun snapshot() {
        val wearView = WearTileSnapshotConfigurator(paparazzi.context)
            .setTileRoundShapeClipping(Enabled.OnlyDevicesWithChin)
            .previewToView(preview)

        paparazzi.snapshot(
            view = wearView,
            name = WearTilePreviewScreenshotIdBuilder(preview)
                .encodeUnsafeCharacters()
                .doNotIgnoreMethodParametersType()
                .build()
                .replace("sergio.sastre.composable.preview.scanner.", "")
        )
    }
}
