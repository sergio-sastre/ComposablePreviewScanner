package sergio.sastre.composable.preview.scanner.tests.paparazzi.sourceset

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
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
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the 'main' and 'screenshotTest' sources based on their respective compiled classes.
 *
 * ./gradlew paparazziPreviewsSourceSet
 */
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

@OptIn(RequiresShowStandardStreams::class)
private val cachedMainPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
    AndroidComposablePreviewScanner()
        .enableScanningLogs()
        .setTargetSourceSet(
            sourceSetClasspath = Classpath(SourceSet.MAIN),
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

@RunWith(Parameterized::class)
class PaparazziSourceSetMainComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedMainPreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder.build(preview.previewInfo.device),
        renderingMode = SessionParams.RenderingMode.SHRINK
    )

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .ignoreMethodName()
            .doNotIgnoreMethodParametersType()
            .encodeUnsafeCharacters()
            .build()

        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }
}

@RunWith(Parameterized::class)
class PaparazziSourceSetScreenshotTestComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedScreenshotTestPreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder.build(preview.previewInfo.device),
        renderingMode = SessionParams.RenderingMode.SHRINK
    )

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .ignoreMethodName()
            .doNotIgnoreMethodParametersType()
            .encodeUnsafeCharacters()
            .build()

        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }
}

@RunWith(Parameterized::class)
class PaparazziSourceSetAndroidTestComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedAndroidTestPreviews
    }

    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = 34),
        deviceConfig = DeviceConfigBuilder.build(preview.previewInfo.device),
        renderingMode = SessionParams.RenderingMode.SHRINK
    )

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .ignoreMethodName()
            .doNotIgnoreMethodParametersType()
            .encodeUnsafeCharacters()
            .build()

        paparazzi.snapshot(name = screenshotId) {
            preview()
        }
    }
}