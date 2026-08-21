package sergio.sastre.composable.preview.scanner.wear

import androidx.wear.tiles.tooling.preview.TilePreviewData
import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import io.github.classgraph.ClassGraph
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreatorWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewWithPreviewParameterMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewWithPreviewParameterMapperWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinderWithResult
import sergio.sastre.composable.preview.scanner.wear.WearScannerUtils.valueForKey
import java.lang.reflect.Method

/**
 * Scans the target package trees for @androidx.wear.tiles.tooling.preview.Preview and returns their Composable,
 * which can be invoked to get TilePreviewData.
 */
class WearTileComposablePreviewScanner: ComposablePreviewScannerWithResult<WearTilePreviewInfo, TilePreviewData>(
    findComposableWithPreviewsInClass = ClasspathPreviewsFinderWithResult(
        annotationToScanClassName = "androidx.wear.tiles.tooling.preview.Preview",
        previewInfoMapper = WearComposablePreviewInfoMapper(),
        previewMapperCreator = WearPreviewMapperCreator(),
    ),
    defaultPackageTreesOfCrossModuleCustomPreviews = emptyList()
) {


    private class WearComposablePreviewInfoMapper :
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
