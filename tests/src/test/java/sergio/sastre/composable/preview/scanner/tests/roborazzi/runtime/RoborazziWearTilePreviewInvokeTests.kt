package sergio.sastre.composable.preview.scanner.tests.roborazzi.runtime

import android.app.Application
import android.content.ComponentName
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.wear.tiles.tooling.preview.TilePreviewData
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment.setFontScale
import org.robolectric.RuntimeEnvironment.setQualifiers
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.configuration.SkipShapeClipping
import sergio.sastre.composable.preview.scanner.wear.configuration.SkipShapeClipping.ROUND_WITHOUT_CHIN
import sergio.sastre.composable.preview.scanner.wear.configuration.WearTileSnapshotConfigurator
import sergio.sastre.composable.preview.scanner.wear.device.WearDevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.wear.device.domain.RobolectricWearDeviceQualifierBuilder
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder

/**
 * These tests ensure that the invoke() function of a ComposablePreview works as expected
 * for all the @Composable's in the main source at build time.
 *
 * ./gradlew :tests:recordRoborazziDebug --tests 'RoborazziWearTilePreviewInvokeTests' -Plibrary=roborazzi
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RoborazziWearTilePreviewInvokeTests(
    private val preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>,
) {
    companion object {
        @OptIn(RequiresShowStandardStreams::class)
        private val wearCachedBuildTimePreviews by lazy {
            WearTilePreviewScanner()
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
                .doNotIgnoreMethodParametersType()
                .build()
        }.png"

    private val isRoundWithoutChin : Boolean
        get() {
            val device = WearDevicePreviewInfoParser.parse(preview.previewInfo.device)!!
            val isRound = device.shape == Shape.ROUND
            val hasChin = device.chinSize.value > 0f
            return isRound && !hasChin
        }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [35])
    @Test
    fun snapshot() {
        setQualifiers(
            "+${RobolectricWearDeviceQualifierBuilder.build(preview.previewInfo.device)}"
        )
        setQualifiers("+${preview.previewInfo.locale.ifBlank { "en" }}")
        setFontScale(preview.previewInfo.fontScale)

        val wearView = WearTileSnapshotConfigurator(activityScenarioRule.activity)
            .skipShapeClipping(ROUND_WITHOUT_CHIN)
            .previewToView(preview)

        activityScenarioRule.activity.setContentView(wearView)

        wearView.captureRoboImage(
            filePath = screenshotName(preview),
            // account for round shapes
            roborazziOptions =
                RoborazziOptions(
                    recordOptions = RecordOptions(applyDeviceCrop = isRoundWithoutChin)
                )
        )
    }
}
