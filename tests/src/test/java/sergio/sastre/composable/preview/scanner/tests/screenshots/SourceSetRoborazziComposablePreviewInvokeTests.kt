package sergio.sastre.composable.preview.scanner.tests.screenshots

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
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.SourceSet.SCREENSHOT_TEST

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the 'main' and 'screenshotTest' sources based on their respective compiled classes.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'SourceSetRoborazziComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class SourceSetRoborazziComposablePreviewInvokeTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        private val cachedMainPreviews: List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()

        private val cachedScreenshotTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(SCREENSHOT_TEST))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()

        private val cachedAndroidTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(ANDROID_TEST))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> =
            cachedMainPreviews + cachedScreenshotTestPreviews + cachedAndroidTestPreviews
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30])
    @Test
    fun snapshot() {
        RobolectricDeviceQualifierBuilder.build(preview.previewInfo.device)?.run {
            RuntimeEnvironment.setQualifiers(this)
        }

        captureRoboImage(
            filePath = "${AndroidPreviewScreenshotIdBuilder(preview).build()}.png",
        ) {
            preview()
        }
    }
}