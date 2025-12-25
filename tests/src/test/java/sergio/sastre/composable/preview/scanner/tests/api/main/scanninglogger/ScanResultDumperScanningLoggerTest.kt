package sergio.sastre.composable.preview.scanner.tests.api.main.scanninglogger

import org.junit.Assert
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.utils.ScanResultFileName
import sergio.sastre.composable.preview.scanner.utils.SystemOutputTestRule

class ScanResultDumperScanningLoggerTest {

    val regexAnyNumberBut0 = "[1-9]\\d*"

    @get:Rule
    val systemOutputTestRule = SystemOutputTestRule()

    @Test
    fun `GIVEN scan file doesn't exist WHEN logging is not enabled THEN outputs nothing`() {
        // WHEN
        val scanResultFile = testFilePath(ScanResultFileName.SCAN_RESULT_DUMPER_SCANNING_LOGGER_TEST)
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFile(scanResultFile)

            // THEN
            val output = systemOutputTestRule.systemOutput

            Assert.assertTrue(
                "Output does not contain logs",
                output.isEmpty()
            )
        } finally {
            scanResultFile.delete()
        }
    }

    @RequiresShowStandardStreams
    @Test
    fun `GIVEN scan file doesn't exist WHEN Scanning package trees THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = testFilePath(ScanResultFileName.SCAN_RESULT_DUMPER_SCANNING_LOGGER_TEST)
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFile(scanResultFile)

            // THEN
            val output = systemOutputTestRule.systemOutput

            // DOES NOT CONTAIN
            Assert.assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            Assert.assertTrue(
                "Output contains the header",
                output.contains("Scan Result Dumper")
            )
            Assert.assertTrue(
                "Output contains package trees",
                output.contains("Package trees: sergio.sastre.composable.preview.scanner")
            )
            Assert.assertTrue(
                "Output contains name of file to dump scan result",
                output.contains("File to dump Scan Result: ${scanResultFile.absolutePath}")
            )
            Assert.assertTrue(
                "Output contains time to scan files in ms",
                "Time to dump scan result: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )
        } finally {
            scanResultFile.delete()
        }
    }

    @RequiresShowStandardStreams
    @RequiresLargeHeap
    @Test
    fun `GIVEN scan file doesn't exist WHEN Scanning all packages THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = testFilePath(ScanResultFileName.SCAN_RESULT_DUMPER_SCANNING_LOGGER_TEST)
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        try {
            ScanResultDumper()
                .enableScanningLogs()
                .scanAllPackages()
                .dumpScanResultToFile(scanResultFile)

            // THEN
            val output = systemOutputTestRule.systemOutput

            // DOES NOT CONTAIN
            Assert.assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            Assert.assertTrue(
                "Output contains the header",
                output.contains("Scan Result Dumper")
            )
            Assert.assertTrue(
                "Output contains all packages",
                output.contains("Scans all packages")
            )
            Assert.assertTrue(
                "Output contains name of file to dump scan result",
                output.contains("File to dump Scan Result: ${scanResultFile.absolutePath}")
            )
            Assert.assertTrue(
                "Output contains time to scan files in ms",
                "Time to dump scan result: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )
        } finally {
            scanResultFile.delete()
        }
    }

    @RequiresShowStandardStreams
    @Test
    fun `GIVEN scan files don't exist WHEN dumping scan result to file in Assets THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = assetsFilePath(
            fileName = "scan_result0.json",
            variantName = "debug"
        )
        val customPreviewsFile = assetsFilePath(
            fileName = "custom_previews.json",
            variantName = "debug"
        )
        scanResultFile.delete()
        customPreviewsFile.delete()
        assumeFalse(scanResultFile.exists())
        assumeFalse(customPreviewsFile.exists())

        try {
            ScanResultDumper()
                .enableScanningLogs()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFileInAssets(
                    scanFileName = "scan_result0.json",
                    customPreviewsFileName = "custom_previews.json",
                    variantName = "debug"
                )

            // THEN
            val output = systemOutputTestRule.systemOutput

            // DOES NOT CONTAIN
            Assert.assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            Assert.assertTrue(
                "Output contains the header",
                output.contains("Scan Result Dumper")
            )
            Assert.assertTrue(
                "Output contains name of file to dump scan result",
                output.contains("File to dump Scan Result: ${scanResultFile.absolutePath}")
            )
            Assert.assertTrue(
                "Output contains name of file to dump custom previews",
                output.contains("File to dump Custom Previews: ${customPreviewsFile.absolutePath}")
            )
            Assert.assertTrue(
                "Output contains time to scan files in ms",
                "Time to dump scan result: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )
        } finally {
            scanResultFile.delete()
            customPreviewsFile.delete()
        }
    }
}