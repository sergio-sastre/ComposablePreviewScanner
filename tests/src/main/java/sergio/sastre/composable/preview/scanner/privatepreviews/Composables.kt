package sergio.sastre.composable.preview.scanner.privatepreviews

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.StringProvider


@Composable
fun Example(name: String) {
    Text(name)
}

@Preview
@Composable
private fun ExamplePreview() {
    Example("Example")
}

@Preview
@Composable
private fun ExampleParameterizedPreview(
    @PreviewParameter(provider = StringProvider::class) name: String
) {
    Example(name)
}
