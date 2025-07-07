package sergio.sastre.composable.preview.scanner.tests.api.main.scanninglogger

import org.junit.Assert
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.utils.SystemOutputTestRule

@RunWith(Parameterized::class)
class ComposablePreviewScanningLoggerTest<T>(
    // New Scanner instance on each test
    val scannerFactory: () -> Scanner<T>
) {
    val regexAnyNumberBut0 = "[1-9]\\d*"

    class Scanner<T>(
        val previewScanner: ComposablePreviewScanner<T>,
        val annotation: String
    )

    @get:Rule
    val systemOutputTestRule = SystemOutputTestRule()

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Array<Any> {
            return arrayOf(
                {
                    Scanner(
                        previewScanner = AndroidComposablePreviewScanner(),
                        annotation = "androidx.compose.ui.tooling.preview.Preview"
                    )
                },
                {
                    Scanner(
                        previewScanner = CommonComposablePreviewScanner(),
                        annotation = "org.jetbrains.compose.ui.tooling.preview.Preview"
                    )
                },
                {
                    Scanner(
                        previewScanner = GlanceComposablePreviewScanner(),
                        annotation = "androidx.glance.preview.Preview"
                    )
                }
            )
        }
    }

    @Test
    fun `WHEN logging is not enabled THEN outputs nothing`() {
        // WHEN
        val scanner = scannerFactory()
        scanner.previewScanner
            .scanPackageTrees(
                "sergio.sastre.composable.preview.scanner",
            )
            .getPreviews()

        // THEN
        val output = systemOutputTestRule.systemOutput

        Assert.assertTrue(
            "Output does not contain logs",
            output.isEmpty()
        )
    }

    @RequiresShowStandardStreams
    @Test
    fun `WHEN Scanning package trees THEN outputs all scanning info except source set`() {
        // WHEN
        val scanner = scannerFactory()
        scanner.previewScanner
            .enableScanningLogs()
            .scanPackageTrees(
                "sergio.sastre.composable.preview.scanner",
            ).getPreviews()

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
            output.contains("Composable Preview Scanner")
        )
        Assert.assertTrue(
            "Output contains annotation name",
            output.contains("@Preview annotation: ${scanner.annotation}")
        )
        Assert.assertTrue(
            "Output contains amount of @Previews found",
            "Amount of @Previews found: $regexAnyNumberBut0".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains package trees",
            output.contains("Package trees: sergio.sastre.composable.preview.scanner")
        )
        Assert.assertTrue(
            "Output contains time to scan files in ms",
            "Time to scan target files: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains total time in ms",
            "Total time: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
        )
    }

    @RequiresShowStandardStreams
    @Test
    fun `WHEN including and-or excluding package trees THEN outputs all scanning info except source set`() {
        // WHEN
        val scanner = scannerFactory()
        scanner.previewScanner
            .enableScanningLogs()
            .scanPackageTrees(
                include = listOf("sergio.sastre.composable.preview.scanner"),
                exclude = listOf("sergio.sastre.composable.preview.scanner.android.duplicates")
            ).getPreviews()

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
            output.contains("Composable Preview Scanner")
        )
        Assert.assertTrue(
            "Output contains annotation name",
            output.contains("@Preview annotation: ${scanner.annotation}")
        )
        Assert.assertTrue(
            "Output contains amount of @Previews found",
            "Amount of @Previews found: $regexAnyNumberBut0".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains included package trees",
            output.contains("Included package trees: sergio.sastre.composable.preview.scanner")
        )
        Assert.assertTrue(
            "Output contains excluded package trees",
            output.contains("Excluded package trees: sergio.sastre.composable.preview.scanner.android.duplicates")
        )
        Assert.assertTrue(
            "Output contains time to scan files in ms",
            "Time to scan target files: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains total time in ms",
            "Total time: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
        )
    }

    @RequiresShowStandardStreams
    @RequiresLargeHeap
    @Test
    fun `WHEN scanning all packages THEN outputs all scanning info except source set`() {
        // WHEN
        val scanner = scannerFactory()
        scanner.previewScanner.enableScanningLogs().scanAllPackages().getPreviews()

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
            output.contains("Composable Preview Scanner")
        )
        Assert.assertTrue(
            "Output contains annotation name",
            output.contains("@Preview annotation: ${scanner.annotation}")
        )
        Assert.assertTrue(
            "Output contains amount of @Previews found",
            "Amount of @Previews found: $regexAnyNumberBut0".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains all packages",
            output.contains("Scans all packages")
        )
        Assert.assertTrue(
            "Output contains time to scan files in ms",
            "Time to scan target files: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        Assert.assertTrue(
            "Output contains total time in ms",
            "Total time: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
        )
    }

    @RequiresShowStandardStreams
    @Test
    fun `GIVEN file with previews generated WHEN scanning from file THEN outputs all scanning info except source set`() {
        val scanResultFile = testFilePath("scan_result.json")
        Assume.assumeTrue(scanResultFile.exists().not())

        try {
            // GIVEN
            ScanResultDumper()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFile(scanResultFile)

            // WHEN
            assert(scanResultFile.exists())

            val scanner = scannerFactory()
            scanner.previewScanner
                .enableScanningLogs()
                .scanFile(scanResultFile)
                .getPreviews()

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
                output.contains("Composable Preview Scanner")
            )
            Assert.assertTrue(
                "Output contains annotation name",
                output.contains("@Preview annotation: ${scanner.annotation}")
            )
            Assert.assertTrue(
                "Output contains amount of @Previews found",
                "Amount of @Previews found: $regexAnyNumberBut0".toRegex().containsMatchIn(output)
            )
            Assert.assertTrue(
                "Output contains from file",
                output.contains("Scans from file: scan_result.json")
            )
            Assert.assertTrue(
                "Output contains time to scan files in ms",
                "Time to scan target files: $regexAnyNumberBut0 ms".toRegex()
                    .containsMatchIn(output)
            )
            Assert.assertTrue(
                "Output contains time to find @Previews in ms",
                "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
            )
            Assert.assertTrue(
                "Output contains total time in ms",
                "Total time: $regexAnyNumberBut0 ms".toRegex().containsMatchIn(output)
            )

        } finally {
            scanResultFile.delete()
        }
    }
}