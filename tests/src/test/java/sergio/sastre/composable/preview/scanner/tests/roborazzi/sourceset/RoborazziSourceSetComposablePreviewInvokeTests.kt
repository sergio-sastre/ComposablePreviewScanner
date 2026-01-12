package sergio.sastre.composable.preview.scanner.tests.roborazzi.sourceset

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.device.domain.RobolectricDeviceQualifierBuilder
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the 'main' and 'screenshotTest' sources based on their respective compiled classes.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'RoborazziSourceSetComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RoborazziSourceSetComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    companion object {
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
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> =
            cachedMainPreviews + cachedScreenshotTestPreviews + cachedAndroidTestPreviews
    }

    fun screenshotName(preview: ComposablePreview<AndroidPreviewInfo>): String =
        "src/test/screenshots/sourceset/${
            AndroidPreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
        }.png"

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {
        RobolectricDeviceQualifierBuilder.build(preview.previewInfo.device)?.run {
            RuntimeEnvironment.setQualifiers(this)
        }
        captureRoboImage(filePath = screenshotName(preview)) {
            preview()
        }
    }
}