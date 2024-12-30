package sergio.sastre.composable.preview.scanner

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import junit.framework.TestCase.assertEquals
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.Classpath

class AndroidComposablePreviewScannerInstrumentationTest {

    // WARNING! : Execute the unit tests inside "run_before_instrumentation_tests"
    // to generate the "scan_result.json"
    @Test
    fun GIVEN_scan_result_for_all_previews_is_dumped_into_assets_file_WHEN_that_file_is_scanned_THEN_that_file_exists_and_contains_all_previews() {

        val assets = getInstrumentation().context.assets
        val previewsFromFile =
            AndroidComposablePreviewScanner()
                // TODO -> Add Screenshot tests with Dropshots and check whether the classes are loaded
                // TODO 1. For ScreenshotTest sourceSet, they are not loaded hack the sourceSet in gradle?
                // TODO 2. Which ClassLoader is needed when running instrumentation tests? decide based on whether it runs instrumentation test or not?
                // TODO 3. Add Previews under androidTest and add Tests
                // TODO 4. Add new module with custom preview annotations
                // TODO 5. Documentation!!
                //.setTargetSourceSet(Classpath(BuildConfig.BUILD_DIR_PATH, "tmp/kotlin-classes/debug"))
                //.scanPackageTrees("sergio.sastre.composable.preview.scanner")

                .scanFile(
                    targetInputStream = assets.open("scan_result.json"),
                    customPreviewsInfoInputStream = assets.open("custom_previews.json")
                )
                .includePrivatePreviews()
                .getPreviews()

        assertEquals(previewsFromFile.size, 53)
    }
}