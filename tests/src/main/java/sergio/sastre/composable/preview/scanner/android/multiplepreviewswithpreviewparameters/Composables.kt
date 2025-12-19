package sergio.sastre.composable.preview.scanner.android.multiplepreviewswithpreviewparameters

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.AndroidStringProvider
import sergio.sastre.composable.preview.scanner.android.custompreviewannotation.MyCustomDarkModePreview
import sergio.sastre.composable.preview.scanner.android.previewparameters.Example

// Total: 14 Previews
@Preview                 // 1 Preview
@MyCustomDarkModePreview // 2 Previews
@PreviewDynamicColors    // 4 previews
@Composable
fun ExampleMultiplePreviewWithParams(
    @PreviewParameter(provider = AndroidStringProvider::class) name: String?
){
    Example(name)
}