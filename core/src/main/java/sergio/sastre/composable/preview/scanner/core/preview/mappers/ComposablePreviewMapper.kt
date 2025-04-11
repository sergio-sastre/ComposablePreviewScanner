package sergio.sastre.composable.preview.scanner.core.preview.mappers

import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import java.lang.reflect.Method

/**
 * Used to map @Previews with params passed via @PreviewParameter or the like, so it creates a sequence of executable ComposablePreviews for each value.
 * This is necessary because Android and CommonMain @Previews use different @PreviewParameter (same name but different path).
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