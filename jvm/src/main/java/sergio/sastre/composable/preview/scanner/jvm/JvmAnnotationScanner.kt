package sergio.sastre.composable.preview.scanner.jvm

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinder
import java.lang.reflect.Method

/**
 * Scans the target package trees for the annotationToScanClassname and returns their Composable,
 * which can be invoked.
 *
 * This is meant to be used for such cases in which the @Preview has AnnotationRetention.SOURCE,
 * like androidx.compose.desktop.ui.tooling.preview.Preview, and therefore it cannot be found by ClassGraph.
 * In such case, that @preview can be extra annotated with a custom annotation -> annotationToScanClassName
 */
class JvmAnnotationScanner(
    annotationToScanClassName: String
) : ComposablePreviewScanner<JvmAnnotationInfo>(
    JvmAnnotationFinder(annotationToScanClassName)
) {
    private object JvmAnnotationFinder {
        operator fun invoke(
            annotationToScanClassName: String
        ): ClasspathPreviewsFinder<JvmAnnotationInfo> =
            ClasspathPreviewsFinder(
                annotationToScanClassName = annotationToScanClassName,
                previewInfoMapper = JvmComposablePreviewInfoMapper(),
                previewMapperCreator = JvmPreviewMapperCreator()
            )

        private class JvmComposablePreviewInfoMapper :
            ComposablePreviewInfoMapper<JvmAnnotationInfo> {
            override fun mapToComposablePreviewInfo(
                parameters: AnnotationParameterValueList
            ): JvmAnnotationInfo = JvmAnnotationInfo
        }

        private class JvmPreviewMapperCreator : ComposablePreviewMapperCreator<JvmAnnotationInfo> {
            override fun createComposablePreviewMapper(
                previewMethod: Method,
                previewInfo: JvmAnnotationInfo,
                annotationsInfo: AnnotationInfoList?
            ): ComposablePreviewMapper<JvmAnnotationInfo> =
                object :
                    ComposablePreviewMapper<JvmAnnotationInfo>(
                        previewMethod = previewMethod,
                        previewInfo = previewInfo,
                        annotationsInfo = annotationsInfo
                    ) {
                    override fun mapToComposablePreviews(): Sequence<ComposablePreview<JvmAnnotationInfo>> =
                        sequenceOf(ProvideComposablePreview<JvmAnnotationInfo>().invoke(this))
                }
        }
    }
}
