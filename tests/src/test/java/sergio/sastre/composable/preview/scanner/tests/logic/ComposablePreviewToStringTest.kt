package sergio.sastre.composable.preview.scanner.tests.logic

import org.junit.Assume.assumeTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `GIVEN Android previews with @PreviewParameters WHEN toString THEN those preview names start with class, method and method parameters type and end with index`() {
        val androidPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.previewparameters",
                )
                .getPreviews()

        val startWithClassAndMethodPattern = androidPreviews.all { preview ->
            val previewString = preview.toString()
            val expectedPattern = "${preview.declaringClass}_${preview.methodName}_${preview.methodParametersType}"
            previewString.startsWith(expectedPattern)
        }

        val endWithDigits = androidPreviews.all { preview ->
            preview.toString().substringAfterLast('_').all { it.isDigit() }
        }

        assertTrue(startWithClassAndMethodPattern)
        assertTrue(endWithDigits)
    }

    @Test
    fun `GIVEN Common previews with @PreviewParameters WHEN toString THEN those preview names start with class, method and method parameters type and end with index`() {
        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.jvmcommonwithpreviewparameters",
                )
                .getPreviews()

        assumeTrue(commonPreviews.size > 1)

        val startWithClassAndMethodPattern = commonPreviews.all { preview ->
            val previewString = preview.toString()
            val expectedPattern = "${preview.declaringClass}_${preview.methodName}_${preview.methodParametersType}"
            previewString.startsWith(expectedPattern)
        }

        val endWithDigits = commonPreviews.all { preview ->
            preview.toString().substringAfterLast('_').all { it.isDigit() }
        }

        assertTrue(startWithClassAndMethodPattern)
        assertTrue(endWithDigits)
    }

    @Test
    fun `GIVEN Common preview WHEN toString THEN those preview names contain class name and method name`() {
        val commonPreview =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .first()

        val previewToString = commonPreview.toString()
        val expectedPreviewString = "${commonPreview.declaringClass}_${commonPreview.methodName}"

        assertEquals(expectedPreviewString, previewToString)
    }
}