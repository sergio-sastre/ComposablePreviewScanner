package sergio.sastre.composable.preview.scanner.jvm

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablesWithPreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.JvmAnnotationScanner.DesktopPreviewInfo
import java.lang.reflect.Method

/**
 * Scans the target package trees for the annotationToScanClassname and can returns their Composable,
 * which can be invoked.
 *
 * This is meant to be used for such cases in which the @Preview has AnnotationRetention.SOURCE,
 * like androidx.compose.desktop.ui.tooling.preview.Preview, and therefore it cannot be found by ClassGraph.
 * In such case, that @preview can be extra annotated with a custom annotation -> annotationToScanClassName
 */
class JvmAnnotationScanner(
    annotationToScanClassName: String
) : ComposablePreviewScanner<DesktopPreviewInfo>(
    ComposablesWithPreviewsFinder(
        annotationToScanClassName = annotationToScanClassName,
        previewInfoMapper = DesktopComposablePreviewInfoMapper(),
        previewMapperCreator = DesktopPreviewMapperCreator()
    )
) {
    data object DesktopPreviewInfo

    private class DesktopComposablePreviewInfoMapper :
        ComposablePreviewInfoMapper<DesktopPreviewInfo> {
        override fun mapToComposablePreviewInfo(
            parameters: AnnotationParameterValueList
        ): DesktopPreviewInfo = DesktopPreviewInfo
    }

    private class DesktopPreviewMapperCreator : ComposablePreviewMapperCreator<DesktopPreviewInfo> {
        override fun createComposablePreviewMapper(
            previewMethod: Method,
            previewInfo: DesktopPreviewInfo,
            annotationsInfo: AnnotationInfoList?
        ): ComposablePreviewMapper<DesktopPreviewInfo> =
            object :
                ComposablePreviewMapper<DesktopPreviewInfo>(
                    previewMethod = previewMethod,
                    previewInfo = previewInfo,
                    annotationsInfo = annotationsInfo
                ) {
                override fun mapToComposablePreviews(): Sequence<ComposablePreview<DesktopPreviewInfo>> =
                    sequenceOf(ProvideComposablePreview<DesktopPreviewInfo>()(this))
            }
    }
}
