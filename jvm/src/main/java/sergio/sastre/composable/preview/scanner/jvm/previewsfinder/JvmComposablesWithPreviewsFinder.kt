package sergio.sastre.composable.preview.scanner.jvm.previewsfinder

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablesWithPreviewsFinder
import java.lang.reflect.Method

class JvmComposablesWithPreviewsFinder(
    private val annotationToScanClassName: String
) {

    operator fun invoke() = ComposablesWithPreviewsFinder(
        annotationToScanClassName = annotationToScanClassName,
        previewInfoMapper = JvmComposablePreviewInfoMapper(),
        previewMapperCreator = JvmPreviewMapperCreator()
    )

    data object PreviewWithoutInfo

    private class JvmComposablePreviewInfoMapper :
        ComposablePreviewInfoMapper<PreviewWithoutInfo> {
        override fun mapToComposablePreviewInfo(
            parameters: AnnotationParameterValueList
        ): PreviewWithoutInfo = PreviewWithoutInfo
    }

    private class JvmPreviewMapperCreator : ComposablePreviewMapperCreator<PreviewWithoutInfo> {
        override fun createComposablePreviewMapper(
            previewMethod: Method,
            previewInfo: PreviewWithoutInfo,
            annotationsInfo: AnnotationInfoList?
        ): ComposablePreviewMapper<PreviewWithoutInfo> =
            object :
                ComposablePreviewMapper<PreviewWithoutInfo>(
                    previewMethod = previewMethod,
                    previewInfo = previewInfo,
                    annotationsInfo = annotationsInfo
                ) {
                override fun mapToComposablePreviews(): Sequence<ComposablePreview<PreviewWithoutInfo>> =
                    sequenceOf(ProvideComposablePreview<PreviewWithoutInfo>()(this))
            }
    }
}