package sergio.sastre.composable.preview.scanner.tests.paparazzi.runtime

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.ScreenOrientation.LANDSCAPE
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.configuration.GlanceDeviceConfigDimensions
import sergio.sastre.composable.preview.scanner.glance.configuration.GlanceSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * These tests also ensure that the GlanceViewConfigurator configures the GlanceView as expected
 *
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziGlanceComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziGlanceComposablePreviewInvokeTests(
    private val preview: ComposablePreview<GlancePreviewInfo>,
) {

    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val glanceCachedBuildTimePreviews by lazy {
            GlanceComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.glance")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<GlancePreviewInfo>> = glanceCachedBuildTimePreviews
    }

    // Always use Landscape for Glance
    val baseDeviceConfig: DeviceConfig =
        DeviceConfig.NEXUS_7.copy(orientation = LANDSCAPE)

    val glanceDeviceConfig = GlanceDeviceConfigDimensions(
        densityDpi = baseDeviceConfig.density.dpiValue.toFloat(),
        previewWidthDp = preview.previewInfo.widthDp,
        previewHeightDp = preview.previewInfo.heightDp
    )

    @get:Rule(order = 1)
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = baseDeviceConfig.copy(
            screenWidth = glanceDeviceConfig.width(baseDeviceConfig.screenWidth),
            screenHeight = glanceDeviceConfig.height(baseDeviceConfig.screenHeight),
        ),
        theme = "Theme.AppCompat.Light",
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun snapshot() {
        val glanceView =
            GlanceSnapshotConfigurator(paparazzi.context)
                .setSizeDp(
                    widthDp = preview.previewInfo.widthDp,
                    heightDp = preview.previewInfo.heightDp
                )
                .composableToView {
                    preview()
                }

        paparazzi.snapshot(
            view = glanceView,
            name = GlancePreviewScreenshotIdBuilder(preview)
                .ignoreClassName()
                .build()
        )
    }
}