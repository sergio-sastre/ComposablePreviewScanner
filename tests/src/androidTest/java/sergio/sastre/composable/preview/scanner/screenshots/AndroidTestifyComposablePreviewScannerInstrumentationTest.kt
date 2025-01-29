package sergio.sastre.composable.preview.scanner.screenshots

import android.content.res.AssetManager
import androidx.activity.compose.setContent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dev.testify.annotation.ScreenshotInstrumentation
import dev.testify.core.processor.capture.pixelCopyCapture
import dev.testify.scenario.ScreenshotScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.MainActivity
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.uitesting.android_testify.screenshotscenario.assertSame
import sergio.sastre.uitesting.android_testify.screenshotscenario.generateDiffs
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule
import sergio.sastre.uitesting.utils.utils.waitForActivity
import sergio.sastre.uitesting.utils.utils.waitForComposeView

/**
 * ./gradlew :tests:screenshotRecord -PincludeSourceSetScreenshotTest
 *
 * if executed without gradle property -PincludeSourceSetScreenshotTest,
 * it should throw ClassInScreenshotTestSourceSetNotFoundException
 */
@RunWith(Parameterized::class)
class TestifyComposablePreviewScannerInstrumentationInvokeTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        private val assets: AssetManager by lazy {
            getInstrumentation().context.assets
        }

        private val cachedMainPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(MAIN))
                .scanFile(
                    targetInputStream = assets.open("scan_result_main.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        private val cachedScreenshotTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(SCREENSHOT_TEST))
                .scanFile(
                    targetInputStream = getInstrumentation().context.assets.open("scan_result_screenshot_test.json"),
                    customPreviewsInfoInputStream = getInstrumentation().context.assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        private val cachedAndroidTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(ANDROID_TEST))
                .scanFile(
                    targetInputStream = getInstrumentation().context.assets.open("scan_result_android_test.json"),
                    customPreviewsInfoInputStream = getInstrumentation().context.assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> =
            cachedMainPreviews + cachedScreenshotTestPreviews + cachedAndroidTestPreviews
    }

    @get:Rule
    val screenshotRule = ScreenshotScenarioRule()

    @get:Rule
    val composableRule = activityScenarioForActivityRule<MainActivity>()

    @ScreenshotInstrumentation
    @Test
    fun snapshot() {

        screenshotRule
            .withScenario(composableRule.activityScenario)
            .setScreenshotViewProvider {
                composableRule
                    .activityScenario.onActivity {
                        it.setContent { preview() }
                    }
                    .waitForActivity()
                    .waitForComposeView()
            }
            .configure { captureMethod = ::pixelCopyCapture }
            .generateDiffs(true)
            .assertSame(
                name = AndroidPreviewScreenshotIdBuilder(preview).build()
            )
    }
}