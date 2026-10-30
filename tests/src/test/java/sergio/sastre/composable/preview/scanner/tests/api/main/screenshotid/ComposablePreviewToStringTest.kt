package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner

@Suppress("DEPRECATION")
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
    fun `GIVEN a preview with a nullable value-class @PreviewParameter WHEN toString THEN it includes alphanumeric hashcode and value class name`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.name == "valueClassDp" }
                .getPreviews()
                .map { it.toString() }

        Assert.assertTrue(previews.isNotEmpty())

        val hashCodeRegex = "(-[a-zA-Z0-9]{7,11})"
        val idPattern = Regex(
            NON_PRIMITIVE_CLASS_PATH +
                "ValueClassPreviewParameterPreview${hashCodeRegex}_Dp_\\d",
        )

        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }

    @Test
    fun `GIVEN a preview with a List of value-class @PreviewParameter WHEN toString THEN it also includes diamond parenthesis`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.name == "valueClassListDp" }
                .getPreviews()
                .map { it.toString() }

        val idPattern = Regex(
            NON_PRIMITIVE_CLASS_PATH +
                "ListValueClassPreviewParameterPreview_List<Dp>_\\d",
        )

        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }

    @Test
    fun `GIVEN a preview with an Array of value-class @PreviewParameter WHEN toString THEN it also includes square parenthesis`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.name == "valueClassArrayDp" }
                .getPreviews()
                .map { it.toString() }

        val idPattern = Regex(
            NON_PRIMITIVE_CLASS_PATH +
                "ArrayValueClassPreviewParameterPreview_Dp\\[\\]_\\d",
        )

        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }

    @Test
    fun `GIVEN a preview with a regular class @PreviewParameter WHEN toString THEN the class name is in the name`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.name == "myClass" }
                .getPreviews()
                .map { it.toString() }

        val idPattern = Regex(
            NON_PRIMITIVE_CLASS_PATH +
                "MyClassPreviewParameterPreview_MyClass_\\d",
        )

        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }

    @Test
    fun `GIVEN a preview with a List of wildcard @PreviewParameter WHEN toString THEN wildcard translates to question mark`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.name == "wildcardList" }
                .getPreviews()
                .map { it.toString() }

        val idPattern = Regex(
            NON_PRIMITIVE_CLASS_PATH +
                "WildcardList-with-hypen-backsticks_List<\\?>_\\d",
        )

        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }

    @Test
    fun `GIVEN a preview with a nested class @PreviewParameter WHEN toString THEN the nested class name includes a dollar sign`() {
        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.name == "innerClass" }
                .getPreviews()
                .map { it.toString() }

        val idPattern = Regex(
            NON_PRIMITIVE_CLASS_PATH +
                "InnerClassPreviewParameterPreview_Outer\\\$Inner_\\d",
        )

        Assert.assertTrue(previews.toString(), previews.all { idPattern.matches(it) })
    }


    @Test
    fun `GIVEN overloaded wear previews WHEN toString THEN each preview includes its parameter type`() {
        val previews =
            WearTilePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
                .getPreviews()
                .filter { it.methodName == "tilePreviewWithResourcesAndPlatformData" }

        Assert.assertEquals(2, previews.size)


        val previewNames = previews.map { it.toString() }.toSet()
        Assert.assertEquals(2, previewNames.size)
    }

    companion object {
        const val NON_PRIMITIVE_CLASS_PATH =
            "sergio\\.sastre\\.composable\\.preview\\.scanner\\.android\\.previewparameters\\.NonPrimitiveClassComposablesKt_"
    }
}
