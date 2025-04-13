package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ComposablePreviewScannerLoggingTest {

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
        val scanner = AndroidComposablePreviewScanner()
        scanner.scanPackageTrees(
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
            output.contains("@Preview annotation: androidx.compose.ui.tooling.preview.Preview")
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
        val scanner = AndroidComposablePreviewScanner()
        scanner.scanPackageTrees(
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
            output.contains("@Preview annotation: androidx.compose.ui.tooling.preview.Preview")
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
            "Output contaiins time to find @Previews in ms",
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
        val scanner = AndroidComposablePreviewScanner()
        scanner.scanAllPackages().getPreviews()

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
            output.contains("@Preview annotation: androidx.compose.ui.tooling.preview.Preview")
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
            "Output contaiins time to find @Previews in ms",
            "Time to find @Previews: \\d+ ms".toRegex().containsMatchIn(output)
        )
        assertTrue(
            "Output contains total time in ms",
            "Total time: \\d+ ms".toRegex().containsMatchIn(output)
        )
    }
}
