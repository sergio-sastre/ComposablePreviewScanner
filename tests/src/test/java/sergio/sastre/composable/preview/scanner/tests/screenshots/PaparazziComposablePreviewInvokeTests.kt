package sergio.sastre.composable.preview.scanner.tests.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source.
 *
 * ./gradlew :tests:recordPaparazziDebug --tests 'PaparazziComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class PaparazziComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
    }

    @get:Rule
    val paparazzi = PaparazziBuilder.build(preview.previewInfo.device)

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).ignoreClassName().build()
        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }
}

object PaparazziBuilder {

    fun build(previewDevice: String): Paparazzi {
        val device = DevicePreviewInfoParser.parse(previewDevice) ?: return Paparazzi()
        return Paparazzi(
            deviceConfig = DeviceConfig(
                screenHeight = device.dimensions.height.toInt(),
                screenWidth = device.dimensions.width.toInt(),
                xdpi = device.densityDpi,
                ydpi = device.densityDpi,
                ratio = ScreenRatio.valueOf(device.screenRatio.name),
                size = ScreenSize.valueOf(device.screenSize.name),
                density = Density(device.densityDpi),
                screenRound = ScreenRound.valueOf(device.shape.name)
            )
        )
    }
}