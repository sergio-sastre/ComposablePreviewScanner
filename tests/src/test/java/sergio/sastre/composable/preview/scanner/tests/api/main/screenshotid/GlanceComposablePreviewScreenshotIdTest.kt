package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.github.classgraph.AnnotationInfoList
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.utils.glancePreviewBuilder
import sergio.sastre.composable.preview.scanner.utils.previewBuilder

@RunWith(TestParameterInjector::class)
class GlanceComposablePreviewScreenshotIdTest {

    @Test
    fun `GIVEN preview with only width greater than -1, THEN show only W$value$dp`() {
        val preview = previewBuilder(
            previewInfo = GlancePreviewInfo(
                widthDp = 33
            )
        )

        assert(
            GlancePreviewScreenshotIdBuilder(preview).build() == "W33dp"
        )
    }

    @Test
    fun `GIVEN preview with only height greater than -1, THEN show only H$value$dp`() {
        val preview = previewBuilder(
            previewInfo = GlancePreviewInfo(
                heightDp = 33
            )
        )

        assert(
            GlancePreviewScreenshotIdBuilder(preview).build() == "H33dp"
        )
    }

    @Test
    fun `GIVEN preview with only widthDp = -1 but overrides it, THEN show overriden value`() {
        val preview = previewBuilder(
            previewInfo = GlancePreviewInfo(
               widthDp = -1
            )
        )

        assert(
            GlancePreviewScreenshotIdBuilder(preview)
                .overrideDefaultIdFor(
                    previewInfoName = "widthDp",
                    applyInfoValue = {
                        when (it.widthDp == -1) {
                            true -> "WRAPPED_WIDTH"
                            false -> "WIDTH_${it.widthDp}DP"
                        }
                    }
                )
                .build() == "WRAPPED_WIDTH" // instead of "" as the default
        )
    }

    @Test
    fun `GIVEN className ignored, THEN declaringClass is not included`() {
        val preview = glancePreviewBuilder(
            declaringClass = "MyClass",
        )

        assert(
            !GlancePreviewScreenshotIdBuilder(preview)
                .ignoreClassName()
                .build()
                .contains("MyClass")
        )
    }

    @Test
    fun `GIVEN methodParameters not ignored, THEN declaringClass is included`() {
        val preview = glancePreviewBuilder(
            methodParameters = "name_String",
        )

        assert(
            GlancePreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
                .contains("name_String")
        )
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are not ignored, THEN the screenshotIds differ`() {
        val preview1 = glancePreviewBuilder(
            methodParameters = "name_String",
        )
        val preview2 = glancePreviewBuilder(
            methodParameters = "name_Int",
        )

        val screenshotIdPreview1 = GlancePreviewScreenshotIdBuilder(preview1)
            .doNotIgnoreMethodParametersType()
            .build()

        val screenshotIdPreview2 = GlancePreviewScreenshotIdBuilder(preview2)
            .doNotIgnoreMethodParametersType()
            .build()

        assert(screenshotIdPreview1 != screenshotIdPreview2)
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are ignored, THEN the screenshotIds are the same`() {
        val preview1 = glancePreviewBuilder(
            methodParameters = "name_String",
        )
        val preview2 = glancePreviewBuilder(
            methodParameters = "name_Int",
        )

        val screenshotIdPreview1 = GlancePreviewScreenshotIdBuilder(preview1).build()

        val screenshotIdPreview2 = GlancePreviewScreenshotIdBuilder(preview2).build()

        assert(screenshotIdPreview1 == screenshotIdPreview2)
    }

    @Test
    fun `GIVEN methodName ignored, THEN methodName is not included`() {
        val preview = glancePreviewBuilder(
            methodName = "PreviewName",
        )

        assert(
            !GlancePreviewScreenshotIdBuilder(preview)
                .ignoreMethodName()
                .build()
                .contains("PreviewName")
        )
    }

    enum class PreviewKeyAndInfo(
        val key: String,
        val previewInfo: GlancePreviewInfo
    ) {
        WIDTH_DP("widthDp", GlancePreviewInfo(widthDp = 33)),
        HEIGHT_DP("heightDp", GlancePreviewInfo(heightDp = 33)),
    }
    @Test
    fun `GIVEN preview info key ignored, THEN show nothing`(
        @TestParameter previewKeyAndInfo: PreviewKeyAndInfo
    ) {
        val preview = previewBuilder(
            previewInfo = previewKeyAndInfo.previewInfo
        )

        assert(
            GlancePreviewScreenshotIdBuilder(preview)
                .ignoreIdFor(previewKeyAndInfo.key)
                .build() == ""
        )
    }

    @Test
    fun `GIVEN preview with only widthDp and heightDp but both ignored, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = GlancePreviewInfo(
                widthDp = 33,
                heightDp = 32,
            )
        )

        assert(
            GlancePreviewScreenshotIdBuilder(preview)
                .ignoreIdFor("widthDp")
                .ignoreIdFor("heightDp")
                .build() == "" // instead of "W33dp_H32dp" as the default
        )
    }
}