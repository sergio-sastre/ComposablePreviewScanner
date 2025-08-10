package sergio.sastre.composable.preview.scanner.android.multiplepreviewscrossmodule

import androidx.compose.runtime.Composable
import sergio.sastre.composable.preview.custompreviews.CrossModuleCustomPreview
import sergio.sastre.composable.preview.scanner.android.previewparameters.Example

@CrossModuleCustomPreview // 3 Previews
@Composable
fun ExampleMultiplePreviewWithParams() {
    Example("Example")
}