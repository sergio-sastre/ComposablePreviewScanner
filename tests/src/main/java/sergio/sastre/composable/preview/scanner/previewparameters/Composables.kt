package sergio.sastre.composable.preview.scanner.previewparameters

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.StringProvider

@Composable
fun Example(name: String){
    Text(name)
}


@Preview(group = "no-preview-parameter-limit")
@Composable
fun ExamplePreviewNoLimit(
    @PreviewParameter(provider = StringProvider::class) name: String
){
    Example(name)
}

@Preview(group = "preview-parameter-limit=1")
@Composable
fun ExamplePreviewLimit1(
    @PreviewParameter(provider = StringProvider::class, limit = 1) name: String
){
    Example(name)
}