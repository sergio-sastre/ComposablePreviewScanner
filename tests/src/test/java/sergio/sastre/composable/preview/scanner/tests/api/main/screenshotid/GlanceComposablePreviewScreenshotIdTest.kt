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