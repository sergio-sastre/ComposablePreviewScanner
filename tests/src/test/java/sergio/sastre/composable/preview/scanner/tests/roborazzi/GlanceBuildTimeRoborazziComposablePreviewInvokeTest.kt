package sergio.sastre.composable.preview.scanner.tests.roborazzi

import android.app.Application
import android.content.ComponentName
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.uitesting.utils.utils.waitForActivity
import kotlin.intArrayOf

@RunWith(ParameterizedRobolectricTestRunner::class)
class GlanceBuildTimeRoborazziComposablePreviewInvokeTests(
    private val preview: ComposablePreview<GlancePreviewInfo>,
) {
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val glanceCachedBuildTimePreviews by lazy {
            GlanceComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.glance")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<GlancePreviewInfo>> = glanceCachedBuildTimePreviews
    }

    @get:Rule(order = 0)
    val addActivityToRobolectricRule = object : TestWatcher() {
        override fun starting(description: Description?) {
            super.starting(description)
            val appContext: Application = getApplicationContext()
            Shadows.shadowOf(appContext.packageManager).addActivityIfNotPresent(
                ComponentName(
                    appContext.packageName,
                    GlanceScreenshotTestActivity::class.java.name,
                )
            )
        }
    }

    @get:Rule(order = 1)
    val activityScenarioRule =
        ActivityScenarioRule(GlanceScreenshotTestActivity::class.java)

    @OptIn(ExperimentalRoborazziApi::class)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [33])
    @Test
    fun snapshot() {
        activityScenarioRule
            .scenario
            .waitForActivity().renderComposable(
                size = DpSize(
                    width = preview.previewInfo.widthDp.dp,
                    height = preview.previewInfo.heightDp.dp
                )
            ) { preview() }
            .captureRoboImage(
                filePath = "${preview}.png",
                roborazziOptions = RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(
                        changeThreshold = 0F
                    )
                )
            )
    }
}