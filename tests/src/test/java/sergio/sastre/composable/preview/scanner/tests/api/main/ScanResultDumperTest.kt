package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.Assume.assumeFalse
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.*
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.utils.ScanResultFileName

class ScanResultDumperTest {

    @OptIn(RequiresLargeHeap::class)
    @Test
    fun `GIVEN a scan result file doesn't exist WHEN scan result for all previews is dumped into that file THEN that file exists and contains all previews`() {
        val scanResultFile = testFilePath(ScanResultFileName.SCAN_RESULT_DUMPER_TEST)
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .scanAllPackages()
                .dumpScanResultToFile(scanResultFile)

            assert(scanResultFile.exists())

            val allPreviews = AndroidComposablePreviewScanner()
                .scanAllPackages()
                .getPreviews()

            val previewsFromFile =
                AndroidComposablePreviewScanner()
                    .scanFile(scanResultFile)
                    .getPreviews()

            assert(allPreviews.toString() == previewsFromFile.toString())

        } finally {
            scanResultFile.delete()
        }
    }

    @Test
    fun `GIVEN a scan result file does not exist WHEN scan result is dumped into that file with a flavour THEN that file exists in that flavour`() {
        val scanResultFile = assetsFilePath(
            fileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
            variantName = "debug"
        )
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFileInAssets(
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                    variantName = "debug"
                )

            assert(scanResultFile.exists())
            assert(scanResultFile.path.contains("src/androidTestDebug/assets"))
        } finally {
            scanResultFile.delete()
            assetsFilePath(
                fileName = "custom_previews.json",
                variantName = "debug"
            ).delete()
        }
    }

    @Test
    fun `GIVEN a scan result file does not exist WHEN scan result for some previews is dumped into that file THEN that file contains those previews`() {
        val scanResultFile = assetsFilePath(
            fileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
            variantName = "debug"
        )
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFileInAssets(
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                    variantName = "debug"
                )

            val previews = AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()

            val previewsFromFile =
                AndroidComposablePreviewScanner()
                    .scanFile(scanResultFile)
                    .getPreviews()

            assert(previews.toString() == previewsFromFile.toString())
        } finally {
            scanResultFile.delete()
            assetsFilePath(
                fileName = "custom_previews.json",
                variantName = "debug"
            ).delete()
        }
    }

    @Test
    fun `GIVEN a scan result file does not exist WHEN scan result for some previews in MAIN is dumped into that file THEN that file contains those previews`() {
        val scanResultFile = assetsFilePath(
            fileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
            variantName = "debug"
        )
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFileInAssets(
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                    variantName = "debug"
                )

            val previews = AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()

            val previewsFromFile =
                AndroidComposablePreviewScanner()
                    .setTargetSourceSet(Classpath(MAIN))
                    .scanFile(scanResultFile)
                    .getPreviews()

            assert(previews.toString() == previewsFromFile.toString())
        } finally {
            scanResultFile.delete()
            assetsFilePath(
                fileName = "custom_previews.json",
                variantName = "debug"
            ).delete()
        }
    }
}