package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.utils.AndroidScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.CommonScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.GlanceScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.PreviewScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.SizedPreviewScreenshotIdBuilderProvider
import sergio.sastre.composable.preview.scanner.utils.WearTileScreenshotIdBuilderProvider

@RunWith(TestParameterInjector::class)
class GenericComposablePreviewScreenshotIdTest {

    enum class AnyProvider(
        val provider: PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<*>>
    ) {
        ANDROID(AndroidScreenshotIdBuilderProvider),
        COMMON(CommonScreenshotIdBuilderProvider),
        GLANCE(GlanceScreenshotIdBuilderProvider),
        WEAR_TILE(WearTileScreenshotIdBuilderProvider),
    }

    /**
     * WEAR_TILE does not support height and width as preview info params
     */
    enum class SizedProvider(
        val provider: SizedPreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<*>>
    ) {
        ANDROID(AndroidScreenshotIdBuilderProvider),
        COMMON(CommonScreenshotIdBuilderProvider),
        GLANCE(GlanceScreenshotIdBuilderProvider),
    }

    companion object {
        private const val ALL_UNSAFE_CHARS = "<>:\\\"/\\\\|?*"
    }

    @Test
    fun `GIVEN preview with only previewIndex, THEN show only index`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(previewIndex = 1)
                .build()

        assertEquals("1", previewScreenshotId)
    }

    @Test
    fun `GIVEN preview with previewIndex & displayName, THEN show only displayName without white spaces`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(
                    previewIndex = 1,
                    previewParameterDisplayName = "Display Name"
                )
                .build()

        assertEquals("Display_Name", previewScreenshotId)
    }

    @Test
    fun `GIVEN preview with previewIndex & displayName, WHEN replaceDisplayNameWithIndex, THEN show only index`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(
                    previewIndex = 1,
                    previewParameterDisplayName = "Display Name"
                )
                .replaceDisplayNameWithIndex()
                .build()

        assertEquals("1", previewScreenshotId)
    }

    @Test
    fun `GIVEN preview with only width greater than -1, THEN show only W$value$dp`(
        @TestParameter sizedProvider: SizedProvider
    ) {
        val previewScreenshotId =
            sizedProvider.provider
                .passPreviewWithInfo(widthDp = 33)
                .build()

        assertEquals("W33dp", previewScreenshotId)
    }

    @Test
    fun `GIVEN preview with only height greater than -1, THEN show only H$value$dp`(
        @TestParameter sizedProvider: SizedProvider
    ) {
        val previewScreenshotId =
            sizedProvider.provider
                .passPreviewWithInfo(heightDp = 33)
                .build()

        assertEquals("H33dp", previewScreenshotId)
    }

    @Test
    fun `GIVEN preview className and methodName, THEN show them only but separated by a dot`(
        @TestParameter anyProvider: AnyProvider,
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(
                    declaringClass = "MyClass",
                    methodName = "PreviewName",
                )
                .build()

        assertEquals("MyClass.PreviewName", previewScreenshotId)
    }

    @Test
    fun `GIVEN methodName ignored, THEN methodName is not included`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(
                    methodName = "PreviewName",
                )
                .ignoreMethodName()
                .build()

        assertFalse(
            previewScreenshotId.contains("PreviewName")
        )
    }

    @Test
    fun `GIVEN className ignored, THEN declaringClass is not included`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(
                    declaringClass = "MyClass",
                )
                .ignoreClassName()
                .build()

        assertFalse(
            previewScreenshotId.contains("MyClass")
        )
    }

    @Test
    fun `GIVEN methodParameters not ignored, THEN methodParameters is included`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(
                    methodParameters = "name_String",
                )
                .doNotIgnoreMethodParametersType()
                .build()

        assertTrue(
            previewScreenshotId.contains("name_String")
        )
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are not ignored, THEN the screenshotIds differ`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val screenshotIdPreview1 =
            anyProvider.provider
                .passPreviewWithInfo(
                    methodParameters = "name_String",
                )
                .doNotIgnoreMethodParametersType()
                .build()

        val screenshotIdPreview2 =
            anyProvider.provider
                .passPreviewWithInfo(
                    methodParameters = "name_Int",
                )
                .doNotIgnoreMethodParametersType()
                .build()

        assertNotEquals(screenshotIdPreview1, screenshotIdPreview2)
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are ignored, THEN the screenshotIds are the same`(
        @TestParameter anyProvider: AnyProvider,
    ) {
        val screenshotIdPreview1 =
            anyProvider.provider
                .passPreviewWithInfo(
                    methodParameters = "name_String",
                )
                .build()

        val screenshotIdPreview2 =
            anyProvider.provider
                .passPreviewWithInfo(
                    methodParameters = "name_Int",
                )
                .build()

        assertEquals(screenshotIdPreview1, screenshotIdPreview2)
    }

    @Test
    fun `GIVEN preview with only widthDp and heightDp but both ignored, THEN show nothing`(
        @TestParameter sizedProvider: SizedProvider
    ) {
        val previewScreenshotId =
            sizedProvider.provider
                .passPreviewWithInfo(
                    widthDp = 33,
                    heightDp = 32,
                )
                .ignoreIdFor("widthDp")
                .ignoreIdFor("heightDp")
                .build()

        assertEquals("", previewScreenshotId)
    }

    @Test
    fun `GIVEN Preview with name is fully of unsafe chars WHEN escaped THEN each character matches URl encoding pattern`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(methodParameters = ALL_UNSAFE_CHARS)
                .doNotIgnoreMethodParametersType()
                .encodeUnsafeCharacters()
                .build()

        val unicodePattern = "%[0-9A-F]+"
        val multipleUnicodePattern = "($unicodePattern)+"
        val entireStringWithMultipleUnicodePattern = "^$multipleUnicodePattern$"
        val unicodeRegex = Regex(entireStringWithMultipleUnicodePattern)
        assertTrue(unicodeRegex.matches(previewScreenshotId))
    }

    @Test
    fun `GIVEN Preview with name is fully of unsafe chars WHEN escaped THEN no unsafe chars contained`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(methodParameters = ALL_UNSAFE_CHARS)
                .doNotIgnoreMethodParametersType()
                .encodeUnsafeCharacters()
                .build()

        val unsafeCharsRegex = Regex("[$ALL_UNSAFE_CHARS]")
        assertFalse(unsafeCharsRegex.containsMatchIn(previewScreenshotId))
    }

    @Test
    fun `GIVEN Preview with name full of unsafe chars WHEN not escaped THEN contains the full unsafe chars as name`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val previewScreenshotId =
            anyProvider.provider
                .passPreviewWithInfo(methodParameters = ALL_UNSAFE_CHARS)
                .doNotIgnoreMethodParametersType()
                .build()

        assertEquals(ALL_UNSAFE_CHARS, previewScreenshotId)
    }
}
