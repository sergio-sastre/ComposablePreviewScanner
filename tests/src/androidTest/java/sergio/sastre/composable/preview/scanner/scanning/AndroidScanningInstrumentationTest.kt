package sergio.sastre.composable.preview.scanner.scanning

import androidx.activity.compose.setContent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Rule
import org.junit.Test
import sergio.sastre.composable.preview.scanner.MainActivity
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ClassNotFoundInSourceSetException
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ScanSourceNotSupported
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.uitesting.utils.activityscenario.activityScenarioForActivityRule
import sergio.sastre.uitesting.utils.utils.waitForActivity
import sergio.sastre.uitesting.utils.utils.waitForComposeView

class AndroidScanningInstrumentationTest {

    @get:Rule
    val composableRule = activityScenarioForActivityRule<MainActivity>()

    @OptIn(RequiresLargeHeap::class)
    @Test(expected = ScanSourceNotSupported::class)
    fun WHEN_scanAllPackages_in_instrumentation_test_THEN_throw_ScanSourceNotSupported_error() {
        AndroidComposablePreviewScanner()
            .scanAllPackages()
            .getPreviews()
    }

    @Test(expected = ScanSourceNotSupported::class)
    fun WHEN_scanPackageTrees_in_instrumentation_test_THEN_throw_ScanSourceNotSupported_error() {
        AndroidComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .getPreviews()
    }

    @Test(expected = ScanSourceNotSupported::class)
    fun WHEN_scanFile_in_instrumentation_test_THEN_throw_ScanSourceNotSupported_error() {
        AndroidComposablePreviewScanner()
            .scanFile(testFilePath("scan_result.json"))
            .getPreviews()
    }

    @Test
    fun WHEN_scanFile_with_inputStreams_in_instrumentation_test_THEN_do_NOT_throw_ScanSourceNotSupported_error() {
        val assets = getInstrumentation().context.assets
        AndroidComposablePreviewScanner()
            .scanFile(
                targetInputStream = assets.open("scan_result_main_android.json"),
                customPreviewsInfoInputStream = assets.open("custom_previews.json")
            )
            .getPreviews()
    }

    @Test(expected = ClassNotFoundInSourceSetException::class)
    fun WHEN_scanFile_for_screenshotTest_without_sourceSet_included_THEN_throw_ClassNotFoundInSourceSetException() {
        val assets = getInstrumentation().context.assets
        AndroidComposablePreviewScanner()
            .setTargetSourceSet(Classpath(SourceSet.SCREENSHOT_TEST))
            .scanFile(
                targetInputStream = assets.open("scan_result_screenshot_test.json"),
                customPreviewsInfoInputStream = assets.open("custom_previews.json")
            )
            .getPreviews()
    }
}