package sergio.sastre.composable.preview.scanner.android

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewWithPreviewParameterMapper
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinder
import java.lang.reflect.Method

/**
 * Scans the target package trees for @androidx.compose.ui.tooling.preview.Preview and can returns their Composable,
 * which can be invoked, along with other infos that might be relevant, like the Preview values (i.e. AndroidPreviewInfo)
 * and other annotations applied to the @Preview
 */
class AndroidComposablePreviewScanner : ComposablePreviewScanner<AndroidPreviewInfo>(
    findComposableWithPreviewsInClass = ClasspathPreviewsFinder(
        annotationToScanClassName = "androidx.compose.ui.tooling.preview.Preview",
        previewInfoMapper = AndroidComposablePreviewInfoMapper(),
        previewMapperCreator = AndroidPreviewMapperCreator(),
    ),
    defaultPackageTreesOfCrossModuleCustomPreviews = listOf("androidx.compose.ui.tooling.preview")
) {

    private class AndroidComposablePreviewInfoMapper :
        ComposablePreviewInfoMapper<AndroidPreviewInfo> {
        override fun mapToComposablePreviewInfo(parameters: AnnotationParameterValueList): AndroidPreviewInfo =
            AndroidPreviewInfo(
                name = parameters.valueForKey("name") ?: "",
                group = parameters.valueForKey("group") ?: "",
                apiLevel = parameters.valueForKey("apiLevel") ?: -1,
                widthDp = parameters.valueForKey("widthDp") ?: -1,
                heightDp = parameters.valueForKey("heightDp") ?: -1,
                locale = parameters.valueForKey("locale") ?: "",
                fontScale = parameters.valueForKey("fontScale") ?: 1f,
                showBackground = parameters.valueForKey("showBackground") ?: false,
                showSystemUi = parameters.valueForKey("showSystemUi") ?: false,
                backgroundColor = parameters.valueForKey("backgroundColor") ?: 0L,
                device = parameters.valueForKey("device") ?: "",
                uiMode = parameters.valueForKey("uiMode") ?: 0,
                wallpaper = parameters.valueForKey("wallpaper") ?: -1,
            )

        @Suppress("UNCHECKED_CAST")
        private fun <T> AnnotationParameterValueList.valueForKey(key: String): T? =
            this[key]?.value as T?
    }

    private class AndroidPreviewMapperCreator : ComposablePreviewMapperCreator<AndroidPreviewInfo> {
        override fun createComposablePreviewMapper(
            previewMethod: Method,
            previewInfo: AndroidPreviewInfo,
            annotationsInfo: AnnotationInfoList?
        ): ComposablePreviewMapper<AndroidPreviewInfo> =
            ComposablePreviewWithPreviewParameterMapper(
                previewParameterClassName = "androidx.compose.ui.tooling.preview.PreviewParameter",
                previewMethod = previewMethod,
                previewInfo = previewInfo,
                annotationsInfo = annotationsInfo,
            )
    }
}
