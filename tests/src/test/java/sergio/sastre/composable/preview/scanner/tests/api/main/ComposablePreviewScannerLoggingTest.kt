package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
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
    fun `WHEN Scanning package trees THEN outputs scanning times after scan operation`() {
        // WHEN
        val scanner = AndroidComposablePreviewScanner()
        scanner.scanPackageTrees("sergio.sastre.composable.preview.scanner")

        // THEN
        val output = outputStreamCaptor.toString().trim()
        assertTrue(
            "Output contains the header",
            output.contains("Composable Preview Scanner\n=====================")
        )
        assertTrue(
            "Output contains annotation name",
            output.contains("Annotation: androidx.compose.ui.tooling.preview.Preview")
        )
        assertFalse(
            "Output does not contain source set",
            output.contains("Source set (compiled classes path)")
        )
        assertTrue(
            "Output contains package trees",
            output.contains("Package trees: sergio.sastre.composable.preview.scanner")
        )
        assertTrue(
            "Output should include scanning time in ms",
            "Scanning time: \\d+ ms".toRegex().containsMatchIn(output)
        )
    }

    /*
    @Test
    fun `printScanningTimes should indicate when no scans have been performed`() {
        // Arrange
        val scanner = createConcreteScanner()

        // Act - Print times without performing any scan
        scanner.printScanningTimes()

        // Assert
        val output = outputStreamCaptor.toString().trim()
        assertEquals("No scanning operations performed yet.", output)
    }

    // Helper method to create a temporary JSON file for testing
    private fun createTempJsonFile(): File {
        val tempFile = Files.createTempFile("test", ".json").toFile()
        tempFile.writeText("""{"previews":[]}""") // Minimal valid content
        return tempFile
    }

     */
}
