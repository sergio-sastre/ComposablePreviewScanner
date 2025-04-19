package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.utils.SystemOutputTestRule

class ScanResultDumperScanningLoggerTest {

    val regexAnyNumberBut0 = "[1-9]\\d*"

    @get:Rule
    val systemOutputTestRule = SystemOutputTestRule()


    @Test
    fun `WHEN logging is disabled THEN outputs nothing`() {
        // WHEN
        val scanResultFile = testFilePath("scan_result.json")
        Assume.assumeTrue(scanResultFile.exists().not())

        try {
            ScanResultDumper()
                .disableLogging()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.included",
                    "sergio.sastre.composable.preview.scanner.multiplepreviews"
                )
                .dumpScanResultToFile(scanResultFile)

            // THEN
            val output = systemOutputTestRule.systemOutput

            assertTrue(
                "Output does not contain logs",
                output.isEmpty()
            )
        } finally {
            scanResultFile.delete()
        }
    }

    @Test
    fun `WHEN Scanning package trees THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = testFilePath("scan_result.json")
        Assume.assumeTrue(scanResultFile.exists().not())

        try {
            ScanResultDumper()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.included",
                    "sergio.sastre.composable.preview.scanner.multiplepreviews"
                )
                .dumpScanResultToFile(scanResultFile)

            // THEN
            val output = systemOutputTestRule.systemOutput

            // DOES NOT CONTAIN
            assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            assertTrue(
                "Output contains the header",
                output.contains("Scan Result Dumper")
            )
            assertTrue(
                "Output contains package trees",
                output.contains("Package trees: sergio.sastre.composable.preview.scanner.included, sergio.sastre.composable.preview.scanner.multiplepreviews")
            )
            assertTrue(
                "Output contains name of file to dump scan result",
                output.contains("File to dump Scan result: ${scanResultFile.absolutePath}")
            )
            assertTrue(
                "Output contains time to scan files in ms",
                "Time to dump scan result: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )
        } finally {
            scanResultFile.delete()
        }
    }


    @OptIn(RequiresLargeHeap::class)
    @Test
    fun `WHEN Scanning all packages THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = testFilePath("scan_result.json")
        Assume.assumeTrue(scanResultFile.exists().not())

        try {
            ScanResultDumper()
                .scanAllPackages()
                .dumpScanResultToFile(scanResultFile)

            // THEN
            val output = systemOutputTestRule.systemOutput

            // DOES NOT CONTAIN
            assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            assertTrue(
                "Output contains the header",
                output.contains("Scan Result Dumper")
            )
            assertTrue(
                "Output contains all packages",
                output.contains("Scans all packages")
            )
            assertTrue(
                "Output contains name of file to dump scan result",
                output.contains("File to dump Scan result: ${scanResultFile.absolutePath}")
            )
            assertTrue(
                "Output contains time to scan files in ms",
                "Time to dump scan result: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )
        } finally {
            scanResultFile.delete()
        }
    }

    @Test
    fun `WHEN dumping scan result to file in Assets THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = assetsFilePath(
            fileName = "scan_result.json",
            variantName = "debug"
        )
        val customPreviewsFile = assetsFilePath(
            fileName = "custom_previews.json",
            variantName = "debug"
        )
        Assume.assumeTrue(scanResultFile.exists().not())
        Assume.assumeTrue(customPreviewsFile.exists().not())

        try {
            ScanResultDumper()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFileInAssets(
                    scanFileName = "scan_result.json",
                    customPreviewsFileName = "custom_previews.json",
                    variantName = "debug"
                )

            // THEN
            val output = systemOutputTestRule.systemOutput

            // DOES NOT CONTAIN
            assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            assertTrue(
                "Output contains the header",
                output.contains("Scan Result Dumper")
            )
            assertTrue(
                "Output contains name of file to dump scan result",
                output.contains("File to dump Scan result: ${scanResultFile.absolutePath}")
            )
            assertTrue(
                "Output contains name of file to dump custom previews",
                output.contains("File to dump Custom Previews: ${customPreviewsFile.absolutePath}")
            )
            assertTrue(
                "Output contains time to scan files in ms",
                "Time to dump scan result: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )
        } finally {
            scanResultFile.delete()
            customPreviewsFile.delete()
        }
    }
}