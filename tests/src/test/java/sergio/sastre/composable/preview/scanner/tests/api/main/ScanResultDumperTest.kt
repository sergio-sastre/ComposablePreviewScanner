package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.Assert.assertEquals
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
import java.io.File
import java.nio.file.Files

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

    @Test
    fun `GIVEN wear compose previews WHEN scan result is dumped with default custom previews THEN scanFile finds all of them`() {
        val scanResultFile = assetsFilePath(
            fileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
            variantName = "debug"
        )
        val customPreviewsFile = assetsFilePath(
            fileName = "custom_previews.json",
            variantName = "debug"
        )
        scanResultFile.delete()
        customPreviewsFile.delete()
        assumeFalse(scanResultFile.exists())

        val wearPackage = "sergio.sastre.composable.preview.scanner.android.multiplepreviews.wear"
        try {
            ScanResultDumper()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees(wearPackage)
                .dumpScanResultToFileInAssets(
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                    variantName = "debug"
                )

            val expectedPreviews = AndroidComposablePreviewScanner()
                .scanPackageTrees(wearPackage)
                .getPreviews()

            val previewsFromFile = AndroidComposablePreviewScanner()
                .setTargetSourceSet(Classpath(MAIN))
                .scanFile(
                    targetInputStream = scanResultFile.inputStream(),
                    customPreviewsInfoInputStream = customPreviewsFile.inputStream()
                )
                .getPreviews()

            assertEquals(
                expectedPreviews.map { "${it.previewInfo}|${it.declaringClass}|${it.methodName}" }.sorted(),
                previewsFromFile.map { "${it.previewInfo}|${it.declaringClass}|${it.methodName}" }.sorted()
            )
        } finally {
            scanResultFile.delete()
            customPreviewsFile.delete()
        }
    }

    @Test
    fun `GIVEN custom assets directory WHEN dumping scan result to file THEN custom_previews is also dumped in that directory`() {
        val customAssetsDir = Files.createTempDirectory("custom-assets-dir").toFile()
        val scanResultFile = File(customAssetsDir, ScanResultFileName.SCAN_RESULT_DUMPER_TEST)
        val customPreviewsFile = File(customAssetsDir, "custom_previews.json")

        try {
            ScanResultDumper()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.multiplepreviewscrossmodule")
                .dumpScanResultToFile(scanResultFile)

            assert(scanResultFile.exists())
            assert(customPreviewsFile.exists()) {
                "Expected custom_previews.json in custom assets dir (${customAssetsDir.absolutePath}), but it was not created"
            }
        } finally {
            customAssetsDir.deleteRecursively()
        }
    }
}