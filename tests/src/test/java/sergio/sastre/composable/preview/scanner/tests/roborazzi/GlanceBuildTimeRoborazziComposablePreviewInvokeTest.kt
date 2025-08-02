package sergio.sastre.composable.preview.scanner.tests.roborazzi

import android.app.Application
import android.content.ComponentName
import android.view.ViewGroup
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziTransparentActivity
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
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
                    RoborazziTransparentActivity::class.java.name,
                )
            )
        }
    }

    @get:Rule(order = 1)
    val activityScenarioRule =
        createAndroidComposeRule<RoborazziTransparentActivity>()

    @OptIn(ExperimentalRoborazziApi::class)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [33])
    @Test
    fun snapshot() {
        // Necessary for Roborazzi
        RuntimeEnvironment.setQualifiers("land")
        val width = preview.previewInfo.widthDp
        if (width > 0) {
            RuntimeEnvironment.setQualifiers("+w${width}dp")
        }
        val height = preview.previewInfo.heightDp
        if (height > 0) {
            RuntimeEnvironment.setQualifiers("+h${height}dp")
        }

        val activity = activityScenarioRule.activity

        GlanceWrapper(activity.window.decorView as ViewGroup)
            .setSizeDp(
                widthDp = preview.previewInfo.widthDp,
                heightDp = preview.previewInfo.heightDp
            )
            .renderComposable { preview() }
            .captureRoboImage(
                filePath = "${preview.previewInfo.widthDp}_${preview.previewInfo.heightDp}.png",
            )
    }
}