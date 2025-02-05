package sergio.sastre.composable.preview.scanner.multiplepreviewscrossmodule

import androidx.compose.runtime.Composable
import sergio.sastre.composable.preview.custompreviews.CrossModuleCustomPreview
import sergio.sastre.composable.preview.scanner.previewparameter.Example

@CrossModuleCustomPreview // 3 Previews
@Composable
fun ExampleMultiplePreviewWithParams(){
    Example("Example")
}