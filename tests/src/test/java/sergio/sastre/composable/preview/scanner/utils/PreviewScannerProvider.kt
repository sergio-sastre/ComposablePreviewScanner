package sergio.sastre.composable.preview.scanner.utils

import androidx.wear.tiles.tooling.preview.TilePreviewData
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner

sealed interface PreviewScannerProvider {
    fun scanBuildTime(): List<String>
    fun scanMainSourceSet(): List<String>
}

abstract class BasePreviewScannerProvider<T, R>(
    private val buildTimeScanner: () -> SourceScannerWithResult<T, R>,
    private val mainSourceSetScanner: () -> SourceScannerWithResult<T, R>,
    private val packageTree: String,
    private val scanFileName: String,
    private val customPreviewsFileName: String = "custom_previews.json",
    private val mapPreview: (ComposablePreviewWithResult<T, R>) -> String = {
        it.previewInfo.toString() + it.identity()
    },
) : PreviewScannerProvider {

    @OptIn(RequiresShowStandardStreams::class)
    override fun scanBuildTime(): List<String> =
        buildTimeScanner()
            .scanPackageTrees(packageTree)
            .includePrivatePreviews()
            .getPreviews()
            .map(mapPreview)
            .sorted()

    @OptIn(RequiresShowStandardStreams::class)
    override fun scanMainSourceSet(): List<String> =
        mainSourceSetScanner()
            .scanFile(
                targetInputStream = assetsFilePath(scanFileName).inputStream(),
                customPreviewsInfoInputStream = assetsFilePath(customPreviewsFileName).inputStream()
            )
            .includePrivatePreviews()
            .getPreviews()
            .map(mapPreview)
            .sorted()
}

data object AndroidPreviewScannerProvider : BasePreviewScannerProvider<AndroidPreviewInfo, Unit>(
    buildTimeScanner = { AndroidComposablePreviewScanner() },
    mainSourceSetScanner = { AndroidComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
    packageTree = "sergio.sastre.composable.preview.scanner",
    scanFileName = "scan_result_main_android.json",
)

data object GlancePreviewScannerProvider : BasePreviewScannerProvider<GlancePreviewInfo, Unit>(
    buildTimeScanner = { GlanceComposablePreviewScanner() },
    mainSourceSetScanner = { GlanceComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
    packageTree = "sergio.sastre.composable.preview.scanner",
    scanFileName = "scan_result_main_glance.json",
)

data object WearTilePreviewScannerProvider : BasePreviewScannerProvider<WearTilePreviewInfo, TilePreviewData>(
    buildTimeScanner = { WearTilePreviewScanner() },
    mainSourceSetScanner = { WearTilePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
    packageTree = "sergio.sastre.composable.preview.scanner.wear",
    scanFileName = "scan_result_main_wear.json",
    mapPreview = { preview ->
        preview.previewInfo.copy(device = preview.previewInfo.device.ifEmpty { "id:wearos_small_round" }).toString() +
            preview.identity()
    }
)

private fun ComposablePreviewWithResult<*, *>.identity(): String =
    "|$declaringClass|$methodName|$methodParametersType|$previewIndex"
