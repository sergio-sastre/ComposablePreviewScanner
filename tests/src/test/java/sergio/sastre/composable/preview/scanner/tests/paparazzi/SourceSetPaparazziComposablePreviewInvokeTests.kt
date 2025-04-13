package sergio.sastre.composable.preview.scanner.tests.paparazzi

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
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
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the 'main' and 'screenshotTest' sources based on their respective compiled classes.
 *
 * ./gradlew :tests:recordPaparazziDebug --tests 'SourceSetPaparazziComposablePreviewInvokeTests' -Plibrary=paparazzi
 */
@RunWith(Parameterized::class)
class SourceSetPaparazziComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        private val cachedMainPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = Classpath(SourceSet.MAIN),
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        private val cachedScreenshotTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = Classpath(SourceSet.SCREENSHOT_TEST),
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        private val cachedAndroidTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = Classpath(SourceSet.ANDROID_TEST),
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> =
            cachedMainPreviews + cachedScreenshotTestPreviews + cachedAndroidTestPreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfigBuilder.build(preview.previewInfo.device),
        renderingMode = SessionParams.RenderingMode.SHRINK
    )

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .doNotIgnoreMethodParametersType()
            .build()
        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }

    object DeviceConfigBuilder {
        fun build(previewDevice: String): DeviceConfig {
            val device = DevicePreviewInfoParser.parse(previewDevice) ?: return DeviceConfig()
            return DeviceConfig(
                screenHeight = device.dimensions.height.toInt(),
                screenWidth = device.dimensions.width.toInt(),
                xdpi = device.densityDpi, // not 100% precise
                ydpi = device.densityDpi, // not 100% precise
                ratio = ScreenRatio.valueOf(device.screenRatio.name),
                size = ScreenSize.valueOf(device.screenSize.name),
                density = Density(device.densityDpi),
                screenRound = ScreenRound.valueOf(device.shape.name)
            )
        }
    }
}