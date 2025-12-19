package sergio.sastre.composable.preview.scanner.android.previewparameters

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.AndroidStringProvider

@Composable
fun Example(name: String?){
    Text(name.toString())
}

@Preview(group = "no-preview-parameter-limit")
@Composable
fun ExamplePreviewNoLimit(
    @PreviewParameter(provider = AndroidStringProvider::class) name: String?
){
    Example(name)
}

@Preview(group = "preview-parameter-limit=1")
@Composable
fun ExamplePreviewLimit1(
    @PreviewParameter(provider = AndroidStringProvider::class, limit = 1) name: String?
){
    Example(name)
}