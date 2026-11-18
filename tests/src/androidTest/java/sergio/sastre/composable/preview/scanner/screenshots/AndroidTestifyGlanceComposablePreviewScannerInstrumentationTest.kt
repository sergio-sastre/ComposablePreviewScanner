package sergio.sastre.composable.preview.scanner.screenshots

import android.content.res.AssetManager
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dev.testify.annotation.ScreenshotInstrumentation
import dev.testify.core.processor.capture.pixelCopyCapture
import dev.testify.scenario.ScreenshotScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.ScreenshotTestActivity
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.configuration.GlanceSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder
import sergio.sastre.uitesting.android_testify.screenshotscenario.assertSame
import sergio.sastre.uitesting.android_testify.screenshotscenario.generateDiffs
import sergio.sastre.uitesting.android_testify.screenshotscenario.waitForIdleSync
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule
import sergio.sastre.uitesting.utils.utils.waitForView
import kotlin.text.replace

/**
 * ./gradlew :tests:screenshotRecord -PtestClass=sergio.sastre.composable.preview.scanner.screenshots.TestifyGlancePreviewsTest
 */
@RunWith(Parameterized::class)
class TestifyGlancePreviewsTest(
    private val preview: ComposablePreview<GlancePreviewInfo>,
) {

    companion object {
        private val assets: AssetManager by lazy {
            getInstrumentation().context.assets
        }

        private val cachedMainPreviews: List<ComposablePreview<GlancePreviewInfo>> by lazy {
            GlanceComposablePreviewScanner()
                .setTargetSourceSet(Classpath(MAIN))
                .scanFile(
                    targetInputStream = assets.open("scan_result_main_glance.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<GlancePreviewInfo>> = cachedMainPreviews
    }

    @get:Rule
    val screenshotRule = ScreenshotScenarioRule()

    @get:Rule
    val composableRule = activityScenarioForActivityRule<ScreenshotTestActivity>()

    @ScreenshotInstrumentation
    @Test
    fun snapshot() {
        screenshotRule
            .withScenario(composableRule.activityScenario)
            .setScreenshotViewProvider {
                val activity = composableRule.activity
                waitForView {
                    val glanceView =
                        GlanceSnapshotConfigurator(activity)
                            .setSizeDp(
                                widthDp = preview.previewInfo.widthDp,
                                heightDp = preview.previewInfo.heightDp
                            )
                            .composableToView { preview() }
                    activity.setContentView(glanceView)
                    (glanceView as ViewGroup).getChildAt(0)
                }
            }
            .configure {
                captureMethod = ::pixelCopyCapture
                hideSoftKeyboard = false
            }
            .generateDiffs(true)
            .waitForIdleSync()
            .assertSame(
                name = GlancePreviewScreenshotIdBuilder(preview)
                    .doNotIgnoreMethodParametersType()
                    .encodeUnsafeCharacters()
                    .build()
                    .replace("sergio.sastre.composable.preview.scanner.", "")
            )
    }
}