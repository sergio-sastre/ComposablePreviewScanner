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

    private val repeatablePreviewPackages = arrayOf(
        // @PreviewFontScale, @PreviewScreenSizes, and wear subpackage (@WearPreviewDevices, @WearPreviewFontScales, @WearPreviewSmallRound, @WearPreviewLargeRound, @WearPreviewSquare)
        "sergio.sastre.composable.preview.scanner.android.multiplepreviews",
        // @PreviewLightDark
        "sergio.sastre.composable.preview.scanner.android.previewwrapper",
        // @PreviewDynamicColors (also uses @MyCustomDarkModePreview from custompreviewannotation)
        "sergio.sastre.composable.preview.scanner.android.multiplepreviewswithpreviewparameters",
        "sergio.sastre.composable.preview.scanner.android.custompreviewannotation",
    )

    private fun assertDumpedPreviewsMatchBuildTime(
        scanResultFile: File,
        customPreviewsFile: File,
        vararg packages: String,
    ) {
        val expectedPreviews = AndroidComposablePreviewScanner()
            .scanPackageTrees(*packages)
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
    }

    @Test
    fun `GIVEN wear and repeatable android compose previews WHEN scan result is dumped with default custom previews THEN scanFile finds all of them`() {
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

        try {
            ScanResultDumper()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees(*repeatablePreviewPackages)
                .dumpScanResultToFileInAssets(
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                    variantName = "debug"
                )

            assertDumpedPreviewsMatchBuildTime(
                scanResultFile = scanResultFile,
                customPreviewsFile = customPreviewsFile,
                *repeatablePreviewPackages,
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
                .dumpScanResultToFileInAssets(
                    assetsDir = customAssetsDir,
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                    packageTreesOfCustomPreviews = listOf(
                        "androidx.compose.ui.tooling.preview",
                        "sergio.sastre.composable.preview.custompreviews"
                    )
                )

            assert(scanResultFile.exists())
            assert(customPreviewsFile.exists()) {
                "Expected custom_previews.json in custom assets dir (${customAssetsDir.absolutePath}), but it was not created"
            }
        } finally {
            customAssetsDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN wear and repeatable android compose previews WHEN scan result is dumped into custom assetsDir with default custom previews THEN scanFile finds all of them`() {
        val customAssetsDir = Files.createTempDirectory("custom-assets-dir").toFile()
        val scanResultFile = File(customAssetsDir, ScanResultFileName.SCAN_RESULT_DUMPER_TEST)
        val customPreviewsFile = File(customAssetsDir, "custom_previews.json")

        try {
            ScanResultDumper()
                .setTargetSourceSet(Classpath(MAIN))
                .scanPackageTrees(*repeatablePreviewPackages)
                .dumpScanResultToFileInAssets(
                    assetsDir = customAssetsDir,
                    scanFileName = ScanResultFileName.SCAN_RESULT_DUMPER_TEST,
                )

            assertDumpedPreviewsMatchBuildTime(
                scanResultFile = scanResultFile,
                customPreviewsFile = customPreviewsFile,
                *repeatablePreviewPackages,
            )
        } finally {
            customAssetsDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN user_dir differs from module dir WHEN using Classpath(MAIN) THEN ScanResultDumper still finds previews`() {
        val originalUserDir = System.getProperty("user.dir")
        val simulatedGradleRootDir = Files.createTempDirectory("simulated-gradle-root").toFile()
        val outputDir = Files.createTempDirectory("scan-output").toFile()
        val scanResultFile = File(outputDir, ScanResultFileName.SCAN_RESULT_DUMPER_TEST)

        try {
            System.setProperty("user.dir", simulatedGradleRootDir.absolutePath)

            ScanResultDumper()
                .setTargetSourceSet(Classpath(MAIN, rootDir = "$originalUserDir/build"))
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.androidvals")
                .dumpScanResultToFile(scanResultFile)

            val previewsFromFile = AndroidComposablePreviewScanner()
                .scanFile(scanResultFile)
                .getPreviews()

            assert(previewsFromFile.isNotEmpty()) {
                "Expected previews from MAIN source set, but got 0 because Classpath(MAIN) resolved against user.dir (${simulatedGradleRootDir.absolutePath})"
            }
        } finally {
            System.setProperty("user.dir", originalUserDir)
            simulatedGradleRootDir.deleteRecursively()
            outputDir.deleteRecursively()
        }
    }
}