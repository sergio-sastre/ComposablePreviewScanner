package sergio.sastre.composable.preview.scanner.run_before_instrumentation_tests

import org.junit.Test
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath

class SaveScanResultInAssets {
    @Test
    fun `task -- save scan result in assets`() {
        val scanResultScreenshotTestFileName = "scan_result_screenshot_test.json"
        val scanResultMainFileName = "scan_result_main.json"
        val scanResultAndroidTestFileName = "scan_result_android_test.json"

        ScanResultDumper()
            .setTargetSourceSet(Classpath(SCREENSHOT_TEST))
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .dumpScanResultToFileInAssets(
                scanFileName = scanResultScreenshotTestFileName,
                packageTreesOfCustomPreviews = listOf(
                    "androidx.compose.ui.tooling.preview",
                    "sergio.sastre.composable.preview.custompreviews"
                )
            )

        ScanResultDumper()
            .setTargetSourceSet(Classpath(MAIN))
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .dumpScanResultToFileInAssets(
                scanFileName = scanResultMainFileName,
                packageTreesOfCustomPreviews = listOf(
                    "androidx.compose.ui.tooling.preview",
                    "sergio.sastre.composable.preview.custompreviews"
                )
            )

        ScanResultDumper()
            .setTargetSourceSet(Classpath(ANDROID_TEST))
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .dumpScanResultToFileInAssets(
                scanFileName = scanResultAndroidTestFileName,
                packageTreesOfCustomPreviews = listOf(
                    "androidx.compose.ui.tooling.preview",
                    "sergio.sastre.composable.preview.custompreviews"
                )
            )

        assert(
            assetsFilePath(scanResultScreenshotTestFileName).exists()
        )
        assert(
            assetsFilePath(scanResultMainFileName).exists()
        )
        assert(
            assetsFilePath(scanResultAndroidTestFileName).exists()
        )
    }
}