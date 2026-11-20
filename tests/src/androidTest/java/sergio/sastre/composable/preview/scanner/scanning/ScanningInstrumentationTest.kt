package sergio.sastre.composable.preview.scanner.scanning

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.MainActivity
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ScanSourceNotSupported
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule

@RunWith(Parameterized::class)
class ScanningInstrumentationTest(
    private val config: ScannerTestConfig,
) {

    data class ScannerTestConfig(
        val name: String,
        val mainScanResultFileName: String,
        val createScanner: () -> SourceScannerWithResult<*, *>,
    ) {
        override fun toString(): String = name
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun values(): List<ScannerTestConfig> = listOf(
            ScannerTestConfig(
                name = "Android",
                mainScanResultFileName = "scan_result_main_android.json",
                createScanner = { AndroidComposablePreviewScanner() },
            ),
            ScannerTestConfig(
                name = "Glance",
                mainScanResultFileName = "scan_result_main_glance.json",
                createScanner = { GlanceComposablePreviewScanner() },
            ),
            ScannerTestConfig(
                name = "Wear",
                mainScanResultFileName = "scan_result_main_wear.json",
                createScanner = { WearTilePreviewScanner() },
            ),
        )
    }

    @get:Rule
    val composableRule = activityScenarioForActivityRule<MainActivity>()

    @OptIn(RequiresLargeHeap::class)
    @Test(expected = ScanSourceNotSupported::class)
    fun WHEN_scanAllPackages_in_instrumentation_test_THEN_throw_ScanSourceNotSupported_error() {
        config.createScanner()
            .scanAllPackages()
            .getPreviews()
    }

    @Test(expected = ScanSourceNotSupported::class)
    fun WHEN_scanPackageTrees_in_instrumentation_test_THEN_throw_ScanSourceNotSupported_error() {
        config.createScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .getPreviews()
    }

    @Test(expected = ScanSourceNotSupported::class)
    fun WHEN_scanFile_in_instrumentation_test_THEN_throw_ScanSourceNotSupported_error() {
        config.createScanner()
            .scanFile(testFilePath("scan_result.json"))
            .getPreviews()
    }

    @Test
    fun WHEN_scanFile_with_inputStreams_in_instrumentation_test_THEN_do_NOT_throw_ScanSourceNotSupported_error() {
        val assets = getInstrumentation().context.assets
        config.createScanner()
            .scanFile(
                targetInputStream = assets.open(config.mainScanResultFileName),
                customPreviewsInfoInputStream = assets.open("custom_previews.json")
            )
            .getPreviews()
    }
}
