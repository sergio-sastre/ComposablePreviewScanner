package sergio.sastre.composable.preview.scanner

import android.content.res.AssetManager
import androidx.activity.compose.setContent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dev.testify.annotation.ScreenshotInstrumentation
import dev.testify.core.processor.capture.pixelCopyCapture
import dev.testify.scenario.ScreenshotScenarioRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.SourceSetClasspath.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.SourceSetClasspath.SCREENSHOT_TEST
import sergio.sastre.uitesting.android_testify.screenshotscenario.assertSame
import sergio.sastre.uitesting.android_testify.screenshotscenario.generateDiffs
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule
import sergio.sastre.uitesting.utils.utils.waitForActivity
import sergio.sastre.uitesting.utils.utils.waitForComposeView

/**
 * ./gradlew :tests:screenshotRecord
 */
/*
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
                // TODO -> this is necessary... should not?
                // TODO 1. Check whether necessary for jvm-based and scanFile
                .setTargetSourceSet(Classpath(SCREENSHOT_TEST))
                .scanFile(
                    targetInputStream = assets.open("scan_result.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        private val cachedScreenshotTestPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(SCREENSHOT_TEST))
                .scanFile(
                    targetInputStream = getInstrumentation().context.assets.open("scan_result.json"),
                    customPreviewsInfoInputStream = getInstrumentation().context.assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> =
            cachedMainPreviews
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

*/
class AndroidComposablePreviewScannerInstrumentationTest {

    // WARNING! : Execute the unit tests inside "run_before_instrumentation_tests"
    // to generate the "scan_result.json"
    @Test
    fun GIVEN_scan_result_for_all_previews_is_dumped_into_assets_file_WHEN_that_file_is_scanned_THEN_that_file_exists_and_contains_all_previews() {

        val assets = getInstrumentation().context.assets
        val previewsFromFile =
            AndroidComposablePreviewScanner()
                // TODO -> Add Screenshot tests with Dropshots and check whether the classes are loaded
                // TODO 1. For ScreenshotTest sourceSet, they are not loaded hack the sourceSet in gradle?
                // TODO 2. Which ClassLoader is needed when running instrumentation tests? decide based on whether it runs instrumentation test or not?
                // TODO 3. Add Previews under androidTest and add Tests
                // TODO 4. Add new module with custom preview annotations
                // TODO 5. Documentation!!
                //.setTargetSourceSet(Classpath(BuildConfig.BUILD_DIR_PATH, SCREENSHOT_TEST))
                //.scanPackageTrees("sergio.sastre.composable.preview.scanner")

                .scanFile(
                    targetInputStream = assets.open("scan_result.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()

        assertEquals(previewsFromFile.size, 53)
    }
}
