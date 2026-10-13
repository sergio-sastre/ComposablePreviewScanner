package sergio.sastre.composable.preview.scanner.core.preview.mappers

import io.github.classgraph.AnnotationInfoList
import java.lang.reflect.Method

interface ComposablePreviewMapperCreatorWithResult<T, R> {
    fun createComposablePreviewMapper(
        previewMethod: Method,
        previewInfo: T,
        annotationsInfo: AnnotationInfoList?
    ): ComposablePreviewMapperWithResult<T, R>
}

interface ComposablePreviewMapperCreator<T> : ComposablePreviewMapperCreatorWithResult<T, Unit> {
    override fun createComposablePreviewMapper(
        previewMethod: Method,
        previewInfo: T,
        annotationsInfo: AnnotationInfoList?
    ): ComposablePreviewMapper<T>
}
