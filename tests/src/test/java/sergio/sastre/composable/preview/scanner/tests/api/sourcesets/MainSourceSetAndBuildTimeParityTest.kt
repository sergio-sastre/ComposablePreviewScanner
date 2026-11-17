package sergio.sastre.composable.preview.scanner.tests.api.sourcesets

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.utils.AndroidPreviewScannerProvider
import sergio.sastre.composable.preview.scanner.utils.GlancePreviewScannerProvider
import sergio.sastre.composable.preview.scanner.utils.PreviewScannerProvider
import sergio.sastre.composable.preview.scanner.utils.WearTilePreviewScannerProvider

@RunWith(TestParameterInjector::class)
class MainSourceSetAndBuildTimeParityTest {

    enum class AnyProvider(
        val provider: PreviewScannerProvider
    ) {
        ANDROID(AndroidPreviewScannerProvider),
        GLANCE(GlancePreviewScannerProvider),
        WEAR_TILE(WearTilePreviewScannerProvider),
    }

    @Test
    fun `GIVEN main source set WHEN comparing previews with build time THEN they find the exact same previews`(
        @TestParameter anyProvider: AnyProvider
    ) {
        val buildTimePreviews = anyProvider.provider.scanBuildTime()
        val mainSourceSetPreviews = anyProvider.provider.scanMainSourceSet()

        assertEquals(
            "Mismatch in preview count between buildTime and mainSourceSet for ${anyProvider.name}",
            buildTimePreviews.size,
            mainSourceSetPreviews.size
        )

        assertEquals(
            "Mismatch between buildTime and mainSourceSet previews for ${anyProvider.name}",
            buildTimePreviews,
            mainSourceSetPreviews
        )
    }
}
