package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.utils.previewBuilder
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder

@RunWith(TestParameterInjector::class)
class WearTileComposablePreviewScreenshotIdTest {

    @Test
    fun `GIVEN preview with default parameters, THEN show default device`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo()
        )

        assertEquals(
            "",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with name, THEN show name and default device`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo(
                name = "My preview name"
            )
        )

        assertEquals(
            "My_preview_name_id:wearos_small_round",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with group, THEN show group and default device`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo(
                group = "My preview group"
            )
        )

        assertEquals(
            "My_preview_group_id:wearos_small_round",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with custom device, THEN show custom device`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo(
                device = "id:wearos_large_round"
            )
        )

        assertEquals(
            "id:wearos_large_round",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with empty device, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo(
                device = ""
            )
        )

        assertEquals(
            "",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with fontScale, THEN show fontScale`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo(
                fontScale = 1.2f
            )
        )

        assertEquals(
            "id:wearos_small_round_FONT_1_2f",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with locale, THEN show locale`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo(
                locale = "en_US"
            )
        )

        assertEquals(
            "id:wearos_small_round_en_US",
            WearTilePreviewScreenshotIdBuilder(preview).build()
        )
    }

    @Test
    fun `GIVEN preview with default device but overrides it, THEN show overriden value`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo()
        )

        assertEquals(
            "SMALL_ROUND",
            WearTilePreviewScreenshotIdBuilder(preview)
                .overrideDefaultIdFor(
                    previewInfoName = "device",
                    applyInfoValue = {
                        when (it.device) {
                            "id:wearos_small_round" -> "SMALL_ROUND"
                            else -> it.device
                        }
                    }
                )
                .build()
        )
    }

    @Test
    fun `GIVEN preview with default device WHEN encodeUnsafeCharacters, THEN encode colon`() {
        val preview = previewBuilder(
            previewInfo = WearTilePreviewInfo()
        )

        assertEquals(
            "id%3Awearos_small_round",
            WearTilePreviewScreenshotIdBuilder(preview)
                .encodeUnsafeCharacters()
                .build()
        )
    }

    enum class PreviewKeyAndInfo(
        val key: String,
        val previewInfo: WearTilePreviewInfo
    ) {
        NAME("name", WearTilePreviewInfo(name = "name", device = "")),
        GROUP("group", WearTilePreviewInfo(group = "group", device = "")),
        DEVICE("device", WearTilePreviewInfo(device = "id:wearos_small_round")),
        FONT_SCALE("fontScale", WearTilePreviewInfo(fontScale = 1.5f, device = "")),
        LOCALE("locale", WearTilePreviewInfo(locale = "de", device = "")),
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
            WearTilePreviewScreenshotIdBuilder(preview)
                .ignoreIdFor(previewKeyAndInfo.key)
                .build()
        )
    }
}
