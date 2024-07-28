package sergio.sastre.composable.preview.scanner.jvm.common

import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.JvmAnnotationFinder
import sergio.sastre.composable.preview.scanner.jvm.JvmAnnotationFinder.JvmAnnotationInfo

/**
 * Scans the target package trees for the common @Preview s and returns their Composable,
 * which can be invoked.
 *
 * This is meant to be used for @Composables using the @Preview located in "org.jetbrains.compose.ui.tooling.preview.Preview",
 * which is used in Compose Multiplatform for common previews.
 *
 * WARNING: Since ComposablePreviewScanner is based on ClassGraph, which is a Jvm-based Class Scanner,
 * this can only be used inside jvm sources, like Desktop and Android.
 */
class CommonComposablePreviewScanner : ComposablePreviewScanner<JvmAnnotationInfo>(
    JvmAnnotationFinder("org.jetbrains.compose.ui.tooling.preview.Preview").invoke()
)
