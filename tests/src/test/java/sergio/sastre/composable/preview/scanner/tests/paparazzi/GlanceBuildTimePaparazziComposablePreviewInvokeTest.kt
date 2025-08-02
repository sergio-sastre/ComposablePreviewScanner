package sergio.sastre.composable.preview.scanner.tests.paparazzi

import android.widget.FrameLayout
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.tests.roborazzi.GlanceWrapper
import kotlin.math.roundToInt

@RunWith(Parameterized::class)
class GlanceBuildTimePaparazziComposablePreviewInvokeTests(
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

    val baseDeviceConfig: DeviceConfig =
        DeviceConfig.NEXUS_5.copy(
            density = Density.XXHIGH
        )

    val scale = baseDeviceConfig.density.dpiValue.toFloat()

    fun width(original: Int): Int {
        val widthDp = (preview.previewInfo.widthDp * scale).roundToInt()
        return when (widthDp > 0) {
            true -> widthDp
            false -> original
        }
    }

    fun height(original: Int): Int {
        val heightDp = (preview.previewInfo.heightDp * scale).roundToInt()
        return when (heightDp > 0) {
            true -> heightDp
            false -> original
        }
    }

    @get:Rule(order = 1)
    val paparazzi = Paparazzi(
        deviceConfig = baseDeviceConfig.copy(
            screenWidth = width(baseDeviceConfig.screenWidth),
            screenHeight = height(baseDeviceConfig.screenHeight),
        ),
        theme = "Theme.AppCompat.Light",
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun snapshot() {
        val glanceView =
            GlanceWrapper(rootView = FrameLayout(paparazzi.context))
                .setSizeDp(
                    widthDp = preview.previewInfo.widthDp,
                    heightDp = preview.previewInfo.heightDp
                )
                .renderComposable { preview() }

        paparazzi.snapshot(
            view = glanceView.rootView,
            name = "${preview.previewInfo.widthDp}_${preview.previewInfo.heightDp}"
        )
    }
}