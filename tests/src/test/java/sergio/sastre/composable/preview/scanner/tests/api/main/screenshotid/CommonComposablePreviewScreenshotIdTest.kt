package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.utils.commonPreviewBuilder
import sergio.sastre.composable.preview.scanner.utils.previewBuilder

@RunWith(TestParameterInjector::class)
class CommonComposablePreviewScreenshotIdTest {
    @Test
    fun `GIVEN preview className and methodName, THEN show them only but separated by a dot`() {
        val preview = commonPreviewBuilder(
            declaringClass = "MyClass",
            methodName = "PreviewName",
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "MyClass.PreviewName"
        )
    }

    @Test
    fun `GIVEN preview with only previewIndex, THEN show only index`() {
        val preview = commonPreviewBuilder(
            previewIndex = 1,
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "1"
        )
    }

    @Test
    fun `GIVEN preview with only name, THEN show only name with underscores`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                name = "My preview name"
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "My_preview_name"
        )
    }

    @Test
    fun `GIVEN preview with only , THEN show group with underscores`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                group = "My preview group"
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "My_preview_group"
        )
    }

    @Test
    fun `GIVEN preview with only width greater than -1, THEN show only W$value$dp`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                widthDp = 33
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "W33dp"
        )
    }

    @Test
    fun `GIVEN preview with only height greater than -1, THEN show only H$value$dp`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                heightDp = 33
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "H33dp"
        )
    }

    @Test
    fun `GIVEN preview with only locale af_ZA, THEN show only locale af_ZA`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                locale = "af_ZA"
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "af_ZA"
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = true, THEN show only WITH_BACKGROUND`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                showBackground = true
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "WITH_BACKGROUND"
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = false, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                showBackground = false
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == ""
        )
    }

    @Test
    fun `GIVEN preview with only backgroundColor = 16L, THEN show only its backgroundColor long value`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                backgroundColor = 16
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview).build() == "BG_COLOR_16"
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = false but overrides it, THEN show overriden value`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                showBackground = false
            )
        )

        assert(
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
                .build() == "WITHOUT_BACKGROUND" // instead of "" as the default
        )
    }

    @Test
    fun `GIVEN className ignored, THEN declaringClass is not included`() {
        val preview = commonPreviewBuilder(
            declaringClass = "MyClass",
        )

        assert(
            !CommonPreviewScreenshotIdBuilder(preview)
                .ignoreClassName()
                .build()
                .contains("MyClass")
        )
    }

    @Test
    fun `GIVEN methodParameters not ignored, THEN declaringClass is included`() {
        val preview = commonPreviewBuilder(
            methodParameters = "name_String",
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
                .contains("name_String")
        )
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are not ignored, THEN the screenshotIds differ`() {
        val preview1 = commonPreviewBuilder(
            methodParameters = "name_String",
        )
        val preview2 = commonPreviewBuilder(
            methodParameters = "name_Int",
        )

        val screenshotIdPreview1 = CommonPreviewScreenshotIdBuilder(preview1)
            .doNotIgnoreMethodParametersType()
            .build()

        val screenshotIdPreview2 = CommonPreviewScreenshotIdBuilder(preview2)
            .doNotIgnoreMethodParametersType()
            .build()

        assert(screenshotIdPreview1 != screenshotIdPreview2)
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are ignored, THEN the screenshotIds are the same`() {
        val preview1 = commonPreviewBuilder(
            methodParameters = "name_String",
        )
        val preview2 = commonPreviewBuilder(
            methodParameters = "name_Int",
        )

        val screenshotIdPreview1 = CommonPreviewScreenshotIdBuilder(preview1).build()

        val screenshotIdPreview2 = CommonPreviewScreenshotIdBuilder(preview2).build()

        assert(screenshotIdPreview1 == screenshotIdPreview2)
    }

    @Test
    fun `GIVEN methodName ignored, THEN methodName is not included`() {
        val preview = commonPreviewBuilder(
            methodName = "PreviewName",
        )

        assert(
            !CommonPreviewScreenshotIdBuilder(preview)
                .ignoreMethodName()
                .build()
                .contains("PreviewName")
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

        assert(
            CommonPreviewScreenshotIdBuilder(preview)
                .ignoreIdFor(previewKeyAndInfo.key)
                .build() == ""
        )
    }

    @Test
    fun `GIVEN preview with only widthDp and heightDp but both ignored, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = CommonPreviewInfo(
                widthDp = 33,
                heightDp = 32,
            )
        )

        assert(
            CommonPreviewScreenshotIdBuilder(preview)
                .ignoreIdFor("widthDp")
                .ignoreIdFor("heightDp")
                .build() == "" // instead of "W33dp_H32dp" as the default
        )
    }

}