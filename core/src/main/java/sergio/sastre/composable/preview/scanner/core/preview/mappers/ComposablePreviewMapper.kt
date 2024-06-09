package sergio.sastre.composable.preview.scanner.core.preview.mappers

import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

import java.lang.reflect.Method

/**
 * Maps its constructor params to a sequence of ComposablePreviews that can be invoked.
 * It is expected that a method annotated with @Preview turns into several ComposablePreviews one for each @Preview annotation,
 *
 * @param previewMethod The method annotated with @Preview
 * @param previewInfo The info passed into the @Preview, like name, group, apiLevel, locale, uiMode...
 * @param annotationsInfo Extra annotations applied to the @Preview method via ScanResultFilter#includeAnnotationInfoForAllOf(...)
 */
abstract class ComposablePreviewMapper<T>(
    open val previewMethod: Method,
    open val previewInfo: T,
    open val annotationsInfo: AnnotationInfoList?,
) {
    abstract fun mapToComposablePreviews(): Sequence<ComposablePreview<T>>
}