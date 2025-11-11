package sergio.sastre.composable.preview.scanner.tests.api.main.scanner

import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import sergio.sastre.composable.preview.scanner.CommonStringProvider
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner

class CommonComposablePreviewScannerTest {

    @Test
    fun `GIVEN Composable with common @Preview, WHEN Scanning a package tree with such composables, THEN it returns previews`() {
        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.common")
                .getPreviews()

        assert(commonPreviews.isNotEmpty())
    }

    @Test
    fun `GIVEN Composable with common @Preview, WHEN Scanning a package tree without such composables, THEN it returns empty`() {
        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvm")
                .getPreviews()

        assert(commonPreviews.isEmpty())
    }

    @Test
    fun `GIVEN Composable with common @Preview and @PreviewParameter, WHEN Scanning that package tree, THEN it returns one preview per parameter`() {
        val stringProviderValuesSize = CommonStringProvider().values.toList().size

        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.common.withpreviewparameters")
                .getPreviews()
                .filter { it.methodName.contains("WithoutLimit") }

        assertEquals(stringProviderValuesSize, commonPreviews.size)
    }

    @Test
    fun `GIVEN preview parameters with StringProvider but limit 1 THEN it creates only 1 preview with index 0`() {
        val stringProviderValues = CommonStringProvider().values.toList()
        assumeTrue(stringProviderValues.size > 1)

        val previewsWithParameterLimit1 =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.common.withpreviewparameters")
                .getPreviews()
                .filter { it.methodName.contains("WithLimit1") }

        assert(previewsWithParameterLimit1.size == 1)
        assert(previewsWithParameterLimit1.first().previewIndex == 0)
    }

    @Test
    fun `GIVEN preview parameters with StringProvider and MultiplePreviews THEN it creates one preview for every combination`() {
        val stringProviderValuesSize = CommonStringProvider().values.toList().size
        /*
        @Preview                        // 1 Preview
        @Preview(showBackground = true) // 1 Preview
        --------------------------------------
                                TOTAL = // 2 Previews
         */
        val multiplePreviewsAmount = 2
        val expectedAmountOfPreviews = stringProviderValuesSize * multiplePreviewsAmount

        val previews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.common.multiplepreviewswithpreviewparameters")
                .getPreviews()

        assertEquals(previews.size, expectedAmountOfPreviews)
    }
}
