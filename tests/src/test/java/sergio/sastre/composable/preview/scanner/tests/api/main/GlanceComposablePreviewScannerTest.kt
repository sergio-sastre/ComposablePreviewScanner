package sergio.sastre.composable.preview.scanner.tests.api.main

import org.junit.Test
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner

class GlanceComposablePreviewScannerTest {

    @Test
    fun `GIVEN glance composable`() {
        val composablePreviews =
            GlanceComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.glance")
                .getPreviews()

        assert(composablePreviews.size == 1)
    }
}