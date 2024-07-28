package sergio.sastre.composable.preview.scanner.jvm.common

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablesWithPreviewsFinder
import java.lang.reflect.Method

class CommonComposablePreviewFinder(
    private val annotationToScanClassName: String
) {

    operator fun invoke() = ComposablesWithPreviewsFinder(
        annotationToScanClassName = annotationToScanClassName,
        previewInfoMapper = JvmComposablePreviewInfoMapper(),
        previewMapperCreator = CommonPreviewMapperCreator()
    )

    data object CommonPreviewInfo

    private class JvmComposablePreviewInfoMapper :
        ComposablePreviewInfoMapper<CommonPreviewInfo> {
        override fun mapToComposablePreviewInfo(
            parameters: AnnotationParameterValueList
        ): CommonPreviewInfo = CommonPreviewInfo
    }

    private class CommonPreviewMapperCreator : ComposablePreviewMapperCreator<CommonPreviewInfo> {
        override fun createComposablePreviewMapper(
            previewMethod: Method,
            previewInfo: CommonPreviewInfo,
            annotationsInfo: AnnotationInfoList?
        ): ComposablePreviewMapper<CommonPreviewInfo> =
            object :
                ComposablePreviewMapper<CommonPreviewInfo>(
                    previewMethod = previewMethod,
                    previewInfo = previewInfo,
                    annotationsInfo = annotationsInfo
                ) {
                override fun mapToComposablePreviews(): Sequence<ComposablePreview<CommonPreviewInfo>> =
                    sequenceOf(ProvideComposablePreview<CommonPreviewInfo>()(this))
            }
    }
}