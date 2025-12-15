package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.utils.previewBuilder

@RunWith(TestParameterInjector::class)
class CommonComposablePreviewScreenshotIdTest {

    @Test
    fun `GIVEN preview with only name, THEN show only name with underscores`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                name = "My preview name"
            )
        )

        assertEquals(
            "My_preview_name",
            CommonPreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with only group, THEN show group with underscores`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                group = "My preview group"
            )
        )

        assertEquals(
            "My_preview_group",
            CommonPreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with only locale af_ZA, THEN show only locale af_ZA`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                locale = "af_ZA"
            )
        )

        assertEquals(
            "af_ZA",
            CommonPreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = true, THEN show only WITH_BACKGROUND`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                showBackground = true
            )
        )

        assertEquals(
            "WITH_BACKGROUND",
            CommonPreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = false, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                showBackground = false
            )
        )

        assertEquals(
            "",
            CommonPreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with only backgroundColor = 16L, THEN show only its backgroundColor long value`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                backgroundColor = 16
            )
        )

        assertEquals(
            "BG_COLOR_16",
            CommonPreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = false but overrides it, THEN show overriden value`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                showBackground = false
            )
        )

        assertEquals(
            "WITHOUT_BACKGROUND",
            CommonPreviewScreenshotIdBuilder(preview)
                .overrideDefaultIdFor(
                    previewInfoName = "showBackground",
                    applyInfoValue = {
                        when (it.showBackground) {
                            true -> "WITH_BACKGROUND"
                            false -> "WITHOUT_BACKGROUND"
                        }
                    }
                )
                .build()
        )
    }

    enum class PreviewKeyAndInfo(
        val key: String,
        val previewInfo: CommonPreviewInfo
    ) {
        NAME("name", CommonPreviewInfo(name = "name")),
        GROUP("group", CommonPreviewInfo(group = "group")),
        WIDTH_DP("widthDp", CommonPreviewInfo(widthDp = 33)),
        HEIGHT_DP("heightDp", CommonPreviewInfo(heightDp = 33)),
        LOCALE("locale", CommonPreviewInfo(locale = "de")),
        SHOW_BACKGROUND("showBackground", CommonPreviewInfo(showBackground = true)),
        BACKGROUND_COLOR("backgroundColor", CommonPreviewInfo(backgroundColor = 100L)),
    }
    @Test
    fun `GIVEN preview info key ignored, THEN show nothing`(
        @TestParameter previewKeyAndInfo: PreviewKeyAndInfo
    ) {
        val preview = previewBuilder(
            previewInfo = previewKeyAndInfo.previewInfo
        )

        assertEquals(
            "",
            CommonPreviewScreenshotIdBuilder(preview)
                .ignoreIdFor(previewKeyAndInfo.key)
                .build()
        )
    }
}