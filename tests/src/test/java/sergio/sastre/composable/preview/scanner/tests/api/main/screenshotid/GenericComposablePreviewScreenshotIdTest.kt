package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.utils.androidPreviewBuilder
import sergio.sastre.composable.preview.scanner.utils.commonPreviewBuilder
import sergio.sastre.composable.preview.scanner.utils.glancePreviewBuilder

@RunWith(Parameterized::class)
class GenericComposablePreviewScreenshotIdTest(
    private val screenshotIdBuilder: () -> PreviewScreenshotIdBuilder<*>
) {

    companion object {
        private const val ALL_UNSAFE_CHARS = "<>:\\\"/\\\\|?*"

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Any> {
            return listOf(
                {
                    AndroidPreviewScreenshotIdBuilder(
                        androidPreviewBuilder(methodParameters = ALL_UNSAFE_CHARS)
                    )
                },
                {
                    CommonPreviewScreenshotIdBuilder(
                        commonPreviewBuilder(methodParameters = ALL_UNSAFE_CHARS)
                    )
                },
                {
                    GlancePreviewScreenshotIdBuilder(
                        glancePreviewBuilder(methodParameters = ALL_UNSAFE_CHARS)
                    )
                }
            )
        }
    }

    @Test
    fun `GIVEN Preview with name is fully of unsafe chars WHEN escaped THEN each character matches URl encoding pattern`() {
        val previewScreenshotId =
            screenshotIdBuilder()
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
            screenshotIdBuilder()
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
            screenshotIdBuilder()
                .doNotIgnoreMethodParametersType()
                .build()
                // remove leading "_" due to doNotIgnoreMethodParametersType()
                .substring(1)

        assertTrue(previewScreenshotId == ALL_UNSAFE_CHARS)
    }
}