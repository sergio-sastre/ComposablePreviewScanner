package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner

@Suppress("DEPRECATION")
class ComposablePreviewToStringTest() {

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

        Assert.assertTrue(startWithClassAndMethodPattern)
        Assert.assertTrue(endWithDigits)
    }

    @Test
    fun `GIVEN Common previews with @PreviewParameters WHEN toString THEN those preview names start with class, method and method parameters type and end with index`() {
        val commonPreviews =
            CommonComposablePreviewScanner()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.common.withpreviewparameters",
                )
                .getPreviews()

        Assume.assumeTrue(commonPreviews.size > 1)

        val startWithClassAndMethodPattern = commonPreviews.all { preview ->
            val previewString = preview.toString()
            val expectedPattern = "${preview.declaringClass}_${preview.methodName}_${preview.methodParametersType}"
            previewString.startsWith(expectedPattern)
        }

        val endWithDigits = commonPreviews.all { preview ->
            preview.toString().substringAfterLast('_').all { it.isDigit() }
        }

        Assert.assertTrue(startWithClassAndMethodPattern)
        Assert.assertTrue(endWithDigits)
    }

    @Test
    fun `GIVEN Common preview WHEN toString THEN those preview names contain class name and method name`() {
        val commonPreview =
            CommonComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.common")
                .getPreviews()
                .first()

        val previewToString = commonPreview.toString()
        val expectedPreviewString = "${commonPreview.declaringClass}_${commonPreview.methodName}"

        Assert.assertEquals(expectedPreviewString, previewToString)
    }

    @Test
    fun `GIVEN a preview with a value-class @PreviewParameter WHEN toString THEN it is scanned without crashing`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("valueclass.previewparameters.android")
                .getPreviews()
                .map { it.toString() }

        // The JVM method name of a value-class-parameter @Composable is mangled as "<name>-<hash>",
        // and we intentionally keep that raw name rather than demangling it. The "<hash>" disambiguates
        // overloads whose value-class parameters share an underlying JVM type: methodParametersType is
        // the erased underlying type (here "float"), so e.g. two same-named previews taking different
        // value classes over Float would otherwise produce identical ids. The hash is
        // compiler-version-dependent, so it is matched loosely rather than asserted verbatim.
        val idPattern = Regex(
            "valueclass\\.previewparameters\\.android\\.ComposablesKt_" +
                "ValueClassPreviewParameterPreview(-\\w+)?_float_\\d",
        )
        Assert.assertEquals(2, previews.size)
        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }
}