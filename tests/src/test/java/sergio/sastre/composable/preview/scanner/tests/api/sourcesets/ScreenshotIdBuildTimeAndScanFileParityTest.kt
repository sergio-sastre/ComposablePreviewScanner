package sergio.sastre.composable.preview.scanner.tests.api.sourcesets

import androidx.wear.tiles.tooling.preview.TilePreviewData
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder

@RunWith(TestParameterInjector::class)
class ScreenshotIdBuildTimeAndScanFileParityTest {

    @Suppress("UNCHECKED_CAST")
    enum class Provider(
        private val buildTimeScanner: () -> SourceScannerWithResult<*, *>,
        private val sourceSetScanner: () -> SourceScannerWithResult<*, *>,
        private val packageTree: String,
        private val scanFileName: String,
        private val buildScreenshotId: (ComposablePreviewWithResult<*, *>) -> String,
    ) {
        ANDROID(
            buildTimeScanner = { AndroidComposablePreviewScanner() },
            sourceSetScanner = { AndroidComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
            packageTree = "sergio.sastre.composable.preview.scanner",
            scanFileName = "scan_result_main_android.json",
            buildScreenshotId = {
                AndroidPreviewScreenshotIdBuilder(it as ComposablePreview<AndroidPreviewInfo>).build()
            },
        ),
        GLANCE(
            buildTimeScanner = { GlanceComposablePreviewScanner() },
            sourceSetScanner = { GlanceComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
            packageTree = "sergio.sastre.composable.preview.scanner",
            scanFileName = "scan_result_main_glance.json",
            buildScreenshotId = {
                GlancePreviewScreenshotIdBuilder(it as ComposablePreview<GlancePreviewInfo>).build()
            },
        ),
        WEAR_TILE(
            buildTimeScanner = { WearTilePreviewScanner() },
            sourceSetScanner = { WearTilePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
            packageTree = "sergio.sastre.composable.preview.scanner.wear",
            scanFileName = "scan_result_main_wear.json",
            buildScreenshotId = {
                WearTilePreviewScreenshotIdBuilder(
                    it as ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>
                ).build()
            },
        );

        fun scanBuildTimeScreenshotIds(): List<String> =
            buildTimeScanner()
                .scanPackageTrees(packageTree)
                .includePrivatePreviews()
                .getPreviews()
                .map(buildScreenshotId)
                .sorted()

        fun scanFileScreenshotIds(): List<String> =
            sourceSetScanner()
                .scanFile(
                    targetInputStream = assetsFilePath(scanFileName).inputStream(),
                    customPreviewsInfoInputStream = assetsFilePath("custom_previews.json").inputStream(),
                )
                .includePrivatePreviews()
                .getPreviews()
                .map(buildScreenshotId)
                .sorted()
    }

    @Test
    fun `GIVEN previews WHEN comparing screenshot ids from build time and scanFile THEN they should match`(
        @TestParameter provider: Provider
    ) {
        val buildTimeScreenshotIds = provider.scanBuildTimeScreenshotIds()
        val scanFileScreenshotIds = provider.scanFileScreenshotIds()

        assertEquals(
            "Mismatch in screenshot IDs between buildTime and scanFile for ${provider.name}",
            buildTimeScreenshotIds,
            scanFileScreenshotIds
        )
    }
}
