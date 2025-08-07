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
import sergio.sastre.composable.preview.scanner.MainActivity
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.configuration.GlanceSnapshotConfigurator
import sergio.sastre.uitesting.android_testify.screenshotscenario.assertSame
import sergio.sastre.uitesting.android_testify.screenshotscenario.generateDiffs
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule
import sergio.sastre.uitesting.utils.utils.waitForView

/**
 * ./gradlew :tests:screenshotRecord -PincludeSourceSetScreenshotTest
 *
 * if executed without gradle property -PincludeSourceSetScreenshotTest,
 * it should throw ClassInScreenshotTestSourceSetNotFoundException
 */
@RunWith(Parameterized::class)
class TestifyGlanceComposablePreviewScannerInstrumentationInvokeTest(
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
                    targetInputStream = assets.open("scan_result_main.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()
                .apply {
                    println("Preview count: ${this.size}")
                }
        }

        @JvmStatic
        @Parameterized.Parameters
        fun values(): List<ComposablePreview<GlancePreviewInfo>> = cachedMainPreviews
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
            .configure { captureMethod = ::pixelCopyCapture }
            .generateDiffs(true)
            .assertSame(
                name = "${preview.previewInfo.widthDp}_${preview.previewInfo.heightDp}"
            )
    }
}