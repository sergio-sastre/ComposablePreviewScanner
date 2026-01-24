package sergio.sastre.composable.preview.scanner.tests.roborazzi.runtime

import android.app.Application
import android.content.ComponentName
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziActivity
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
import sergio.sastre.composable.preview.scanner.glance.configuration.GlanceSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.glance.configuration.RobolectricDeviceQualifierBuilder
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * These tests also ensure that the GlanceViewConfigurator configures the GlanceView as expected
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'RoborazziGlanceComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RoborazziGlanceComposablePreviewInvokeTests(
    private val preview: ComposablePreview<GlancePreviewInfo>,
) {
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val glanceCachedBuildTimePreviews by lazy {
            GlanceComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<GlancePreviewInfo>> = glanceCachedBuildTimePreviews
    }

    // Workaround to ensure RoborazziActivity is registered with Robolectric's shadow package manager
    // This is necessary because Robolectric may not automatically register activities from external libraries
    @get:Rule(order = 0)
    val addActivityToRobolectricRule = object : TestWatcher() {
        override fun starting(description: Description?) {
            super.starting(description)
            val appContext: Application = getApplicationContext()
            Shadows.shadowOf(appContext.packageManager).addActivityIfNotPresent(
                ComponentName(
                    appContext.packageName,
                    RoborazziActivity::class.java.name,
                )
            )
        }
    }

    @get:Rule(order = 1)
    val activityScenarioRule =
        createAndroidComposeRule<RoborazziActivity>()

    fun screenshotName(preview: ComposablePreview<GlancePreviewInfo>): String =
        "src/test/screenshots/glance/${
            GlancePreviewScreenshotIdBuilder(preview).build()
        }.png"

    @OptIn(ExperimentalRoborazziApi::class)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Nexus7)
    @Test
    fun snapshot() {
        // Apply cumulative qualifier
        RuntimeEnvironment.setQualifiers(
            "+${RobolectricDeviceQualifierBuilder.build(preview.previewInfo)}"
        )

        GlanceSnapshotConfigurator(activityScenarioRule.activity)
            .setSizeDp(
                widthDp = preview.previewInfo.widthDp,
                heightDp = preview.previewInfo.heightDp
            )
            .composableToView { preview() }
            .captureRoboImage(filePath = screenshotName(preview))
    }
}