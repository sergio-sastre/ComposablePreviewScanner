package sergio.sastre.composable.preview.scanner.jvm

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablesWithPreviewsFinder
import java.lang.reflect.Method

class JvmAnnotationFinder(
    private val annotationToScanClassName: String
) {

    operator fun invoke() = ComposablesWithPreviewsFinder(
        annotationToScanClassName = annotationToScanClassName,
        previewInfoMapper = JvmComposablePreviewInfoMapper(),
        previewMapperCreator = JvmPreviewMapperCreator()
    )

    data object JvmAnnotationInfo

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
                    sequenceOf(ProvideComposablePreview<JvmAnnotationInfo>()(this))
            }
    }
}