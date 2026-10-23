package sergio.sastre.composable.preview.scanner.wear

import androidx.wear.tiles.tooling.preview.TilePreviewData
import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreatorWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewWithPreviewParameterMapperWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinderWithResult
import java.lang.reflect.Method

/**
 * Scans the target package trees for @androidx.wear.tiles.tooling.preview.Preview and returns their Composable,
 * which can be invoked to get TilePreviewData.
 */
class WearTilePreviewScanner: ComposablePreviewScannerWithResult<WearTilePreviewInfo, TilePreviewData>(
    findComposableWithPreviewsInClass = ClasspathPreviewsFinderWithResult(
        annotationToScanClassName = "androidx.wear.tiles.tooling.preview.Preview",
        previewInfoMapper = WearPreviewInfoMapper(),
        previewMapperCreator = WearPreviewMapperCreator(),
    ),
    defaultPackageTreesOfCrossModuleCustomPreviews = emptyList()
) {


    private class WearPreviewInfoMapper :
        ComposablePreviewInfoMapper<WearTilePreviewInfo> {
        override fun mapToComposablePreviewInfo(parameters: AnnotationParameterValueList): WearTilePreviewInfo =
            WearTilePreviewInfo(
                name = parameters.valueForKey("name") ?: "",
                group = parameters.valueForKey("group") ?: "",
                locale = parameters.valueForKey("locale") ?: "",
                fontScale = parameters.valueForKey("fontScale") ?: 1f,
                device = parameters.valueForKey("device") ?: "",
            )

        @Suppress("UNCHECKED_CAST")
        private fun <T> AnnotationParameterValueList.valueForKey(key: String): T? =
            this[key]?.value as T?
    }

    private class WearPreviewMapperCreator : ComposablePreviewMapperCreatorWithResult<WearTilePreviewInfo, TilePreviewData> {
        override fun createComposablePreviewMapper(
            previewMethod: Method,
            previewInfo: WearTilePreviewInfo,
            annotationsInfo: AnnotationInfoList?
        ): ComposablePreviewMapperWithResult<WearTilePreviewInfo, TilePreviewData> =
            ComposablePreviewWithPreviewParameterMapperWithResult(
                previewParameterClassName = "androidx.compose.ui.tooling.preview.PreviewParameter",
                previewMethod = previewMethod,
                previewInfo = previewInfo,
                annotationsInfo = annotationsInfo,
            )
    }
}
