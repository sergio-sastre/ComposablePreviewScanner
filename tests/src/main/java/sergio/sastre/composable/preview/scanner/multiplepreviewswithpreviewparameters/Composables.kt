package sergio.sastre.composable.preview.scanner.multiplepreviewswithpreviewparameters

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.StringProvider
import sergio.sastre.composable.preview.scanner.custompreviewannotation.MyCustomDarkModePreview
import sergio.sastre.composable.preview.scanner.previewparameters.Example

// Total: 14 Previews
@Preview                 // 1 Preview
@MyCustomDarkModePreview // 2 Previews
@PreviewDynamicColors    // 4 previews
@Composable
fun ExampleMultiplePreviewWithParams(
    @PreviewParameter(provider = StringProvider::class) name: String
){
    Example(name)
}