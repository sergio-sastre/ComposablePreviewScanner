package sergio.sastre.composable.preview.scanner.tests.logic

import org.junit.Test
import sergio.sastre.composable.preview.scanner.jvm.JvmAnnotationScanner

class JvmAnnotationScannerTest {

    @Test
    fun `GIVEN Composable with @VisibleForScreenshotTesting, WHEN Scanning a package tree with such composables, THEN it returns previews`() {
        val desktopPreviews = JvmAnnotationScanner("sergio.sastre.composable.preview.scanner.VisibleForScreenshotTesting")
            .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvm")
            .getPreviews()

        assert(desktopPreviews.isNotEmpty())
    }
}