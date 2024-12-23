package sergio.sastre.composable.preview.scanner

import junit.framework.TestCase.assertEquals
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner

class AndroidComposablePreviewScannerInstrumentationTest {

    // WARNING! : Execute the unit tests inside "run_before_instrumentation_tests"
    // to generate the "scan_result.json"
    @Test
    fun GIVEN_scan_result_for_all_previews_is_dumped_into_assets_file_WHEN_that_file_is_scanned_THEN_that_file_exists_and_contains_all_previews() {
        val previewsFromFile =
            AndroidComposablePreviewScanner()
                //.overrideClasspath(BuildConfig.BUILD_DIR_PATH, "tmp/kotlin-classes/debug")
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                //.scanFile(getInstrumentation().context.assets.open("scan_result.json"))
                .includePrivatePreviews()
                .getPreviews()

        assertEquals(previewsFromFile.size, 42)
    }
}