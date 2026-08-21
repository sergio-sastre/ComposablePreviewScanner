package sergio.sastre.composable.preview.scanner.tests.roborazzi.runtime

import android.app.Application
import android.content.ComponentName
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.wear.tiles.tooling.preview.TilePreviewData
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
import sergio.sastre.composable.preview.scanner.android.device.domain.RobolectricDeviceQualifierBuilder
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.wear.WearTileComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.configuration.WearTileSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'RoborazziWearTileComposablePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RoborazziWearTileComposablePreviewInvokeTests(
    private val preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>,
) {
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val wearCachedBuildTimePreviews by lazy {
            WearTileComposablePreviewScanner()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>> =
            wearCachedBuildTimePreviews
    }

    // Workaround to ensure RoborazziActivity is registered with Robolectric's shadow package manager
    // This is necessary because Robolectric may not automatically register activities from external libraries
    @get:Rule(order = 0)
    val addActivityToRobolectricRule = object : TestWatcher() {
        override fun starting(description: Description) {
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

    fun screenshotName(preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>): String =
        "src/test/screenshots/weartile/${
            WearTilePreviewScreenshotIdBuilder(preview)
                .build()
        }.png"

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [35])
    @Test
    fun snapshot() {
        // TODO - Remove wear SMALL ROUND from test name, since it is default
        // TODO - Support
        // TODO - Put all this in a wear.RobolectricDeviceQualifierBuilder.build
        RuntimeEnvironment.setQualifiers(
            "+${RobolectricDeviceQualifierBuilder.build(preview.previewInfo.device)}"
        )
        RuntimeEnvironment.setQualifiers("+${preview.previewInfo.locale.ifBlank { "en" }}")
        RuntimeEnvironment.setFontScale(preview.previewInfo.fontScale)

        val wearView = WearTileSnapshotConfigurator(activityScenarioRule.activity)
            // TODO - shouldIAllow further config like glance??
            .previewToView(preview)

        activityScenarioRule.activity.setContentView(wearView)

        wearView.captureRoboImage(filePath = screenshotName(preview))
    }
}
