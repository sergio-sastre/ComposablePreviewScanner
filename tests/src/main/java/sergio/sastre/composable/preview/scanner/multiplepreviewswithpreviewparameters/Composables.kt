package sergio.sastre.composable.preview.scanner.multiplepreviewswithpreviewparameters

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import composable.preview.scanner.StringProvider
import sergio.sastre.composable.preview.scanner.previewparameters.Example

@PreviewLightDark
@Composable
fun ExampleMultiplePreviewWithParams(
    @PreviewParameter(provider = StringProvider::class) name: String
){
    Example(name)
}