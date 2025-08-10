package sergio.sastre.composable.preview.scanner.tests.api.main.scanner

import org.junit.Test
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner

class GlanceComposablePreviewScannerTest {

    @Test
    fun `GIVEN Composable with glance @Previews, WHEN Scanning a package tree with such composables, THEN it returns previews`() {
        val composablePreviews =
            GlanceComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()

        assert(composablePreviews.isNotEmpty())
    }
}