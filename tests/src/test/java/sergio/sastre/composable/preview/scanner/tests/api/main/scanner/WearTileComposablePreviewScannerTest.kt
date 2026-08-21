package sergio.sastre.composable.preview.scanner.tests.api.main.scanner

import org.junit.Assert.assertTrue
import org.junit.Test
import sergio.sastre.composable.preview.scanner.wear.WearTileComposablePreviewScanner

class WearTileComposablePreviewScannerTest {

    @Test
    fun `GIVEN Composable with wear tile @Preview, WHEN Scanning that package tree, THEN it returns the preview`() {
        val composablePreviews =
            WearTileComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")

        assertTrue(composablePreviews.any { it.methodName == "wearTilePreview" })
    }
}
