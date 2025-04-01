package sergio.sastre.composable.preview.scanner.tests.logic

import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.common.CommonComposablePreviewScanner

class ComposablePreviewToStringTest {

    @Test
    fun `GIVEN Android previews WHEN toString THEN those preview names do not end with underscore`() {
        val androidPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
                .map { it.toString() }

        assert(androidPreviews.isNotEmpty())
        assert(androidPreviews.all { it.last() != '_' })
    }

    @Test
    fun `GIVEN Common previews WHEN toString THEN those preview names do not end with underscore`() {
        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .includePrivatePreviews()
                .getPreviews()
                .map { it.toString() }

        assert(commonPreviews.isNotEmpty())
        assert(commonPreviews.all { it.last() != '_' })
    }

    // TODO - test that the toString() includes all the relevant parameters for unique IDs
}