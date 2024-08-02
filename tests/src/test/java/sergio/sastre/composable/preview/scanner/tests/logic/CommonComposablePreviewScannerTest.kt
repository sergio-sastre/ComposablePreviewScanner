package sergio.sastre.composable.preview.scanner.tests.logic

import org.junit.Test
import sergio.sastre.composable.preview.scanner.jvm.common.CommonComposablePreviewScanner

class CommonComposablePreviewScannerTest {

    @Test
    fun `GIVEN Composable with common @Preview, WHEN Scanning a package tree with such composables, THEN it returns previews`() {
        val desktopPreviews =
            CommonComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvmcommon")
            .getPreviews()

        assert(desktopPreviews.isNotEmpty())
    }

    @Test
    fun `GIVEN Composable with common @Preview, WHEN Scanning a package tree without such composables, THEN it returns empty`() {
        val desktopPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvm")
                .getPreviews()

        assert(desktopPreviews.isEmpty())
    }
}
