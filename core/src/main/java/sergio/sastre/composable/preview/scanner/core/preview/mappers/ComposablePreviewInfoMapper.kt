package sergio.sastre.composable.preview.scanner.core.preview.mappers

import io.github.classgraph.AnnotationParameterValueList

/**
 * Maps the AnnotationParameterValueList into a PreviewInfo of type T.
 * This would be "name", "group", "apiLevel", "locale", etc. for Android Previews.
 */
interface ComposablePreviewInfoMapper<T> {
    fun mapToComposablePreviewInfo(parameters: AnnotationParameterValueList): T
}