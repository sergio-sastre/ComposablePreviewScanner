package sergio.sastre.composable.preview.scanner.core.preview.mappers

import io.github.classgraph.AnnotationInfoList
import java.lang.reflect.Method

interface ComposablePreviewMapperCreator<T> {
    fun createComposablePreviewMapper(
        previewMethod: Method,
        previewInfo: T,
        annotationsInfo: AnnotationInfoList?
    ): ComposablePreviewMapper<T>
}