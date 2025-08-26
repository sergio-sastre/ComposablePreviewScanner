package sergio.sastre.composable.preview.scanner.tests.paparazzi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder
import kotlin.math.ceil

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziCommonComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziCommonComposablePreviewInvokeTests(
    private val preview: ComposablePreview<CommonPreviewInfo>,
) {

    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val cachedPreviews: List<ComposablePreview<CommonPreviewInfo>> by lazy {
            CommonComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<CommonPreviewInfo>> = cachedPreviews
    }

    @get:Rule
    val paparazzi = PaparazziPreviewRule.createFor(preview)

    @Test
    fun snapshot() {
        val screenshotId = CommonPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .doNotIgnoreMethodParametersType()
            .build()

        val previewInfo = preview.previewInfo
        paparazzi.snapshot(name = screenshotId) {
            ResizeComposable(
                widthInDp = previewInfo.widthDp,
                heightInDp = previewInfo.heightDp
            ) {
                PreviewBackground(
                    showBackground = previewInfo.showBackground,
                    backgroundColor = previewInfo.backgroundColor
                ) {
                    preview()
                }
            }
        }
    }
}

@Composable
fun PreviewBackground(
    showBackground: Boolean,
    backgroundColor: Long,
    content: @Composable () -> Unit
) {
    when (showBackground) {
        false -> content()
        true -> {
            val color = when (backgroundColor != 0L) {
                true -> Color(backgroundColor)
                false -> Color.White
            }
            Box(Modifier.background(color)) {
                content()
            }
        }
    }
}

@Composable
fun ResizeComposable(
    widthInDp: Int,
    heightInDp: Int,
    content: @Composable () -> Unit
) {
    val modifier: Modifier = when {
        widthInDp > 0 && heightInDp > 0 -> Modifier.size(width = widthInDp.dp, height = heightInDp.dp)
        heightInDp > 0 -> Modifier.height(heightInDp.dp)
        widthInDp > 0 -> Modifier.width(widthInDp.dp)
        else -> Modifier
    }
    Box(modifier = modifier) {
        content()
    }
}

class Dimensions(
    val screenWidthInPx: Int,
    val screenHeightInPx: Int
)

object ScreenDimensions {
    fun dimensions(
        parsedDevice: DeviceConfig,
        widthDp: Int,
        heightDp: Int
    ): Dimensions {
        val conversionFactor = parsedDevice.density.dpiValue / 160f
        val previewWidthInPx = ceil(widthDp * conversionFactor).toInt()
        val previewHeightInPx = ceil(heightDp * conversionFactor).toInt()
        return Dimensions(
            screenHeightInPx = when (heightDp > 0) {
                true -> previewHeightInPx
                false -> parsedDevice.screenHeight
            },
            screenWidthInPx = when (widthDp > 0) {
                true -> previewWidthInPx
                false -> parsedDevice.screenWidth
            }
        )
    }
}

object DeviceConfigBuilder {
    fun build(preview: CommonPreviewInfo): DeviceConfig {
        val dimensions = ScreenDimensions.dimensions(
            parsedDevice = DeviceConfig(),
            widthDp = preview.widthDp,
            heightDp = preview.heightDp
        )

        return DeviceConfig(
            screenHeight = dimensions.screenHeightInPx,
            screenWidth = dimensions.screenWidthInPx,
            locale = preview.locale.ifBlank { "en" },
        )
    }
}

object PaparazziPreviewRule {
    fun createFor(preview: ComposablePreview<CommonPreviewInfo>): Paparazzi {
        val previewInfo = preview.previewInfo
        return Paparazzi(
            deviceConfig = DeviceConfigBuilder.build(preview.previewInfo),
            environment = detectEnvironment().copy(compileSdkVersion = 34),
            supportsRtl = true,
            renderingMode = when(previewInfo.widthDp > 0 && previewInfo.heightDp > 0) {
                true -> SessionParams.RenderingMode.FULL_EXPAND
                false -> SessionParams.RenderingMode.SHRINK
            },
            // maxPercentDifference can be configured here if needed
            maxPercentDifference = 0.0
        )
    }
}