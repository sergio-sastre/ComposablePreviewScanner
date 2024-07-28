package sergio.sastre.composable.preview.scanner.jvm

import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.JvmAnnotationFinder.JvmAnnotationInfo

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
    JvmAnnotationFinder(annotationToScanClassName).invoke()
)
