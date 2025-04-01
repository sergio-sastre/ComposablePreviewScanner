package sergio.sastre.composable.preview.scanner.tests.logic

import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import sergio.sastre.composable.preview.scanner.jvm.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvmcommonwithpreviewparameters.CommonStringProvider

class CommonComposablePreviewScannerTest {

    @Test
    fun `GIVEN Composable with common @Preview, WHEN Scanning a package tree with such composables, THEN it returns previews`() {
        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvmcommon")
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
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvmcommonwithpreviewparameters")
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
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.jvmcommonwithpreviewparameters")
                .getPreviews()
                .filter { it.methodName.contains("WithLimit1") }

        assert(previewsWithParameterLimit1.size == 1)
        assert(previewsWithParameterLimit1.first().previewIndex == 0)
    }
}
