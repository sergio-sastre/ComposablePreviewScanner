package sergio.sastre.composable.preview.scanner.screenshots

import android.content.res.AssetManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.wear.tiles.tooling.preview.TilePreviewData
import dev.testify.annotation.ScreenshotInstrumentation
import dev.testify.core.processor.capture.canvasCapture
import dev.testify.scenario.ScreenshotScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.ScreenshotTestWearActivity
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.configuration.SkipShapeClipping.NEVER
import sergio.sastre.composable.preview.scanner.wear.configuration.WearTileSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder
import sergio.sastre.uitesting.android_testify.screenshotscenario.assertSame
import sergio.sastre.uitesting.android_testify.screenshotscenario.generateDiffs
import sergio.sastre.uitesting.android_testify.screenshotscenario.waitForIdleSync
import sergio.sastre.uitesting.utils.activityscenario.ActivityConfigItem
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule
import sergio.sastre.uitesting.utils.common.FontSizeScale
import sergio.sastre.uitesting.utils.testrules.animations.DisableAnimationsRule
import sergio.sastre.uitesting.utils.utils.waitForActivity
import sergio.sastre.uitesting.utils.utils.waitForComposeView

/**
 * ./gradlew :tests:screenshotRecord -PtestClass=sergio.sastre.composable.preview.scanner.screenshots.TestifyWearTilePreviewsTest
 */
@RunWith(Parameterized::class)
class TestifyWearTilePreviewsTest(
    private val preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>,
) {

    companion object {
        private val assets: AssetManager by lazy {
            getInstrumentation().context.assets
        }

        private val cachedMainPreviews: List<ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>> by lazy {
            WearTilePreviewScanner()
                .setTargetSourceSet(Classpath(MAIN))
                .scanFile(
                    targetInputStream = assets.open("scan_result_main_wear.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>> =
            cachedMainPreviews
    }

    @get:Rule(order = 1)
    val disableAnimations = DisableAnimationsRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotScenarioRule()

    @get:Rule(order = 3)
    val composableRule = activityScenarioForActivityRule<ScreenshotTestWearActivity>(
        config = ActivityConfigItem(
            fontSize = FontSizeScale.Value(preview.previewInfo.fontScale),
            systemLocale = preview.previewInfo.locale
                .removePrefix("b+").replace("+", "-").ifBlank { "en" }
        )
    )

    @ScreenshotInstrumentation
    @Test
    fun snapshot() {
        screenshotRule
            .withScenario(composableRule.activityScenario)
            .setScreenshotViewProvider {
                composableRule
                    .activityScenario.onActivity { activity ->
                        val view = WearTileSnapshotConfigurator(activity)
                            .skipShapeClipping(NEVER)
                            .previewToView(preview)
                        activity.setContentView(view)
                    }
                    .waitForActivity()
                    .waitForComposeView()
            }
            .configure {
                captureMethod = ::canvasCapture
                hideSoftKeyboard = false // avoid error on wear devices
            }
            .generateDiffs(true)
            .waitForIdleSync()
            .assertSame(
                name = WearTilePreviewScreenshotIdBuilder(preview)
                    .doNotIgnoreMethodParametersType()
                    .encodeUnsafeCharacters()
                    .build()
                    .replace("sergio.sastre.composable.preview.scanner.", "")
            )
    }
}
