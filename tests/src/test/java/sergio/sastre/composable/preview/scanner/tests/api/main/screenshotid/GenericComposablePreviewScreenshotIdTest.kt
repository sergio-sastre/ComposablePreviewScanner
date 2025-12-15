package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.utils.AndroidScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.CommonScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.GlanceScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.PreviewScreenshotIdBuilderProvider

@RunWith(Parameterized::class)
class GenericComposablePreviewScreenshotIdTest(
    private val screenshotIdBuilder: PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<*>>
) {

    companion object {
        private const val ALL_UNSAFE_CHARS = "<>:\\\"/\\\\|?*"

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Any> {
            return listOf(
                AndroidScreenshotIdBuilderProvider(),
                CommonScreenshotIdBuilderProvider(),
                GlanceScreenshotIdBuilderProvider()
            )
        }
    }

    @Test
    fun `GIVEN preview with only previewIndex, THEN show only index`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    previewIndex = 1,
                )
                .build()

        assert(
            previewScreenshotId == "1"
        )
    }

    @Test
    fun `GIVEN preview with only width greater than -1, THEN show only W$value$dp`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    widthDp = 33,
                )
                .build()

        assert(
            previewScreenshotId == "W33dp"
        )
    }

    @Test
    fun `GIVEN preview with only height greater than -1, THEN show only H$value$dp`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    heightDp = 33,
                )
                .build()

        assert(
            previewScreenshotId == "H33dp"
        )
    }

    @Test
    fun `GIVEN preview className and methodName, THEN show them only but separated by a dot`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    declaringClass = "MyClass",
                    methodName = "PreviewName",
                )
                .build()

        assert(
            previewScreenshotId == "MyClass.PreviewName"
        )
    }

    @Test
    fun `GIVEN methodName ignored, THEN methodName is not included`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    methodName = "PreviewName",
                )
                .ignoreMethodName()
                .build()

        assert(
            !previewScreenshotId.contains("PreviewName")
        )
    }

    @Test
    fun `GIVEN className ignored, THEN declaringClass is not included`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    declaringClass = "MyClass",
                )
                .ignoreClassName()
                .build()

        assert(
            !previewScreenshotId.contains("MyClass")
        )
    }

    @Test
    fun `GIVEN methodParameters not ignored, THEN methodParameters is included`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    methodParameters = "name_String",
                )
                .doNotIgnoreMethodParametersType()
                .build()

        assert(
            previewScreenshotId.contains("name_String")
        )
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are not ignored, THEN the screenshotIds differ`() {
        val screenshotIdPreview1 =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    methodParameters = "name_String",
                )
                .doNotIgnoreMethodParametersType()
                .build()

        val screenshotIdPreview2 =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    methodParameters = "name_Int",
                )
                .doNotIgnoreMethodParametersType()
                .build()

        assert(screenshotIdPreview1 != screenshotIdPreview2)
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are ignored, THEN the screenshotIds are the same`() {
        val screenshotIdPreview1 =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    methodParameters = "name_String",
                )
                .build()

        val screenshotIdPreview2 =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    methodParameters = "name_Int",
                )
                .build()

        assert(screenshotIdPreview1 == screenshotIdPreview2)
    }

    @Test
    fun `GIVEN preview with only widthDp and heightDp but both ignored, THEN show nothing`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(
                    widthDp = 33,
                    heightDp = 32,
                )
                .ignoreIdFor("widthDp")
                .ignoreIdFor("heightDp")
                .build()

        assert(
            previewScreenshotId == ""
        )
    }

    @Test
    fun `GIVEN Preview with name is fully of unsafe chars WHEN escaped THEN each character matches URl encoding pattern`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(methodParameters = ALL_UNSAFE_CHARS)
                .doNotIgnoreMethodParametersType()
                .encodeUnsafeCharacters()
                .build()
                // remove leading "_" due to doNotIgnoreMethodParametersType()
                .substring(1)

        val unicodePattern = "%[0-9A-F]+"
        val multipleUnicodePattern = "($unicodePattern)+"
        val entireStringWithMultipleUnicodePattern = "^$multipleUnicodePattern$"
        val unicodeRegex = Regex(entireStringWithMultipleUnicodePattern)
        assertTrue(unicodeRegex.matches(previewScreenshotId))
    }

    @Test
    fun `GIVEN Preview with name is fully of unsafe chars WHEN escaped THEN no unsafe chars contained`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(methodParameters = ALL_UNSAFE_CHARS)
                .doNotIgnoreMethodParametersType()
                .encodeUnsafeCharacters()
                .build()
                // remove leading "_" due to doNotIgnoreMethodParametersType()
                .substring(1)

        val unsafeCharsRegex = Regex("[$ALL_UNSAFE_CHARS]")
        assertFalse(unsafeCharsRegex.containsMatchIn(previewScreenshotId.substring(1)))
    }

    @Test
    fun `GIVEN Preview with name full of unsafe chars WHEN not escaped THEN contains the full unsafe chars as name`() {
        val previewScreenshotId =
            screenshotIdBuilder
                .passPreviewWithInfo(methodParameters = ALL_UNSAFE_CHARS)
                .doNotIgnoreMethodParametersType()
                .build()
                // remove leading "_" due to doNotIgnoreMethodParametersType()
                .substring(1)

        assertTrue(previewScreenshotId == ALL_UNSAFE_CHARS)
    }
}