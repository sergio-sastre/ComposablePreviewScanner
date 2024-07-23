package sergio.sastre.composable.preview.scanner.jvm

import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.jvm.previewsfinder.JvmComposablesWithPreviewsFinder
import sergio.sastre.composable.preview.scanner.jvm.previewsfinder.JvmComposablesWithPreviewsFinder.PreviewWithoutInfo

class CommonMainComposablePreviewScanner(): ComposablePreviewScanner<PreviewWithoutInfo>(
    JvmComposablesWithPreviewsFinder("org.jetbrains.compose.ui.tooling.preview.Preview").invoke()
)