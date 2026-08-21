package sergio.sastre.composable.preview.scanner.wear.preview

import androidx.compose.runtime.Composable
import io.github.classgraph.AnnotationInfoList

/**
 * A Unique WearComposablePreview.
 *
 * @param T The type of the preview info
 * @param R The return type of the preview function (e.g., TilePreviewData or Unit)
 */
interface WearComposablePreview<T, R> {
    val previewInfo: T
    val previewIndex: Int?
    val previewIndexDisplayName: String?
    val otherAnnotationsInfo: AnnotationInfoList?
    val declaringClass: String
    val methodName: String
    val methodParametersType: String

    @Composable
    operator fun invoke(): R
}
