package sergio.sastre.composable.preview.scanner.common.multiplepreviewswithpreviewparameters

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.CommonStringProvider
import sergio.sastre.composable.preview.scanner.android.previewparameters.Example

// Total: 4 Previews
@Preview
@Preview(showBackground = true)
@Composable
fun ExampleMultiplePreviewWithParams(
    @PreviewParameter(provider = CommonStringProvider::class) name: String?
){
    Example(name)
}