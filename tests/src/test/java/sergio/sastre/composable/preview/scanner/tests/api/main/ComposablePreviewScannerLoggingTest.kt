package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.jvm.common.CommonComposablePreviewScanner
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@RunWith(Parameterized::class)
class ComposablePreviewScannerLoggingTest<T>(
    val scanner: Scanner<T>
) {

    class Scanner<T>(
        val previewScanner: ComposablePreviewScanner<T>,
        val annotation: String
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Array<Any> {
            return arrayOf(
                Scanner(
                    previewScanner = AndroidComposablePreviewScanner(),
                    annotation = "androidx.compose.ui.tooling.preview.Preview"
                ),
                Scanner(
                    previewScanner = CommonComposablePreviewScanner(),
                    annotation = "org.jetbrains.compose.ui.tooling.preview.Preview"
                )
            )
        }
    }

    // Fields to store the original System.out and our capturing stream
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @Before
    fun setUp() {
        // Redirect System.out to our ByteArrayOutputStream
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @After
    fun tearDown() {
        // Restore the original System.out after each test
        System.setOut(standardOut)
    }

    @Test
    fun `WHEN Scanning package trees THEN outputs all scanning info except source set`() {
        // WHEN
        scanner.previewScanner
            .scanPackageTrees(
                "sergio.sastre.composable.preview.scanner.included",
                "sergio.sastre.composable.preview.scanner.multiplepreviews"
            ).getPreviews()

        // THEN
        val output = outputStreamCaptor.toString().trim()

        // DOES NOT CONTAIN
        assertFalse(
            "Output does not contain source set",
            output.contains("Source set (compiled classes path)")
        )

        // DOES CONTAIN
        assertTrue(
            "Output contains the header",
            output.contains("Composable Preview Scanner")
        )
        assertTrue(
            "Output contains annotation name",
            output.contains("@Preview annotation: ${scanner.annotation}")
        )
        assertTrue(
            "Output contains package trees",
            output.contains("Package trees: sergio.sastre.composable.preview.scanner.included, sergio.sastre.composable.preview.scanner.multiplepreviews")
        )
        assertTrue(
            "Output contains time to scan files in ms",
            "Time to scan target files: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contaiins time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contains total time in ms",
            "Total time: \\d+ ms".toRegex().containsMatchIn(output)
        )
    }

    @Test
    fun `WHEN including and-or excluding package trees THEN outputs all scanning info except source set`() {
        // WHEN
        scanner.previewScanner
            .scanPackageTrees(
                include = listOf("sergio.sastre.composable.preview.scanner"),
                exclude = listOf("sergio.sastre.composable.preview.scanner.duplicates")
            ).getPreviews()

        // THEN
        val output = outputStreamCaptor.toString().trim()

        // DOES NOT CONTAIN
        assertFalse(
            "Output does not contain source set",
            output.contains("Source set (compiled classes path)")
        )

        // DOES CONTAIN
        assertTrue(
            "Output contains the header",
            output.contains("Composable Preview Scanner")
        )
        assertTrue(
            "Output contains annotation name",
            output.contains("@Preview annotation: ${scanner.annotation}")
        )
        assertTrue(
            "Output contains included package trees",
            output.contains("Included package trees: sergio.sastre.composable.preview.scanner")
        )
        assertTrue(
            "Output contains excluded package trees",
            output.contains("Excluded package trees: sergio.sastre.composable.preview.scanner.duplicates")
        )
        assertTrue(
            "Output contains time to scan files in ms",
            "Time to scan target files: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contains time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contains total time in ms",
            "Total time: \\d+ ms".toRegex().containsMatchIn(output)
        )
    }

    @OptIn(RequiresLargeHeap::class)
    @Test
    fun `WHEN scanning all packages THEN outputs all scanning info except source set`() {
        // WHEN
        scanner.previewScanner.scanAllPackages().getPreviews()

        // THEN
        val output = outputStreamCaptor.toString().trim()

        // DOES NOT CONTAIN
        assertFalse(
            "Output does not contain source set",
            output.contains("Source set (compiled classes path)")
        )

        // DOES CONTAIN
        assertTrue(
            "Output contains the header",
            output.contains("Composable Preview Scanner")
        )
        assertTrue(
            "Output contains annotation name",
            output.contains("@Preview annotation: ${scanner.annotation}")
        )
        assertTrue(
            "Output contains all packages",
            output.contains("Scans all packages")
        )
        assertTrue(
            "Output contains time to scan files in ms",
            "Time to scan target files: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contains time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contains total time in ms",
            "Total time: \\d+ ms".toRegex().containsMatchIn(output)
        )
    }

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

            scanner.previewScanner
                .scanFile(scanResultFile)
                .getPreviews()

            // THEN
            val output = outputStreamCaptor.toString().trim()

            // DOES NOT CONTAIN
            assertFalse(
                "Output does not contain source set",
                output.contains("Source set (compiled classes path)")
            )

            // DOES CONTAIN
            assertTrue(
                "Output contains the header",
                output.contains("Composable Preview Scanner")
            )
            assertTrue(
                "Output contains annotation name",
                output.contains("@Preview annotation: ${scanner.annotation}")
            )
            assertTrue(
                "Output contains from file",
                output.contains("Scans from file: scan_result.json")
            )
            assertTrue(
                "Output contains time to scan files in ms",
                "Time to scan target files: \\d+ ms".toRegex().containsMatchIn(output)
            )
            assertTrue(
                "Output contains time to find @Previews in ms",
                "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
            )
            assertTrue(
                "Output contains total time in ms",
                "Total time: \\d+ ms".toRegex().containsMatchIn(output)
            )

        } finally {
            scanResultFile.delete()
        }
    }
}